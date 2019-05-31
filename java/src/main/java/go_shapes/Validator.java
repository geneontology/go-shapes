/**
 * 
 */
package go_shapes;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.jena.JenaGraph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import fr.inria.lille.shexjava.util.Pair;
import fr.inria.lille.shexjava.validation.RecursiveValidation;
import fr.inria.lille.shexjava.validation.RefineValidation;
import fr.inria.lille.shexjava.validation.Status;
import fr.inria.lille.shexjava.validation.Typing;

/**
 * @author bgood
 *
 */
public class Validator {

	/**
	 * 
	 */
	public Validator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		String shexpath = "../shapes/MF_should.shex";
		String test_model_file = "../test_ttl/go_cams/inferred/expanded_reactome-homosapiens-A_tetrasaccharide_linker_sequence_is_required_for_GAG_synthesis.ttl";
		Model test_model = ModelFactory.createDefaultModel() ;
		test_model.read(test_model_file) ;
		ShexSchema schema = null;
		try {
			schema = GenParser.parseSchema(new File(shexpath).toPath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String focus_node_iri = null;//e.g. "http://model.geneontology.org/R-HSA-140342/R-HSA-211196_R-HSA-211207";
		String shape_id = null;//e.g. "http://purl.org/pav/providedBy/S-integer";
		Typing results = null;
		try {
			results = validateShex(schema, test_model, focus_node_iri, shape_id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean positive_only = true;
		String report = shexTypingToString(results, positive_only); 
		System.out.println(report);

	}

	public static Typing validateShex(ShexSchema schema, Model jena_model, String focus_node_iri, String shape_id) throws Exception {
		Typing result = null;
		RDF rdfFactory = new SimpleRDF();
		JenaRDF jr = new JenaRDF();
		JenaGraph shexy_graph = jr.asGraph(jena_model);
		if(focus_node_iri!=null) {
			Label shape_label = new Label(rdfFactory.createIRI(shape_id));
			RDFTerm focus_node = rdfFactory.createIRI(focus_node_iri);
			//recursive only checks the focus node against the chosen shape.  
			RecursiveValidation shex_recursive_validator = new RecursiveValidation(schema, shexy_graph);
			shex_recursive_validator.validate(focus_node, shape_label);
			result = shex_recursive_validator.getTyping();
		}else {
			RefineValidation shex_refine_validator = new RefineValidation(schema, shexy_graph);
			//refine checks all nodes in the graph against all shapes in schema 
			shex_refine_validator.validate();	
			result = shex_refine_validator.getTyping();
		}
		return result;
	}
	
	public static String shexTypingToString(Typing result, boolean positive_only) {
		String s = "";
		for(Pair<RDFTerm, Label> p : result.getStatusMap().keySet()) {
			Status r = result.getStatusMap().get(p);
			if(positive_only&&r.equals(Status.CONFORMANT)&&(!p.two.isGenerated())) {
				s=s+"node: "+p.one+"\tshape id: "+p.two+"\tresult: "+r.toString()+"\n";
			}else if(!positive_only){
				s=s+"node: "+p.one+"\tshape id: "+p.two+"\tresult: "+r.toString()+"\n";
				// e.g. node: <http://purl.obolibrary.org/obo/RO_HOM0000011>	shape id: _:SLGEN_0000	result: NONCONFORMANT
			}
		}
		return s;
	}
	
}
