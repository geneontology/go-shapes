/**
 * 
 */
package go_shapes;

import java.util.HashSet;
import java.util.Set;

/**
 * @author bgood
 *
 */
public class ShexViolation extends Violation {

	Set<ShexExplanation> explanations;
	/**
	 * 
	 */
	public ShexViolation(String node_id) {
		super(node_id);
		explanations = new HashSet<ShexExplanation>();
	}
	public Set<ShexExplanation> getExplanations() {
		return explanations;
	}
	public void setExplanations(Set<ShexExplanation> explanations) {
		this.explanations = explanations;
	}
	public void addExplanation(ShexExplanation explanation) {
		this.explanations.add(explanation);
	}

	
}