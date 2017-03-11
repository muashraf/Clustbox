package com.clustbox.clustering;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.IOException;
import java.util.Random;


import java.io.InputStream;
import java.io.FileInputStream;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.IterativeKMeans;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
import net.sf.javaml.clustering.evaluation.CIndex;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
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
import net.sf.javaml.tools.data.ARFFHandler;
import net.sf.javaml.tools.data.StreamHandler;
import net.sf.javaml.tools.weka.FromWekaUtils;
import net.sf.javaml.clustering.evaluation.AICScore;
//import net.sf.javaml.utils.ArrayUtils;

class bestCentroidResult {
	Instance[] bestCent
}

public class Cluster {
	static Dataset data = null;
	static DistanceMeasure dm;	
	static HashMap<String,String> map;
	static CIndex CIdx;
	static ClusterEvaluationWithNaturalFitness dbEval = new DaviesBouldinScore();
	
//	public enum Algo{
//		SimpleKMeans, IterativeKMeans, SimpleKMedoids, IterativeKMedoids
//	}

	public static void main(String[] args) throws IOException {
		
		Clusterer km;
		/* Load the config file */
		map = getProperty();
		
		/* Load a dataset */
		data = FileHandler.loadDataset(new File(map.get("dataFile")), 4, ",");

		//data = ARFFHandler.loadARFF( new File(map.get("dataFile")) );
		
		/* Set the distance measure */
		if(map.get("similarityMeasure").equals("CosineSimilarity"))
			dm = new CosineSimilarity();
		else if(map.get("similarityMeasure").equals("JaccardIndexSimilarity"))
			dm = new JaccardIndexSimilarity();
		else if(map.get("similarityMeasure").equals("ManhattanDistance"))
			dm = new ManhattanDistance();	
		else if(map.get("similarityMeasure").equals("MinkowskiDistance"))
			dm = new MinkowskiDistance();
		else if(map.get("similarityMeasure").equals("NormalizedEuclideanDistance"))
			dm = new NormalizedEuclideanDistance(data);						
		else if(map.get("similarityMeasure").equals("PearsonCorrelationCoefficient"))
			dm = new PearsonCorrelationCoefficient();				
		else 
			dm = new EuclideanDistance();
		
		CIdx = new CIndex(dm);
		
		CreateOuputDir("Output");
		File sFile = new File("Output/sFile.csv");
		FileWriter fWrite = new FileWriter(sFile);
		/* Run Simple KMeans or KMedoids with input K or centroids and return */
		if(map.containsKey("SimpleKMeans") || map.containsKey("SimpleKMedoids"))
		{
			int k;
			Instance[] centroids = null;
			if(map.containsKey("noOfClusters")){
				k = Integer.parseInt(map.get("noOfClusters"));
			}
			else if(map.containsKey("centroidsSource")){
				Dataset initCentroidsDS = FileHandler.loadDataset(new File(map.get("centroidsSource")), 4, ",");
				ArrayList<Instance> initCentroids = new ArrayList<Instance>();
				for(Instance ins: initCentroidsDS)
				{
					initCentroids.add(ins);
				}
				centroids = initCentroids.toArray(new Instance[0]);
				k = initCentroids.size();
			}
			else{
				System.out.println("Error: Please provide either K or initial centroids for Simle KMeans/KMedoids run");
				return;
			}
			
			/* Run the KMeans or KMedoids Algo */
			if(map.containsKey("SimpleKMedoids")){
				km = new CBKMedoids(k, 100, dm, centroids);
			}
			else {
				km = new CBKMeans(k, 100, dm, centroids);
			}
			
			Dataset[] clusters = km.cluster(data);
			int cnt=0;
			for(Dataset clust: clusters)
			{
				cnt++;
				FileHandler.exportDataset(clust,new File("Output/Cluster-" + cnt + ".data"),false, ",");
				System.out.println("Dumped cluster data to Output/Cluster-" + cnt + ".data");
			}
			return;			
		}
		
		/* Run the Iterative KMeans/KMedoids algorithms*/
		else if (map.containsKey("IterativeKMeans") || map.containsKey("IterativeKMedoids")){
			int k;
			/* If K is given only run best Centroids assessment */
			if(map.containsKey("noOfClusters")){
				k = Integer.parseInt(map.get("noOfClusters"));
				Instance[] bestCentroids = bestCentroids(k);
				return;
			}
			else {
				int KMIN = 2;
				int KMAX = data.size()/2;
				ClusterEvaluation tempEval = new AICScore();
				IterativeKMeans ikm = new IterativeKMeans(2, 5, tempEval);
				Dataset[] iClusters = ikm.cluster(data);
				int cnt = 0;
				for(Dataset clust: iClusters)
				{
					cnt++;
					FileHandler.exportDataset(clust,new File("Output/ClusterT-" + cnt + ".data"),false, ",");
				}
				System.out.println("IKMeans k = " + iClusters.length);
				
				double bestScore = -1; //Initialize of least possible score
				Dataset[] bestClusters = null;
				for(k=KMIN; k<KMAX; k++){
					Instance[] bestCentroids = bestCentroids(k);
					if(map.containsKey("IterativeKMedoids")){
						km = new CBKMedoids(k, 100, dm, bestCentroids);
					}
					else {
						km = new CBKMeans(k, 100, dm, bestCentroids);
					}
					Dataset[] tempClusters = km.cluster(data);
					//double tempScore = CIdx.score(tempClusters);
					double temp = intraCluster(tempClusters, dm);
					double tempScore = getAvgSilhouetteValues(tempClusters, dm);//tempEval.score(tempClusters);//getAvgSilhouetteValues(tempClusters, dm);
					/* Save the first run output as best data */
					if(k == KMIN){
						bestScore = tempScore;
						bestClusters = tempClusters;
					}
					System.out.println("Score with k=" + k + " is " + tempScore);
					fWrite.append(String.valueOf(tempScore));
					if(k<KMAX-1)
						fWrite.append("\n");
					//if( tempEval.compareScore(bestScore, tempScore)) {
					if(tempScore > bestScore){
						bestScore = tempScore;
						bestClusters = tempClusters;
					}
				}
				fWrite.flush();
				fWrite.close();
				System.out.println("Best Score is:" + bestScore);
				cnt=0;
				for(Dataset clust: bestClusters)
				{
					cnt++;
					FileHandler.exportDataset(clust,new File("Output/Cluster-" + cnt + ".data"),false, ",");
					System.out.println("Dumped cluster data to Output/Cluster-" + cnt + ".data");
				}
			}
		}
	
	}
	
