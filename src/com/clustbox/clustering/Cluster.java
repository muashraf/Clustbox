package com.clustbox.clustering;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.CosineSimilarity;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.distance.JaccardIndexSimilarity;
import net.sf.javaml.distance.ManhattanDistance;
import net.sf.javaml.distance.MinkowskiDistance;
import net.sf.javaml.distance.NormalizedEuclideanDistance;
import net.sf.javaml.distance.PearsonCorrelationCoefficient;
import net.sf.javaml.tools.DatasetTools;
import net.sf.javaml.tools.data.FileHandler;

public class Cluster {
	public Dataset data = null;
	public DistanceMeasure dm;
	public List evalMetrics;
	public ClusterEvaluationWithNaturalFitness dbEval = new DaviesBouldinScore();

	public Cluster() {
	}

	public enum Algo {
		SimpleKMeans, IterativeKMeans, SimpleKMedoids, IterativeKMedoids
	}

	public void runClustering(HashMap formElements) throws IOException {

		Clusterer km;
		evalMetrics = getEvalMetrics(formElements);
		dm = getSimilarityMeasure(formElements.get("similarityMeasure"));
		
		/* Load a dataset */
		data = FileHandler.loadDataset(new File((String) formElements.get("dataFile")), 4, ",");

		/* Run Simple KMeans or KMedoids with input K or centroids and return */
		if (formElements.containsKey("simpleKMeans") || formElements.containsKey("simpleKMedoids")) {
			int k;
			Instance[] centroids = null;
			if (formElements.containsKey("noOfClusters")) {
				k = Integer.parseInt((String) formElements.get("noOfClusters"));
			} else if (formElements.containsKey("centroidsSource")) {
				Dataset initCentroidsDS = FileHandler
						.loadDataset(new File((String) formElements.get("centroidsSource")), 4, ",");
				ArrayList<Instance> initCentroids = new ArrayList<Instance>();
				for (Instance ins : initCentroidsDS) {
					initCentroids.add(ins);
				}
				centroids = initCentroids.toArray(new Instance[0]);
				k = initCentroids.size();
			} else {
				System.out.println("Error: Please provide either K or initial centroids for Simle KMeans/KMedoids run");
				return;
			}

			/* Run the KMeans or KMedoids Algo */
			if (formElements.containsKey("simpleKMedoids")) {
				km = new CBKMedoids(k, 100, dm, centroids);
			} else {
				km = new CBKMeans(k, 100, dm, centroids);
			}

			Dataset[] clusters = km.cluster(data);
			int cnt = 0;
			for (Dataset clust : clusters) {
				cnt++;
				FileHandler.exportDataset(clust, new File("Cluster-" + cnt + ".data"), false, ",");
				System.out.println("Dumped cluster data to Cluster-" + cnt + ".data");
			}
			return;
		}

		/* Run the Iterative KMeans/KMedoids algorithms */
		else if (formElements.containsKey("iterativeKMeans") || formElements.containsKey("iterativeKMedoids")) {
			int k;
			/* If K is given only run best Centroids assessment */
			if (formElements.containsKey("noOfClusters")) {
				k = Integer.parseInt((String) formElements.get("noOfClusters"));
				Instance[] bestCentroids = bestCentroids(k);
				return;
			} else {
				int KMIN = 2;
				int KMAX = 10; // data.size()/2;
				double bestScore = 0; // Initialize of least possible score
				Dataset[] bestClusters;
				for (k = KMIN; k < KMAX; k++) {
					Instance[] bestCentroids = bestCentroids(k);
					if (formElements.containsKey("iterativeKMedoids")) {
						km = new CBKMedoids(k, 100, dm, bestCentroids);
					} else {
						km = new CBKMeans(k, 100, dm, bestCentroids);
					}
					Dataset[] tempClusters = km.cluster(data);
					double tempScore = dbEval.score(tempClusters);
					System.out.println(tempScore);
					if (dbEval.compareScore(bestScore, tempScore)) {
						bestScore = tempScore;
						bestClusters = tempClusters;
					}
				}
			}
		}
	}

	private List getEvalMetrics(HashMap formElements) {
		// TODO Auto-generated method stub
		return null;
	}

	private DistanceMeasure getSimilarityMeasure(Object similarityElement) {
		DistanceMeasure dm;
		if (similarityElement.equals("CosineSimilarity"))
			dm = new CosineSimilarity();
		else if (similarityElement.equals("JaccardIndexSimilarity"))
			dm = new JaccardIndexSimilarity();
		else if (similarityElement.equals("ManhattanDistance"))
			dm = new ManhattanDistance();
		else if (similarityElement.equals("MinkowskiDistance"))
			dm = new MinkowskiDistance();
		else if (similarityElement.equals("NormalizedEuclideanDistance"))
			dm = new NormalizedEuclideanDistance(data);
		else if (similarityElement.equals("PearsonCorrelationCoefficient"))
			dm = new PearsonCorrelationCoefficient();
		else
			dm = new EuclideanDistance();
		return dm;
	}

	protected Instance[] bestCentroids(int k) {
		Instance[] bestCentroids = null;
		double bestScore = 0;
		Dataset[] bestClusters;
		for (int i = 0; i < 1000; i++) {
			Random rg = new Random(System.currentTimeMillis());
			Dataset Centroids = DatasetTools.bootstrap(data, k, rg);
			Clusterer km;
			ArrayList<Instance> initCentroids = new ArrayList<Instance>();
			for (Instance ins : Centroids) {
				initCentroids.add(ins);
			}
			Instance[] tempCentroids = initCentroids.toArray(new Instance[0]);
			km = new CBKMeans(k, 100, dm, bestCentroids);
			Dataset[] tempClusters = km.cluster(data);
			double tempScore = dbEval.score(tempClusters);
			System.out.println("bestC: " + tempScore);
			if (dbEval.compareScore(bestScore, tempScore)) {
				bestScore = tempScore;
				bestCentroids = tempCentroids;
			}
		}
		return bestCentroids;

	}

	protected double intraCluster(Dataset[] datas, DistanceMeasure dm) {
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

	protected double interCluster(Dataset[] datas, DistanceMeasure dm) {
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