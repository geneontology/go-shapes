"""
A shex-based validator for GO-CAMs.

This can either be used via the command line, or as a module

"""

import click
import logging
from pyshex.evaluate import evaluate
from rdflib import Graph, Namespace, URIRef
from typing import Optional, Set, List, Union, Dict, Tuple, Any
from rdflib.namespace import RDF, RDFS, OWL
from SPARQLWrapper import SPARQLWrapper, JSON
from cachier import cachier
from dataclasses import dataclass, field
import datetime
import sys


GOSHAPES = Namespace("http://purl.obolibrary.org/obo/go/shapes/")

cmap = {
    "http://purl.obolibrary.org/obo/GO_0003674" : "MolecularFunction",
    "http://purl.obolibrary.org/obo/GO_0008150" : "BiologicalProcess",
    "http://purl.obolibrary.org/obo/GO_0005575" : "CellularComponent",
    "http://purl.obolibrary.org/obo/CHEBI_36080" : "Protein",
    "http://purl.obolibrary.org/obo/CHEBI_33695" : "InformationBiomacromolecule",
    "http://purl.obolibrary.org/obo/GO_0032991" : "Complex",
    "http://purl.obolibrary.org/obo/CARO_0000000" : "AnatomicalEntity",
    "http://purl.obolibrary.org/obo/ECO_0000000" : "Evidence"
}

shex_source = '../shapes/go-cam-shapes.shex'

def get_shexc():
    with open(shex_source,'r') as s:
        shexc = s.read()
    return shexc

@dataclass
class ValidationReport():

    shexc : Optional[str]
    rdf_file: Optional[str]
    all_successful : bool = True
    nodes_with_no_shape : List[URIRef] = field(default_factory=lambda: [])
    pass_list : Tuple[URIRef, URIRef, List[str]] = field(default_factory=lambda: [])
    fail_list : Tuple[URIRef, URIRef, List[str]] = field(default_factory=lambda: [])
    

@click.command()
@click.argument('rdf_file', nargs=-1)
@click.option('-v', '--verbose', count=True)
def validate_files(rdf_file : [str], verbose):
    if verbose >= 2:
        logging.basicConfig(level=logging.DEBUG)
    elif verbose == 1:
        logging.basicConfig(level=logging.INFO)
    else:
        logging.basicConfig(level=logging.WARNING)
    
    shexc = get_shexc()

    all_files_successful = True
    for f in rdf_file:
        rpt = validate(f, shexc)
        print(f'File: {f} Success: {rpt.all_successful} PASS: {len(rpt.pass_list)} FAIL: {len(rpt.fail_list)}')
        if not rpt.all_successful:
            all_files_successful = False
            for inst, sc, reason in rpt.fail_list:
                print(f'  FAIL: {inst} SHAPE: {sc} REASON: {reason}')
    print(f'Final report >> all files successful: { all_files_successful }')
    if all_files_successful:
        exit_code = 0
    else:
        exit_code = 1
    sys.exit(exit_code)
        
def validate(filename : str, shexc : Optional[str] = None) -> ValidationReport:
    if shexc is None:
        shexc = get_shexc()
    g = Graph()
    g.parse(filename, format="turtle")
    # TESTING...
    g.namespace_manager.bind('M', 'http://purl.obolibrary.org/obo/GO_0097325', override=True, replace=True)
    g.serialize(destination='foo.ttl', format='turtle')
    inst_cls_tuples = []
    smap = {}
    rpt = ValidationReport(shexc=shexc, rdf_file=filename)

    for inst,p,cls in g.triples( (None, RDF.type, None) ):
        if cls in [OWL.Class, OWL.NamedIndividual, OWL.AnnotationProperty, OWL.ObjectProperty]:
            continue
        inst_cls_tuples.append( (inst, cls) )
        ancs = get_ancestors(cls)
        #print(f'Ancs {cls} subClassOf {ancs}')
        shape_classes = []
        for a in ancs:
            if a in cmap:
                # inject
                #print(f'Injecting {cls} subClassOf {a}')
                g.add( (cls, RDF.type, OWL.Class) )
                g.add( (cls, RDFS.subClassOf, URIRef(a)) )
                shape_classes.append(GOSHAPES[cmap[a]])
        if shape_classes:
            logging.debug(f'IC={inst} type {cls} sc={shape_classes} ancs={len(ancs)}')
            smap[inst] = shape_classes
        else:
            rpt.nodes_with_no_shape.append(inst)
            logging.info(f"No shape class for tuple {inst} {cls}")

    rpt.all_successful = True
    for inst, shape_classes in smap.items():
        for sc in shape_classes:
            success, reason = evaluate(g, shexc, inst, sc)
            if success:
                logging.info(f"Success: {inst} {sc}")
                rpt.pass_list.append( (inst, sc, reason) )
            else:
                rpt.all_successful = False
                logging.info(f"Fail: {inst} {sc} Reason: {reason}")
                rpt.fail_list.append( (inst, sc, reason) )
    return rpt


@cachier(stale_after=datetime.timedelta(days=3))
def get_ancestors(cls):
    sparql = SPARQLWrapper("http://rdf.geneontology.org/sparql")
    logging.info("Made wrapper: {}".format(sparql))
    sparql.setQuery(f"""
    SELECT ?anc WHERE {{
    <{cls}> rdfs:subClassOf* ?anc .
    FILTER(isURI(?anc))
    }}
    """)
    sparql.setReturnFormat(JSON)
    results = sparql.query().convert()
    bindings = results['results']['bindings']
    ancs = [r['anc']['value'] for r in bindings ]
    return ancs

            
if __name__ == "__main__":
    validate_files()