	protected static void CreateOuputDir(String dirStr){
		File dir = new File(dirStr);
		if(dir.exists()){
			String[]entries = dir.list();
			for(String s: entries){
				File currentFile = new File(dir.getPath(),s);
				currentFile.delete();
			}
		}
		else {
			dir.mkdir();
		}
	}
	
	protected static Instance[] bestCentroids(int k){
		Instance[] bestCentroids = null;
		double bestScore = 0;
		Dataset[] bestClusters;
		for(int i=0; i<2; i++){
			Random rg = new Random(System.currentTimeMillis());
			Dataset Centroids = DatasetTools.bootstrap(data, k, rg);
			Clusterer km;
			ArrayList<Instance> initCentroids = new ArrayList<Instance>();
			for(Instance ins: Centroids)
			{
				initCentroids.add(ins);
			}
			Instance[] tempCentroids = initCentroids.toArray(new Instance[0]);
			km = new CBKMeans(k, 100, dm, bestCentroids);
			Dataset[] tempClusters = km.cluster(data);
			double tempScore = getAvgSilhouetteValues(tempClusters, dm);
			//System.out.println("bestC: " + tempScore);
			if(tempScore > bestScore) {
				bestScore = tempScore;
				bestCentroids = tempCentroids;
			}
		}
		return bestCentroids;
		
	}

