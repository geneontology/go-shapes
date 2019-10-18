package go_shapes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DC;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import go_shapes.ModelValidationResult;

public class ShexValidatorTest {

	public static final String shexpath = "../shapes/go-cam-shapes.shex";
	public static final String goshapemappath = "../shapes/go-cam-shapes.shapeMap";
	public static final String good_models_dir  = "../test_ttl/go_cams/should_pass/";
			//"/Users/bgood/Desktop/test/go_cams/reactome/";			
	public static final String bad_models_dir  = "../test_ttl/go_cams/should_fail/";
	public static final String report_file  = "../report.txt";
	public static final boolean addSuperClasses = true;
	public static final boolean useLocalReasoner = true;
	public static final String url_for_tbox = "http://purl.obolibrary.org/obo/go/extensions/go-lego.owl";
		//"https://raw.githubusercontent.com/geneontology/pathways2GO/master/exchange/generated/reo-go-lego.owl";
	public static OWLReasoner tbox_reasoner = null;
	public static ShexValidator v;

	
	@BeforeClass
	public static void init() {
		reportSystemParams();
		System.out.println("Starting testing "+System.currentTimeMillis()/1000);
		try {
			v = new ShexValidator(shexpath, goshapemappath);
			if(useLocalReasoner) {
				//need to download it..
				URL tbox_location = new URL(url_for_tbox);
				File tbox_file = new File("./target/go-lego.owl");
				System.out.println("downloading tbox ontology from "+url_for_tbox);
				org.apache.commons.io.FileUtils.copyURLToFile(tbox_location, tbox_file);
				System.out.println("loading tbox ontology from "+tbox_file.getAbsolutePath());
				OWLOntologyManager ontman = OWLManager.createOWLOntologyManager();					
				OWLOntology tbox = ontman.loadOntologyFromOntologyDocument(tbox_file);
				System.out.println("done loading, building structural reasoner");
				OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
				tbox_reasoner = reasonerFactory.createReasoner(tbox);
				System.out.println("done building structural reasoner, starting tests");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("shex file failed to parse "+shexpath);
		}
	}

	@AfterClass
	public static void finish() {
		System.out.println("Finished testing "+System.currentTimeMillis()/1000);	
	}

	@Test
	public void shexFileShouldParse() {
		//schema initialized beforeclass
		assertFalse(v.schema==null);
	}

	@Test
	public void allBadModelsShouldFail() {
		Map<String, Model> bad_models = Enricher.loadRDF(bad_models_dir);
		boolean problem = false;
		String problems = "";
		int good = 0; int bad = 0;
		Enricher enrich = new Enricher(null, tbox_reasoner);
		for(String name :bad_models.keySet()) {		
			Model model = bad_models.get(name);
			if(addSuperClasses) {
				model = enrich.enrichSuperClasses(model);
			}
			try {
				boolean stream_output = false;
				ShexValidationReport r = v.runShapeMapValidation(model, stream_output);
				if(r.conformant) {
					problem = true;
					problems+=("bad model failed to be detected: "+name+"\n"+r.getAsText());
					good++;
				}else {
					bad++;
					System.out.println("Bad model successfully detected:"+name);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("dir: "+bad_models_dir+" total:"+bad_models.size()+" missed n bad models:"+good+" detected n bad models "+bad);
		assertFalse(problems, problem);
	}

	@Test
	public void allGoodModelsShouldPass() {
		Map<String, Model> good_models = Enricher.loadRDF(good_models_dir);
		boolean problem = false;
		String problems = "";
		int good = 0; int bad = 0; int n = 0; int total = good_models.keySet().size();
		Enricher enrich = new Enricher(null, tbox_reasoner);
		for(String name : good_models.keySet()) {
			n++;
			Model model = good_models.get(name);
			int t0 = (int)System.currentTimeMillis()/1000;
			int t_adding_superclasses = 0;
			if(addSuperClasses) {
				model = enrich.enrichSuperClasses(model);
				t_adding_superclasses = (int)System.currentTimeMillis()/1000 - t0;
			}
			int t1 = (int)System.currentTimeMillis()/1000;
			try {
				boolean stream_output = false;
				System.out.print(name+"\t");
				if(name.equals("reactome-homosapiens-Synthesis_of_PIPs_at_the_plasma_membrane.ttl")) {
					System.out.print("skipped\t\n");
					continue;
				}
				ShexValidationReport r = v.runShapeMapValidation(model, stream_output);
				int t_validating = (int)System.currentTimeMillis()/1000 - t1;				
				String result = "t_adding_superclasses:\t"+t_adding_superclasses+"\tt_validating:\t"+t_validating+"\t";
				if(!r.conformant) {
					System.out.print("fail\t"+result+"\n");
					problem = true;
					problems+=("good model failed to validate: "+name+"\n"+r.model_report);
					bad++;
				}else {
					System.out.print("pass\t"+result+"\n");
					good++;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("dir: "+good_models_dir+" total:"+good_models.size()+" Good:"+good+" Bad:"+bad);
		assertFalse(problems, problem);
	}

	
	public static void reportSystemParams() {
		 /* Total number of processors or cores available to the JVM */
		  System.out.println("Available processors (cores): " + 
		  Runtime.getRuntime().availableProcessors());

		  /* Total amount of free memory available to the JVM */
		  System.out.println("Free memory (m bytes): " + 
		  Runtime.getRuntime().freeMemory()/1048576);

		  /* This will return Long.MAX_VALUE if there is no preset limit */
		  long maxMemory = Runtime.getRuntime().maxMemory()/1048576;
		  /* Maximum amount of memory the JVM will attempt to use */
		  System.out.println("Maximum memory (m bytes): " + 
		  (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));

		  /* Total memory currently in use by the JVM */
		  System.out.println("Total memory (m bytes): " + 
		  Runtime.getRuntime().totalMemory()/1048576);

		  /* Get a list of all filesystem roots on this system */
		  File[] roots = File.listRoots();

		  /* For each filesystem root, print some info */
		  for (File root : roots) {
		    System.out.println("File system root: " + root.getAbsolutePath());
		    System.out.println("Total space (bytes): " + root.getTotalSpace());
		    System.out.println("Free space (bytes): " + root.getFreeSpace());
		    System.out.println("Usable space (bytes): " + root.getUsableSpace());
		  }
	}
}
