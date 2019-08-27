/**
 * 
 */
package go_shapes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
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
	public Map<String, String> GoQueryMap;
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
	public static void main(String[] args) throws Exception {		
		ShexValidator v = new ShexValidator();
		String shexpath = null;//"../shapes/go-cam-shapes.shex";
		String model_file = "";//"../test_ttl/go_cams/should_pass/typed_reactome-homosapiens-Acetylation.ttl";
		boolean addSuperClasses = false;
		String extra_endpoint = null;
		Map<String, Model> name_model = new HashMap<String, Model>();
		// create Options object
		Options options = new Options();
		options.addOption("f", true, "ttl file or directory of ttl files to validate");
		options.addOption("s", true, "shex schema file");
		options.addOption("all", false, "if added will return a map of all shapes to all non bnodes in the input rdf");
		options.addOption("m", true, "query shape map file"); 
		options.addOption("e", false, "if added, will use rdf.geneontology.org to add subclass relations to the model");
		options.addOption("extra_endpoint", false, "if added, will use the additional endpoint at the indicated url - "
				+ "e.g. http://192.168.1.5:9999/blazegraph/sparql to provide additional suuperclass expansions.  "
				+ "Use this when the main GO endpoint rdf.geneontology.org does not contain all of the information required.");
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse( options, args);

		if(cmd.hasOption("f")) {
			model_file = cmd.getOptionValue("f");
			//accepts both single files and directories
			name_model = Enricher.loadRDF(model_file);
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
		if(cmd.hasOption("m")) { 
			String shapemappath = cmd.getOptionValue("m");
			v.GoQueryMap = makeGoQueryMap(shapemappath);
		}
		else {
			System.out.println("please provide a shape map file to validate.  e.g. -s ../../shapes/go-cam-shapes.shapemap");
			System.exit(0);
		}		
		if(cmd.hasOption("e")) {
			addSuperClasses = true;
		}
		if(cmd.hasOption("extra_endpoint")) {
			extra_endpoint = cmd.getOptionValue("extra_endpoint");
		}

		ShexSchema schema = null;
		try {
			if(shexpath==null) {
				URL shex_schema_url = new URL("https://raw.githubusercontent.com/geneontology/go-shapes/master/shapes/go-cam-shapes.shex");
				File shex_schema_file = new File("shex-schema.shex");
				org.apache.commons.io.FileUtils.copyURLToFile(shex_schema_url, shex_schema_file);
				schema = GenParser.parseSchema(shex_schema_file.toPath());			
			}else {
				schema = GenParser.parseSchema(new File(shexpath).toPath());	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileWriter w = new FileWriter("report_file.txt");
		int good = 0; int bad = 0;
		for(String name : name_model.keySet()) {
			Model test_model = name_model.get(name);
			if(addSuperClasses) {
				Enricher enrich = new Enricher(extra_endpoint);
				test_model = enrich.enrichSuperClasses(test_model);
			}
			if(run_all) {
				ModelValidationResult r = v.runGeneralValidation(test_model, schema, null, null);
				System.out.println("report for model:"+r.model_title+"\n"+r.model_report);
				//this is the main one - others can probably be dropped
			}else if(v.GoQueryMap!=null){
				//v.runShapeMapValidation(schema, test_model, true);
				boolean stream_output = true;
				ModelValidationResult r = v.runShapeMapValidation(schema, test_model, stream_output);
				w.write(name+"\t");
				if(!r.model_is_valid) {
					w.write("invalid\n");
					bad++;
					System.out.println(name+"\n\t"+r.model_report);
				}else {
					good++;
					w.write("valid\n");
				}
			}else { //tagging pattern
				ModelValidationResult r = v.runGoValidationWithTags(test_model, schema);
				System.out.println("GO specific report for model:"+r.model_title+"\n"+r.model_report);
			}
		}
		w.close();
		System.out.println("input: "+model_file+" total:"+name_model.size()+" Good:"+good+" Bad:"+bad);
	}

	public ModelValidationResult runShapeMapValidation(ShexSchema schema, Model test_model, boolean stream_output) throws Exception {
		Map<String, Typing> shape_node_typing = validateGoShapeMap(schema, test_model);
		ModelValidationResult r = new ModelValidationResult(test_model);
		RDF rdfFactory = new SimpleRDF();
		for(String shape_node : shape_node_typing.keySet()) {
			Typing typing = shape_node_typing.get(shape_node);
			String shape_id = shape_node.split(",")[0];
			String focus_node_iri = shape_node.split(",")[1];
			String result = getValidationReport(typing, rdfFactory, focus_node_iri, shape_id, true);
			r.model_report+=result;
		}
		if(r.model_report.contains("NONCONFORMANT")) {
			r.model_is_valid=false;
			if(stream_output) {
				System.out.println("Invalid model: GO shape map report for model:"+r.model_title+"\n"+r.model_report);
			}
		}else {
			r.model_is_valid=true;
			if(stream_output) {
				System.out.println("Valid model:"+r.model_title);
			}
		}
		return r;
	}

	public ModelValidationResult runGoShapeMapValidation(Model model, ShexSchema schema) {
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

	public ModelValidationResult runGoValidationWithTags(Model model, ShexSchema schema) {
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

	public String getValidationReport(Typing typing_result, RDF rdfFactory, String focus_node_iri, String shape_id, boolean only_negative) throws Exception {
		Label shape_label = new Label(rdfFactory.createIRI(shape_id));
		RDFTerm focus_node = rdfFactory.createIRI(focus_node_iri);
		Pair<RDFTerm, Label> p = new Pair<RDFTerm, Label>(focus_node, shape_label);
		Status r = typing_result.getStatusMap().get(p);
		String s = "";
		if(r!=null) {
			if(only_negative) {
				if(r.equals(Status.NONCONFORMANT)) {
					s+=p.two+"\t"+p.one+"\t"+r.toString()+"\n";
				}
			}else { //report all
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
		return getFocusNodesByTags(model, "<http://purl.obolibrary.org/obo/CHEBI_24431>");
	}
	public static Set<String> getMFnodes(Model model){
		return getFocusNodesByTags(model, "<http://purl.obolibrary.org/obo/GO_0003674>");
	}
	public static Set<String> getBPnodes(Model model){
		return getFocusNodesByTags(model, "<http://purl.obolibrary.org/obo/GO_0008150>");
	}
	public static Set<String> getCCnodes(Model model){
		return getFocusNodesByTags(model, "<http://purl.obolibrary.org/obo/GO_0005575>");
	}
	public static Set<String> getFocusNodesByTags(Model model, String category_uri){
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

	public static Set<String> getFocusNodesBySparql(Model model, String sparql){
		Set<String> nodes = new HashSet<String>();
		QueryExecution qe = QueryExecutionFactory.create(sparql, model);
		ResultSet results = qe.execSelect();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			Resource node = qs.getResource("x");
			nodes.add(node.getURI());
		}
		qe.close();
		return nodes;
	}

	public static Map<String, String> makeGoQueryMap(String shapemap_file) throws IOException{ //"../shapes/go-cam-shapes.shapeMap
		Map<String, String> shapelabel_sparql = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new FileReader(shapemap_file));
		String line = reader.readLine();
		String all = line;
		while(line!=null) {
			all+=line;
			line = reader.readLine();			
		}
		reader.close();
		String[] maps = all.split(",");
		for(String map : maps) {
			String sparql = StringUtils.substringBetween(map, "'", "'");
			sparql = sparql.replace("a/", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?c . ?c ");
			String[] shapemaprow = map.split("@");
			String shapelabel = shapemaprow[1];
			shapelabel = shapelabel.replace(">", "");
			shapelabel = shapelabel.replace("<", "");
			shapelabel = shapelabel.trim();
			shapelabel_sparql.put(shapelabel, sparql);
		}
		return shapelabel_sparql;
	}

	public Map<String, Typing> validateGoShapeMap(ShexSchema schema, Model jena_model) throws Exception {
		Map<String, Typing> shape_node_typing = new HashMap<String, Typing>();
		Typing result = null;
		RDF rdfFactory = new SimpleRDF();
		JenaRDF jr = new JenaRDF();
		//this shex implementation likes to use the commons JenaRDF interface, nothing exciting here
		JenaGraph shexy_graph = jr.asGraph(jena_model);
		//recursive only checks the focus node against the chosen shape.  
		RecursiveValidation shex_recursive_validator = new RecursiveValidation(schema, shexy_graph);
		for(String shapelabel : GoQueryMap.keySet()) {
			Label shape_label = new Label(rdfFactory.createIRI(shapelabel));
			Set<String> focus_nodes = getFocusNodesBySparql(jena_model, GoQueryMap.get(shapelabel));
			for(String focus_node_iri : focus_nodes) {
				RDFTerm focus_node = rdfFactory.createIRI(focus_node_iri);
				shex_recursive_validator.validate(focus_node, shape_label);
				result = shex_recursive_validator.getTyping();
				shape_node_typing.put(shapelabel+","+focus_node_iri, result);
			}
		}
		return shape_node_typing;
	}


}
