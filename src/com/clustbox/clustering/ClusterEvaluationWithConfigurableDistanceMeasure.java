package com.clustbox.clustering;

import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.distance.DistanceMeasure;

/**
 * A Cluster evaluation with a configurable Distance Measure.
 * 
 * @author Gianmario Spacagna (gmspacagna@gmail.com)
 */
public interface ClusterEvaluationWithConfigurableDistanceMeasure extends ClusterEvaluation {

	public DistanceMeasure getDistanceMeasure();
	
	public void setDistanceMeasure(DistanceMeasure distanceMeasure);
}