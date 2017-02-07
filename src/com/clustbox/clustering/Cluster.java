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
import net.sf.javaml.tools.weka.*;

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
		
		/* params to be read from config file or gui input*/
		int KMIN = 2;
		int KMAX = 50;
		DistanceMeasure dm = new EuclideanDistance();
		ClusterEvaluation sse = new SumOfSquaredErrors();
		ClusterEvaluationWithNaturalFitness dbEval = new DaviesBouldinScore();
		
		Clusterer km = new CBKMeans(KMIN, 100, dm);
		Dataset[] bestClustersMean = km.cluster(data);
		double bestScoreMean = dbEval.score(bestClustersMean);
		
		Clusterer kmed = new CBKMedoids(KMIN,100,dm);
		Dataset[] bestClustersMedoid = km.cluster(data);
		double bestScoreMedoid = dbEval.score(bestClustersMedoid);
		
		for(int k=KMIN+1; k < KMAX ; k++){
	
			//Instance[] initCentroids = new Instance[k];
			
			//for(int j=5; j<50 ; j=j+5){

			//	for(int i=0; i<k ; i++)
			//	{
			//		initCentroids [i] = data.get(i*j);
			//	}
			
			
			
			System.out.println("*****************Iteration with k: " + k + " ************************\n");
			System.out.println("Running K-Means........\n");

			km = new CBKMeans(k, 100, dm);//, initCentroids);

			Dataset[] clustersMean = km.cluster(data);
			
			double sseScore = sse.score(clustersMean);
			double intraClusterScore = intraCluster(clustersMean, dm);
			double interClusterScore = interCluster(clustersMean, dm);
			//double silH = getAvgSilhouetteValue(clusters, dm);

			double tmpScoreMean = dbEval.score(clustersMean);
			
			System.out.println("Sum of squared errors: " + sseScore + "\n intraCluster Score " + intraClusterScore
					+ "\n interCluster Score: " + interClusterScore + "\n  Davies-Bouldin index = " + tmpScoreMean);//Silhuette = " + silH + "\n");
			
			if(dbEval.compareScore(bestScoreMean, tmpScoreMean)) {
				bestScoreMean = tmpScoreMean;
				bestClustersMean = clustersMean;
			}
			
			
			
			System.out.println("Running K-Medoids........\n");
			kmed = new CBKMedoids(k, 100, dm);//, initCentroids);
			Dataset[] ClustersMedoid = kmed.cluster(data);

			ClusterEvaluation medSSE = new SumOfSquaredErrors();
			double medsseScore = medSSE.score(ClustersMedoid);
			double medintraClusterScore = intraCluster(ClustersMedoid, dm);
			double medinterClusterScore = interCluster(ClustersMedoid, dm);
			//double silHmed = getAvgSilhouetteValue(clusters, dm);
			
			double tmpScoreMedoid = dbEval.score(ClustersMedoid);
			System.out.println("Sum of squared errors: " + medsseScore + "\n intraCluster Score: " + medintraClusterScore
					+ "\n interCluster Score: " + medinterClusterScore + "\n  Davies-Bouldin index = " + tmpScoreMedoid);//Silhuette = " + silHmed + "\n");
	
			if(dbEval.compareScore(bestScoreMedoid, tmpScoreMedoid)) {
				bestScoreMedoid = tmpScoreMedoid;
				bestClustersMedoid = ClustersMedoid;
			}
			
		}
		System.out.println("\n\n Best K for k-means is : " + bestClustersMean.length + " with best Davies-Bouldin index score " + bestScoreMean );
		System.out.println("\n\n Best K for k-medoids is : " + bestClustersMedoid.length + " with best Davies-Bouldin index score " + bestScoreMedoid );
		
	}

	/*
	protected static double getAvgSilhouetteValue(Dataset[] datas, DistanceMeasure dm) {
		double silhouette = 0;
		for (int i = 0; i < datas.length; i++) {
			double a = 0;
			double b = 0;
			for(int j = 0; j < datas[i].size(); j++) {
				
				double dw = 0, fw = 0;
				Instance x = datas[i].instance(j);
				for (int k = 0; k < datas[i].size(); k++) {
					if(k == j)
						continue;
					Instance y = datas[i].instance(k);
					double distance = dm.measure(x, y);
					dw += distance;
					fw++;
				}
				a += dw/fw;
				
				dw = 0;
				fw = 0;
				
				for(int k = 0; k < datas.length; k++){
					for(int l = 0; l < datas[k].size(); l++){
						Instance y = datas[k].instance(l);
						if(!datas[i].contains(y)){
							double distance = dm.measure(x, y);
							dw += distance;
							fw++;
							
						}
					}
				}
				
				double newb = dw/fw;
				if(j == 0) b = newb;
				if(newb<b)
					b = newb;
			}
			if(a<b)
				silhouette = (b-a) / b;
			else
				silhouette = (b-a) / a;
			
			
			silhouette = silhouette/2;
		}
		
		return silhouette;
	}
	
	*/
	
	protected static double intraCluster(Dataset[] datas, DistanceMeasure dm) {
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

	protected static double interCluster(Dataset[] datas, DistanceMeasure dm) {
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