import $ivy.`org.apache.jena:apache-jena-libs:3.12.0`

import java.io.{File, FileReader, FileWriter}

import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.{OWL2, RDF, RDFS}

import scala.collection.JavaConverters._


/**
 * Run using Ammonite: amm enrich-with-superclasses.sc --infile inmodel.ttl --outfile outmodel.ttl
 */
@main
def main(endpoint: String = "http://rdf.geneontology.org/sparql", infile: os.Path, outfile: os.Path): Unit = {
    val rdfType = RDF.`type`.getURI
    val rdfsSubClassOf = RDFS.subClassOf.getURI
    val owlClass = OWL2.Class.getURI
    val owlNamedIndividual = OWL2.NamedIndividual.getURI

    val model = ModelFactory.createDefaultModel()
    val reader = new FileReader(infile.toIO)
    model.read(reader, "", "TURTLE")
    reader.close()

    val termQuery =
    s"""
        SELECT DISTINCT ?term
        WHERE {
        ?ind a <$owlNamedIndividual> .
        ?ind a ?term .
        FILTER(?term != <$owlNamedIndividual>)
        FILTER(isIRI(?term)) .
        }
    """
    val termQueryEx = QueryExecutionFactory.create(termQuery, model)
    val terms = termQueryEx.execSelect().asScala.map(qs => qs.getResource("term").getURI).map(t => s"<$t>").mkString(" ")
    termQueryEx.close()

    val superclassesQuery =
    s"""
        CONSTRUCT {
        ?term <$rdfsSubClassOf> ?superclass .
        ?term a <$owlClass> .
        }
        WHERE {
        VALUES ?term { $terms }
        ?term <$rdfsSubClassOf>* ?superclass .
        FILTER(isIRI(?superclass)) .
        }
    """
    val superclassesQueryEx = QueryExecutionFactory.sparqlService(endpoint, superclassesQuery)
    superclassesQueryEx.execConstruct(model)
    superclassesQueryEx.close()
    val writer = new FileWriter(outfile.toIO)
    model.write(writer, "TURTLE")
    writer.close()
}