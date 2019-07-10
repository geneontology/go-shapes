package go_shapes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import go_shapes.ModelValidationResult;

public class ShexValidatorTest {

	public static final String shexpath = "../shapes/go-cam-shapes.shex";
	public static final String good_models_dir  = "../test_ttl/go_cams/should_pass/";
	public static final String bad_models_dir  = "../test_ttl/go_cams/should_fail/";
	public static ShexSchema schema;
	
	@BeforeClass
	public static void init() {
		try {
			schema = GenParser.parseSchema(new File(shexpath).toPath());			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("shex file failed to parse "+shexpath);
		}
	}
	
	@Test
    public void shexFileShouldParse() {
		ShexSchema schema = null;
		try {
			schema = GenParser.parseSchema(new File(shexpath).toPath());			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("shex file failed to parse "+shexpath);
		}
    }
	
	@Test
	public void modelFilesShouldParse() {
		Set<Model> good_models = loadRDF(good_models_dir);
		assertTrue("good models load: "+good_models.size(),good_models.size()>0);
		Set<Model> bad_models = loadRDF(bad_models_dir);
		assertTrue("bad models load: "+bad_models.size(),bad_models.size()>0);
	}
	
	@Test
	public void allBadModelsShouldFail() {
		String focus_node_iri = null;//"http://model.geneontology.org/R-HSA-156582/R-HSA-174967";//e.g. "http://model.geneontology.org/R-HSA-140342/R-HSA-211196_R-HSA-211207";
		String shape_id = null;//"http://geneontology.org/MF";//e.g. "http://purl.org/pav/providedBy/S-integer";
		ShexValidator v = new ShexValidator();
		
		Set<Model> bad_models = loadRDF(bad_models_dir);
		for(Model model : bad_models) {			
			ModelValidationResult r = v.runValidation(model, schema, focus_node_iri, shape_id);
			assertFalse("bad model not caught: "+r.model_title, r.model_is_valid);
		}
	}
	
	@Test
	public void allGoodModelsShouldPass() {
		String focus_node_iri = null;//"http://model.geneontology.org/R-HSA-156582/R-HSA-174967";//e.g. "http://model.geneontology.org/R-HSA-140342/R-HSA-211196_R-HSA-211207";
		String shape_id = null;//"http://geneontology.org/MF";//e.g. "http://purl.org/pav/providedBy/S-integer";
		ShexValidator v = new ShexValidator();
		
		Set<Model> bad_models = loadRDF(good_models_dir);
		for(Model model : bad_models) {			
			ModelValidationResult r = v.runValidation(model, schema, focus_node_iri, shape_id);
			assertTrue("good model failed to validate: "+r.model_title,r.model_is_valid);
		}
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
			models.add(model);
		}	
		return models;
	}

}
