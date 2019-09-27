package go_shapes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
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
	public static final String good_models_dir  = "/Users/bgood/Desktop/test/go_cams/reactome/";
			//"../test_ttl/go_cams/should_pass/";
	//	"/Users/bgood/Documents/GitHub/noctua-models/models/";
	public static final String bad_models_dir  = "../test_ttl/go_cams/should_fail/";
	public static final String report_file  = "../report.txt";
	public static final boolean addSuperClasses = true;
	public static final boolean useLocalReasoner = true;
	public static OWLReasoner tbox_reasoner = null;
	public static ShexValidator v;

	@BeforeClass
	public static void init() {
		System.out.println("Starting testing "+System.currentTimeMillis()/1000);
		try {
			v = new ShexValidator(shexpath, goshapemappath);
			if(useLocalReasoner) {
				String tbox_file_2 = "/Users/bgood/gocam_ontology/REO.owl";
				String tbox_file = "/Users/bgood/gocam_ontology/go-lego-merged-9-23-2019-human.owl";
				OWLOntologyManager ontman = OWLManager.createOWLOntologyManager();	
				System.out.println("loading ontology");
				OWLOntology tbox = ontman.loadOntologyFromOntologyDocument(new File(tbox_file));
				System.out.println("done loading "+tbox_file);
				OWLOntology tbox2 = ontman.loadOntologyFromOntologyDocument(new File(tbox_file_2));
				System.out.println("done loading "+tbox_file_2);
				for(OWLAxiom a : tbox2.getAxioms()) {
					ontman.addAxiom(tbox, a);
				}
				System.out.println("done adding axioms from "+tbox_file_2);
				System.out.println("done loading, building reasoner");
				OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
				tbox_reasoner = reasonerFactory.createReasoner(tbox);
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
		int good = 0; int bad = 0;
		Enricher enrich = new Enricher(null, tbox_reasoner);
		for(String name : good_models.keySet()) {
			Model model = good_models.get(name);
			if(addSuperClasses) {
				model = enrich.enrichSuperClasses(model);
			}
			try {
				boolean stream_output = false;
				System.out.println("Validating (hopefully good) model: "+name);
				ShexValidationReport r = v.runShapeMapValidation(model, stream_output);
				if(!r.conformant) {
					System.out.println("Good model failed!: "+name);
					problem = true;
					problems+=("good model failed to validate: "+name+"\n"+r.model_report);
					bad++;
				}else {
					System.out.println("Good model validated: "+name);
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

}