	protected static double getAvgSilhouetteValues(Dataset[] datas, DistanceMeasure dm) {
		ArrayList<Double> sillhouettes = new ArrayList<Double>();
		Double s = 0.0;
		for (int i = 0; i < datas.length; i++) {
			for(int j = 0; j < datas[i].size(); j++) {
				Instance x = datas[i].instance(j);
				double Dw = 0, avgDw = 0, De = 0, avgDe = 0, nearDe = Double.MAX_VALUE;
				for(int k=0; k < datas[i].size();k++) {
//					if(k == j)
//						continue;
					Instance y = datas[i].instance(k);
					if(!y.equals(x))
						Dw += dm.measure(x, datas[i].instance(k)); 
				}
				avgDw = Dw / (datas[i].size() - 1);
				
												
				for(int l = 0; l < datas.length; l++){ 
					if( l == i)
						continue;
					De = 0;
					for(int m=0; m < datas[l].size(); m++){
						Instance y = datas[l].instance(m);
//						if(datas[l].contains(x))
//							continue;
						De += dm.measure(x, datas[l].instance(m));
					}
					avgDe = De / datas[l].size();
					if(avgDe <  nearDe)
						nearDe = avgDe;
				}
				
				if(nearDe < avgDw)
					s = ( nearDe - avgDw ) / avgDw;
				else
					s = ( nearDe - avgDw ) / nearDe;
				
				if (Double.isNaN(s))
					s = 0.0;
				
				sillhouettes.add(s);
				
			}
		}
		
		return calculateAverage(sillhouettes);

		
		
	}
	
	protected static double calculateAverage(ArrayList<Double> Sills) {
		Double sum = 0.0;
		Double avg = 0.0;
		if(!Sills.isEmpty()) {
			for (Double s : Sills) {
				sum += s;
			}
			avg =  sum / Sills.size();
		}
		return avg.doubleValue();
	}
	
//	protected static double getAvgSilhouetteValue(Dataset[] datas, DistanceMeasure dm) {
//	
//		double silhouette = 0;
//		for (int i = 0; i < datas.length; i++) {
//			double a = 0;
//			double b = 0;
//			for(int j = 0; j < datas[i].size(); j++) {
//				
//				double dw = 0, fw = 0;
//				Instance x = datas[i].instance(j);
//				for (int k = 0; k < datas[i].size(); k++) {
//					if(k == j)
//						continue;
//					Instance y = datas[i].instance(k);
//					double distance = dm.measure(x, y);
//					dw += distance;
//					fw++;
//				}
//				a += dw/fw;
//				
//				dw = 0;
//				fw = 0;
//				
//				for(int k = 0; k < datas.length; k++){
//					for(int l = 0; l < datas[k].size(); l++){
//						Instance y = datas[k].instance(l);
//						if(!datas[i].contains(y)){
//							double distance = dm.measure(x, y);
//							dw += distance;
//							fw++;
//							
//						}
//					}
//				}
//				
//				double newb = dw/fw;
//				if(j == 0) b = newb;
//				if(newb<b)
//					b = newb;
//			}
//			if(a<b)
//				silhouette = (b-a) / b;
//			else
//				silhouette = (b-a) / a;
//			
//			
//			silhouette = silhouette/2;
//		}
//		
//		return silhouette;
//		
//	}
//	
	
	
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
	
	protected static HashMap<String,String> getProperty()
    {
        Properties prop = new Properties();
        HashMap<String,String>map = new HashMap<String,String>();
        try
        {
            FileInputStream inputStream = new FileInputStream(new File("data/test.prop"));
            prop.load(inputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Some issue finding or loading file....!!! " + e.getMessage());

        }
        for(String key : prop.stringPropertyNames()) {
        	map.put(key, prop.getProperty(key)); 
        }
        return map;
    }
	
}