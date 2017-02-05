package com.clustbox.clustering;

import java.io.File;
import java.io.IOException;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.data.FileHandler;

public class Cluster {

	/**
	 * The distance measure used in the algorithm, defaults to Euclidean
	 * distance.
	 */
	private DistanceMeasure dm;

	public Cluster(DistanceMeasure dm) {
		this.dm = dm;
	}

	public static void main(String[] args) {
		/* Load a dataset */
		Dataset irisData = null;
		try {
			irisData = FileHandler.loadDataset(new File("data/iris.data"), 4, ",");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Iris Data: " + irisData);
		
		Cluster cluster = new Cluster(new EuclideanDistance());
		// TODO How to computing attribute?
		double[][] attr = null;
		Clusterer km = new CBKMeans(3, 10, cluster.dm, cluster.createCentroids(attr));
		Dataset[] clusters = km.cluster(irisData);
		ClusterEvaluation sse = new SumOfSquaredErrors();
		double sseScore = sse.score(clusters);
		double intraClusterScore = cluster.intraCluster(clusters);
		double interClusterScore = cluster.interCluster(clusters);
		System.out.println("Sum of squared errors: " + sseScore + "\t intraCluster Score " + intraClusterScore
				+ "\t interCluster Score " + interClusterScore);
	}

	protected Instance[] createCentroids(double[][] attributes) {
		int m = attributes[0].length;
		int k = attributes.length;
		Instance[] centroids = new Instance[k];
		for (int i = 0; i < k; i++) {
			assert attributes[i].length == m;
			centroids[i] = ClusterOperations.createInstance(attributes[i]);
		}
		return centroids;
	}

	public double intraCluster(Dataset[] datas) {
		double dw = 0, fw = 0;

		for (int i = 0; i < datas.length; i++) {
			for (int j = 0; j < datas[i].size(); j++) {
				Instance x = datas[i].instance(j);
				// calculate sum of intra cluster distances dw and count their
				// number.
				for (int k = j + 1; k < datas[i].size(); k++) {
					Instance y = datas[i].instance(k);
					double distance = dm.measure(x, y);
					dw += distance;
					fw++;
				}
			}
		}
		double wb = dw / fw;
		return wb;
	}

	public double interCluster(Dataset[] datas) {
		double db = 0, fb = 0;

		for (int i = 0; i < datas.length; i++) {
			for (int j = 0; j < datas[i].size(); j++) {
				Instance x = datas[i].instance(j);

				// calculate sum of inter cluster distances dw and count their
				// number.
				for (int k = i + 1; k < datas.length; k++) {
					for (int l = 0; l < datas[k].size(); l++) {
						Instance y = datas[k].instance(l);
						double distance = dm.measure(x, y);
						db += distance;
						fb++;
					}
				}
			}
		}
		double wb = (db / fb);
		return wb;
	}

}