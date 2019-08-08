package go_shapes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
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
	public static final String good_models_dir  = "../scala/target/should_pass/";
	public static final String bad_models_dir  = "../scala/target/should_fail/";
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
	public void modelFilesShouldParse() {
		Set<Model> good_models = loadRDF(good_models_dir);
		System.out.println("good models loaded: "+good_models.size());
		assertTrue("good models load: "+good_models.size(),good_models.size()>0);
		Set<Model> bad_models = loadRDF(bad_models_dir);
		System.out.println("bad models loaded: "+bad_models.size());
		assertTrue("bad models load: "+bad_models.size(),bad_models.size()>0);
	}
	
	@Test
	public void allBadModelsShouldFail() {
		ShexValidator v = new ShexValidator();
		v.GoQueryMap = GoQueryMap;
		Set<Model> bad_models = loadRDF(bad_models_dir);
		boolean problem = false;
		String problems = "";
		for(Model model : bad_models) {			
			try {
				boolean stream_output = false;
				ModelValidationResult r = v.runShapeMapValidation(schema, model, stream_output);
				if(r.model_is_valid) {
					problem = true;
					problems+=("bad model failed to be detected: "+r.model_title+"\n"+r.model_report);
				}	
				assertFalse("bad model not caught: "+r.model_title+"\n"+r.model_report, r.model_is_valid);
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
		Set<Model> good_models = loadRDF(good_models_dir);
		boolean problem = false;
		String problems = "";
		for(Model model : good_models) {			
			try {
				boolean stream_output = false;
				ModelValidationResult r = v.runShapeMapValidation(schema, model, stream_output);
				if(!r.model_is_valid) {
					problem = true;
					problems+=("good model failed to validate: "+r.model_title+"\n"+r.model_report);
				}				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		assertFalse(problems, problem);
	}
	
	private Set<Model> loadRDF(String model_dir){
		Set<Model> models = new HashSet<Model>();
		File good_dir = new File(model_dir);
		assertTrue(good_dir.exists()&&good_dir.isDirectory());
		File[] good_files = good_dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".ttl");
		    }
		});		
		for(File good_file : good_files) {
			Model model = ModelFactory.createDefaultModel() ;
			model.read(good_file.getAbsolutePath()) ;
			Statement s = model.createLiteralStatement(model.createResource(), DC.description, good_file.getName());
			model.add(s);
			models.add(model);
		}	
		return models;
	}

}
