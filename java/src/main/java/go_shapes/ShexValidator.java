/**
 * 
 */
package go_shapes;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.jena.JenaGraph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.apache.commons.rdf.simple.SimpleRDFTermFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.abstrsynt.Annotation;
import fr.inria.lille.shexjava.schema.abstrsynt.EachOf;
import fr.inria.lille.shexjava.schema.abstrsynt.RepeatedTripleExpression;
import fr.inria.lille.shexjava.schema.abstrsynt.Shape;
import fr.inria.lille.shexjava.schema.abstrsynt.ShapeExpr;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleConstraint;
import fr.inria.lille.shexjava.schema.abstrsynt.TripleExpr;
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
public class ShexValidator {
	public final static String shape_base = "http://purl.obolibrary.org/obo/go/shapes/";
	/**
	 * 
	 */
	public ShexValidator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		String shexpath = "../shapes/go-cam-shapes.shex";
		String model_dir = "../test_ttl/go_cams/should_pass/";
		String model_file = model_dir+"typed_reactome-homosapiens-Acetylation.ttl";
		Model test_model = ModelFactory.createDefaultModel() ;
		test_model.read(model_file) ;
		ShexSchema schema = null;
		try {
			schema = GenParser.parseSchema(new File(shexpath).toPath());			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String focus_node_iri = null;//"http://model.geneontology.org/R-HSA-156582/R-HSA-174967";//e.g. "http://model.geneontology.org/R-HSA-140342/R-HSA-211196_R-HSA-211207";
		String shape_id = null;//"http://geneontology.org/MF";//e.g. "http://purl.org/pav/providedBy/S-integer";
		ShexValidator v = new ShexValidator();
		ModelValidationResult r = v.runValidation(test_model, schema, focus_node_iri, shape_id);
	}
	
	public ModelValidationResult runValidation(Model model, ShexSchema schema,String focus_node_iri, String shape_id) {
		ModelValidationResult validation_result = new ModelValidationResult(model);
		//TODO - connect to Arachne and do this for real
		//if not already present, add biolink typing here
		validation_result = runOwlValidation(model, validation_result);
		try {
			Typing typing_results = validateShex(schema, model, focus_node_iri, shape_id);
			boolean positive_only = true;
			validation_result = shexTypingToReport(schema, typing_results, positive_only, validation_result); 
			//not working
			//printSchemaComments(schema);
			System.out.println(validation_result.model_report);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return validation_result;
	}
	
	
	private ModelValidationResult runOwlValidation(Model model, ModelValidationResult result) {
		// TODO Auto-generated method stub
		if(result==null) {
			result = new ModelValidationResult(model);
		}
		result.model_is_consistent = false;
		return result;
	}
	
	public static void printSchemaComments(ShexSchema schema) {
		for(Label label : schema.getRules().keySet()) {
			
			ShapeExpr shape_exp = schema.getRules().get(label);
			Shape shape_rule = (Shape) schema.getRules().get(label);
			
			List<Annotation> annotations = shape_rule.getAnnotations();
			for(Annotation a : annotations) {
				System.out.println(shape_rule.getId()+" \n\t"+a.getPredicate()+" "+a.getObjectValue());
			}
			//would need to make the following recursive to be complete.
			TripleExpr trp = shape_rule.getTripleExpression();
			Set<Annotation> sub_annotations = getAnnos(null, trp);
			for(Annotation a : sub_annotations) {
				System.out.println(shape_rule.getId()+"SUB \n\t"+a.getPredicate()+" "+a.getObjectValue());
			}
		}
	}

	public static Set<Annotation> getAnnos(Set<Annotation> annos, TripleExpr exp){
		if(annos==null) {
			annos = new HashSet<Annotation>();
		}
		if(exp instanceof TripleConstraint) {
			TripleConstraint tc = (TripleConstraint)exp;
			annos.addAll(tc.getAnnotations());
		}else if (exp instanceof RepeatedTripleExpression) {
			RepeatedTripleExpression rtc = (RepeatedTripleExpression)exp;
			TripleExpr sub_exp = rtc.getSubExpression();
			annos = getAnnos(annos, sub_exp);
		}else if (exp instanceof EachOf) {
			EachOf rtc = (EachOf)exp;
			List<TripleExpr> sub_exps = rtc.getSubExpressions();
			for(TripleExpr sub_exp : sub_exps) {
				annos = getAnnos(annos, sub_exp);
			}
		} 
		return annos;
	}


	public static ModelValidationResult shexTypingToReport(ShexSchema schema, Typing typing_result, boolean positive_only, ModelValidationResult validation_result) {
		String s = "";		
		Set<RDFTerm> all_nodes = null;
		Set<RDFTerm> nodes = findNonBNodes(typing_result);
		if(nodes!=null) {
			all_nodes = nodes;
		}
		for(Label test_shape : schema.getRules().keySet()) {		
			//Pair<RDFTerm, Label>
			for(RDFTerm node : all_nodes) {	
				Pair<RDFTerm, Label> p = new Pair<RDFTerm, Label>(node, test_shape);
				Status r = typing_result.getStatusMap().get(p);
				if(r!=null) {
					if(positive_only&&r.equals(Status.CONFORMANT)&&(!p.two.isGenerated())) {
						s=s+"shape id: "+p.two+"\tnode: "+p.one+"\tresult: "+r.toString()+"\n";
					}else if(!positive_only){
						s=s+"shape id: "+p.two+"\t node: "+p.one+"\t\tresult: "+r.toString()+"\n";
						// e.g. node: <http://purl.obolibrary.org/obo/RO_HOM0000011>	shape id: _:SLGEN_0000	result: NONCONFORMANT
					}	
				}
			}
		}
		validation_result.model_report = s;
		return validation_result;
	}

	public static Set<RDFTerm> findNonBNodes(Typing result){
		Set<RDFTerm> nodes = new HashSet<RDFTerm>();
		for(Pair<RDFTerm, Label> p : result.getStatusMap().keySet()) {
			Status r = result.getStatusMap().get(p);
			Label shape_id = p.two;
			String uri = shape_id.stringValue();
			if(r.equals(Status.CONFORMANT)&&(uri.startsWith(shape_base))) {
				nodes.add(p.one);
			}
		}
		return nodes;
	}

	public static Typing validateShex(ShexSchema schema, Model jena_model, String focus_node_iri, String shape_id) throws Exception {
		Typing result = null;
		RDF rdfFactory = new SimpleRDF();
		JenaRDF jr = new JenaRDF();
		//this shex implementation likes to use the commons JenaRDF interface, nothing exciting here
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
}
