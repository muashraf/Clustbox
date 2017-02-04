package com.clustbox.clustering;

import java.io.File;
import java.io.IOException;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
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
		Cluster cluster = new Cluster();
		/*
		 * Create a new instance of the KMeans algorithm that will create 3
		 * clusters
		 */
		Clusterer km = new KMeans(3, 10, new EuclideanDistance());
		Dataset[] clusters = km.cluster(data);
		ClusterEvaluation sse = new SumOfSquaredErrors();
		double sseScore = sse.score(clusters);
		double intraClusterScore = cluster.intraCluster(clusters, new EuclideanDistance());
		double interClusterScore = cluster.interCluster(clusters, new EuclideanDistance());
		System.out.println("Sum of squared errors: " + sseScore + "\t intraCluster Score " + intraClusterScore
				+ "\t interCluster Score " + interClusterScore);
	}

	public double intraCluster(Dataset[] datas, DistanceMeasure dm) {
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

	public double interCluster(Dataset[] datas, DistanceMeasure dm) {
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