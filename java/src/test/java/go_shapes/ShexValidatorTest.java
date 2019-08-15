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

import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import go_shapes.ModelValidationResult;

public class ShexValidatorTest {

	public static final String shexpath = "../shapes/go-cam-shapes.shex";
	public static final String goshapemappath = "../shapes/go-cam-shapes.shapeMap";
	public static final String good_models_dir  = "../test_ttl/go_cams/should_pass/";
		//	"/Users/bgood/Documents/GitHub/noctua-models/models/";
	public static final String bad_models_dir  = "../test_ttl/go_cams/should_fail/";
	public static final String report_file  = "../report.txt";
	public static final boolean addSuperClasses = true;
	public static ShexSchema schema;
	public static Map<String, String> GoQueryMap;

	@BeforeClass
	public static void init() {
		try {
			System.out.println("Starting schema parse init "+System.currentTimeMillis()/1000);
			schema = GenParser.parseSchema(new File(shexpath).toPath());	
			System.out.println("Finished schema parse init "+System.currentTimeMillis()/1000);		
			GoQueryMap = ShexValidator.makeGoQueryMap(goshapemappath);
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
		assertFalse(schema==null);
	}

	@Test
	public void allBadModelsShouldFail() {
		ShexValidator v = new ShexValidator();
		v.GoQueryMap = GoQueryMap;
		Map<String, Model> bad_models = Enricher.loadRDF(bad_models_dir);
		boolean problem = false;
		String problems = "";
		for(String name :bad_models.keySet()) {		
			Model model = bad_models.get(name);
			if(addSuperClasses) {
				model = Enricher.enrichSuperClasses(model);
			}
			try {
				boolean stream_output = false;
				ModelValidationResult r = v.runShapeMapValidation(schema, model, stream_output);
				if(r.model_is_valid) {
					problem = true;
					problems+=("bad model failed to be detected: "+name+"\n"+r.model_report);
				}	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		assertFalse(problems, problem);
	}

	@Test
	public void allGoodModelsShouldPass() {
		ShexValidator v = new ShexValidator();	
		v.GoQueryMap = GoQueryMap;
		Map<String, Model> good_models = Enricher.loadRDF(good_models_dir);
		boolean problem = false;
		String problems = "";
		int good = 0; int bad = 0;
		try {
			FileWriter w = new FileWriter(report_file);
			for(String name : good_models.keySet()) {
				Model model = good_models.get(name);
				if(addSuperClasses) {
					model = Enricher.enrichSuperClasses(model);
				}
				try {
					boolean stream_output = false;
					ModelValidationResult r = v.runShapeMapValidation(schema, model, stream_output);
					w.write(name+"\t");
					if(!r.model_is_valid) {
						problem = true;
						problems+=("good model failed to validate: "+name+"\n"+r.model_report);
						w.write("invalid\n");
						bad++;
					}else {
						good++;
						w.write("valid\n");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			w.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("dir: "+good_models_dir+" total:"+good_models.size()+" Good:"+good+" Bad:"+bad);
		assertFalse(problems, problem);
	}

}
