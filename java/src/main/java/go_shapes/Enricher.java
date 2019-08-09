/**
 * 
 */
package go_shapes;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DC;

/**
 * @author bgood
 *
 */
public class Enricher {
	public static final String endpoint = "http://rdf.geneontology.org/blazegraph/sparql";
	/**
	 * 
	 */
	public Enricher() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String dir = "../test_ttl/go_cams/should_fail/";
		Map<String,Model> name_model = loadRDF(dir);
		System.out.println("Start on "+name_model.size()+" models "+System.currentTimeMillis()/1000);
		for(String name : name_model.keySet()) {
			Model model = name_model.get(name);
			model = enrichSuperClasses(model);
			write(model, "/Users/bgood/Desktop/test/shex/should_fail/enriched_"+name);
		}
		System.out.println("Finish on "+name_model.size()+" models "+System.currentTimeMillis()/1000);

	}

	public static Model enrichSuperClasses(Model model) {
		String getOntTerms = 
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> "
						+ "SELECT DISTINCT ?term " + 
						"        WHERE { " + 
						"        ?ind a owl:NamedIndividual . " + 
						"        ?ind a ?term . " + 
						"        FILTER(?term != owl:NamedIndividual)" + 
						"        FILTER(isIRI(?term)) ." + 
						"        }";
		String terms = "";
		try{
			QueryExecution qe = QueryExecutionFactory.create(getOntTerms, model);
			ResultSet results = qe.execSelect();

			while (results.hasNext()) {
				QuerySolution qs = results.next();
				Resource term = qs.getResource("term");
				terms+=("<"+term.getURI()+"> ");
			}
			qe.close();
		} catch(QueryParseException e){
			e.printStackTrace();
		}
		String superQuery = ""
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "CONSTRUCT { " + 
				"        ?term rdfs:subClassOf ?superclass ." + 
				"        ?term a owl:Class ." + 
				"        }" + 
				"        WHERE {" + 
				"        VALUES ?term { "+terms+" } " + 
				"        ?term rdfs:subClassOf* ?superclass ." + 
				"        FILTER(isIRI(?superclass)) ." + 
				"        }";

		Query query = QueryFactory.create(superQuery); 
		try ( 
				QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query) ) {
			qexec.execConstruct(model);
			qexec.close();
		} catch(QueryParseException e){
			e.printStackTrace();
		}
		return model;
	}

	public static void write(Model model, String outfilename) throws IOException {
		FileOutputStream o = new FileOutputStream(outfilename);
		model.write(o, "TURTLE");
		o.close();
	}
	
	public static Map<String, Model> loadRDF(String model_dir){
		Map<String, Model> name_model = new HashMap<String, Model>();
		File good_dir = new File(model_dir);
		File[] good_files = good_dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".ttl");
		    }
		});		
		for(File good_file : good_files) {
			Model model = ModelFactory.createDefaultModel() ;
			model.read(good_file.getAbsolutePath()) ;
			name_model.put(good_file.getName(), model);
		}	
		return name_model;
	}
}
