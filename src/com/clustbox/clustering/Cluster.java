package com.clustbox.clustering;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.DatasetTools;
import net.sf.javaml.tools.data.FileHandler;

public class Cluster {

	public static void main(String[] args) {
		/* Load a dataset */
		Dataset data = null;
		try {
			data = FileHandler.loadDataset(new File("data/iris.data"), 4, ",");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Instance[] initCentroids = new Instance[4];
		for(int i=0; i<4 ; i++)
		{
			initCentroids [i] = data.get(i*10);
		}

		
		CBKMeans cluster = new CBKMeans();
		//double[] initCentriod = { 6.292500795048066, 2.2303537747886404, 3.803421765264713, 1.5086439695060503 };
		Clusterer km = new CBKMeans(4, 10, new EuclideanDistance(), initCentroids);
		//Clusterer km = new CBKMeans(3, 10, new EuclideanDistance());
		Dataset[] clusters = km.cluster(data);
		ClusterEvaluation sse = new SumOfSquaredErrors();
		double sseScore = sse.score(clusters);
		double intraClusterScore = cluster.intraCluster(clusters, new EuclideanDistance());
		double interClusterScore = cluster.interCluster(clusters, new EuclideanDistance());
		System.out.println("Sum of squared errors: " + sseScore + "\t intraCluster Score " + intraClusterScore
				+ "\t interCluster Score " + interClusterScore);
	}
}