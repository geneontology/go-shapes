/**
 * 
 */
package go_shapes;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.jena.JenaGraph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
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
	//TODO dig these out of the schema rather than hard coding here
	public final static String shape_base = "http://purl.obolibrary.org/obo/go/shapes/";
	public final static String ChemicalEntity = shape_base+"ChemicalEntity";
	public final static String MolecularFunction = shape_base+"MolecularFunction";
	public final static String BiologicalProcess = shape_base+"BiologicalProcess";
	public final static String CellularComponent = shape_base+"CellularComponent";
	public final static String GoCamEntity = shape_base+"GoCamEntity";
	/**
	 * 
	 */
	public ShexValidator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws ParseException {
		String shexpath = "";//"../shapes/go-cam-shapes.shex";
		String model_file = "";//"../test_ttl/go_cams/should_pass/typed_reactome-homosapiens-Acetylation.ttl";

		// create Options object
		Options options = new Options();
		options.addOption("f", true, "ttl file to validate");
		options.addOption("s", true, "shex schema file");
		options.addOption("all", false, "if added will return a map of all shapes to all non bnodes in the input rdf");
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse( options, args);
		
		if(cmd.hasOption("f")) {
			model_file = cmd.getOptionValue("f");
		}
		else {
		    System.out.println("please provide a file to validate.  e.g. -f ../../test_ttl/go_cams/should_pass/typed_reactome-homosapiens-Acetylation.ttl");
		    System.exit(0);
		}
		if(cmd.hasOption("s")) {
			shexpath = cmd.getOptionValue("s");
		}
		else {
		    System.out.println("please provide a shex schema file to validate.  e.g. -s ../../shapes/go-cam-shapes.shex");
		    System.exit(0);
		}
		boolean run_all = false;
		if(cmd.hasOption("all")) {
			run_all = true;
		}
		
		
		Model test_model = ModelFactory.createDefaultModel() ;
		test_model.read(model_file) ;
		ShexSchema schema = null;
		try {
			schema = GenParser.parseSchema(new File(shexpath).toPath());			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ShexValidator v = new ShexValidator();
		if(run_all) {
			ModelValidationResult r = v.runGeneralValidation(test_model, schema, null, null);
			System.out.println("report for model:"+r.model_title+"\n"+r.model_report);
		}else {
			ModelValidationResult r = v.runGoValidation(test_model, schema);
			System.out.println("GO specific report for model:"+r.model_title+"\n"+r.model_report);
		}
		//not working
		//printSchemaComments(schema);
	}

	public ModelValidationResult runGoValidation(Model model, ShexSchema schema) {
		RDF rdfFactory = new SimpleRDF();
		ModelValidationResult validation_result = new ModelValidationResult(model);
		//TODO - connect to Arachne and do this for real
		//if not already present, add biolink typing here
		validation_result = runOwlValidation(model, validation_result);
		try {
			Set<String> mfs = getMFnodes(model);
			for(String mf : mfs ) {
				String s = getValidationReport(rdfFactory, model, schema, mf, MolecularFunction);
				validation_result.model_report+=s;
			}
			Set<String> bps = getBPnodes(model);
			for(String bp : bps ) {
				String s = getValidationReport(rdfFactory, model, schema, bp, BiologicalProcess);
				validation_result.model_report+=s;
			}
			Set<String> ccs = getCCnodes(model);
			for(String cc : ccs ) {
				String s = getValidationReport(rdfFactory, model, schema, cc, CellularComponent);
				validation_result.model_report+=s;
			}
//			Set<String> chemicals = getChemicalnodes(model);
//			for(String c : chemicals ) {
//				String s = getValidationReport(rdfFactory, model, schema, c, ChemicalEntity);
//				validation_result.model_report+=s;
//			}
			//get all categorized nodes...
//			Set<String> all = getFocusNodes(model, null);
//			for(String a : all ) {
//				String s = getValidationReport(rdfFactory, model, schema, a, GoCamEntity);
//				validation_result.model_report+=s;
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return validation_result;
	}

	public String getValidationReport(RDF rdfFactory, Model model, ShexSchema schema,String focus_node_iri, String shape_id) throws Exception {
		Typing typing_result = validateShex(schema, model, focus_node_iri, shape_id);
		boolean positive_only = false;
		//validation_result = shexTypingToReport(schema, typing_results, positive_only, validation_result); 
		Label shape_label = new Label(rdfFactory.createIRI(shape_id));
		RDFTerm focus_node = rdfFactory.createIRI(focus_node_iri);
		Pair<RDFTerm, Label> p = new Pair<RDFTerm, Label>(focus_node, shape_label);
		Status r = typing_result.getStatusMap().get(p);
		String s = "";
		if(r!=null) {
			if(positive_only&&r.equals(Status.CONFORMANT)&&(!p.two.isGenerated())) {
				s+=p.two+"\t"+p.one+"\t"+r.toString()+"\n";
			}else if(!positive_only){
				s+=p.two+"\t"+p.one+"\t"+r.toString()+"\n";
			}	
		}
		return s;
	}
	
	
	public ModelValidationResult runGeneralValidation(Model model, ShexSchema schema,String focus_node_iri, String shape_id) {
		ModelValidationResult validation_result = new ModelValidationResult(model);
		//TODO - connect to Arachne and do this for real
		//if not already present, add biolink typing here
		validation_result = runOwlValidation(model, validation_result);
		try {
			Typing typing_results = validateShex(schema, model, focus_node_iri, shape_id);
			boolean positive_only = false;
			validation_result = shexTypingToReport(schema, typing_results, positive_only, validation_result); 
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
						s+=p.two+"\t"+p.one+"\t"+r.toString()+"\n";
					}else if(!positive_only){
						s+=p.two+"\t"+p.one+"\t"+r.toString()+"\n";
					}	
				}
			}
		}
		validation_result.model_report = validation_result.model_report+"\n\n"+s;
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

	public static Set<String> getChemicalnodes(Model model){
		return getFocusNodes(model, "<http://purl.obolibrary.org/obo/CHEBI_24431>");
	}
	public static Set<String> getMFnodes(Model model){
		return getFocusNodes(model, "<http://purl.obolibrary.org/obo/GO_0003674>");
	}
	public static Set<String> getBPnodes(Model model){
		return getFocusNodes(model, "<http://purl.obolibrary.org/obo/GO_0008150>");
	}
	public static Set<String> getCCnodes(Model model){
		return getFocusNodes(model, "<http://purl.obolibrary.org/obo/GO_0005575>");
	}
	public static Set<String> getFocusNodes(Model model, String category_uri){
		if(category_uri==null) {
			category_uri = "?any";
		}
		Set<String> nodes = new HashSet<String>();
		String q = "select ?node where { " + 
				" ?node <https://w3id.org/biolink/vocab/category> "+category_uri + 
				" }";
		QueryExecution qe = QueryExecutionFactory.create(q, model);
		ResultSet results = qe.execSelect();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			Resource node = qs.getResource("node");
			nodes.add(node.getURI());
		}
		qe.close();
		return nodes;
	}

}
