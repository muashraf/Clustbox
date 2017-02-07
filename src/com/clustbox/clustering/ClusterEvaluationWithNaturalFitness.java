package com.clustbox.clustering;

import net.sf.javaml.clustering.evaluation.ClusterEvaluation;

public interface ClusterEvaluationWithNaturalFitness extends ClusterEvaluation {
	
	/**
	 * Returns true if the cluster evaluation is natural (the higher the better), false otherwise.
	 * @return
	 */
	public boolean isNatural();

}