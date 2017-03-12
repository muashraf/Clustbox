package com.clustbox.clustering;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.io.IOException;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.IterativeKMeans;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.clustering.evaluation.SumOfCentroidSimilarities;
import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
import net.sf.javaml.clustering.evaluation.CIndex;
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
import net.sf.javaml.tools.data.ARFFHandler;
import net.sf.javaml.tools.data.StreamHandler;
import net.sf.javaml.tools.weka.FromWekaUtils;
import net.sf.javaml.clustering.evaluation.AICScore;
import net.sf.javaml.clustering.evaluation.BICScore;
//import net.sf.javaml.utils.ArrayUtils;

public class Cluster {
	public Dataset data = null;
	public DistanceMeasure dm;
	public HashMap<String, String> map;
	public List evalMetrics;
	public CIndex CIdx;
	public ClusterEvaluationWithNaturalFitness dbEval = new DaviesBouldinScore();
	public Display display;
	public Shell shlClustbox;
	public OutputStream out;
	
	private static final int bestC_THREADS = 1000;
	

	// public enum Algo{
	// SimpleKMeans, IterativeKMeans, SimpleKMedoids, IterativeKMedoids
	// }
	public Cluster() {
	}

	public void runClustering(HashMap formElements, Shell parent) throws IOException {

		display = Display.getDefault();
		shlClustbox = new Shell(parent);
		shlClustbox.setMinimumSize(new Point(800, 600));
		shlClustbox.setSize(450, 300);
		shlClustbox.setText("EXECUTE CLUSTBOX");
		shlClustbox.setLayout(new FillLayout());
		final Text text = new Text(shlClustbox, SWT.READ_ONLY | SWT.MULTI);
		shlClustbox.open();
		out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				text.append(Character.toString((char) b));
			}
		};
		System.setOut(new PrintStream(out));

		Clusterer km;
		evalMetrics = getEvalMetrics(formElements);
		dm = getSimilarityMeasure(formElements.get("similarityMeasure"));

		/* Load a dataset */
		data = FileHandler.loadDataset(new File((String) formElements.get("dataFile")), 4, ",");

		CIdx = new CIndex(dm);
		//Create swt output window
		CreateOuputDir("Output");
		File sFile = new File("Output/sFile.csv");
		FileWriter fWrite = new FileWriter(sFile);
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
			if (formElements.containsKey("SimpleKMedoids")) {
				km = new CBKMedoids(k, 100, dm, centroids);
			} else {
				km = new CBKMeans(k, 100, dm, centroids);
			}

			Dataset[] clusters = km.cluster(data);
			System.out.println("\nEvaluation Scores: \n");
			HashMap<String, Double> score = getEvalScores(formElements, clusters);
			for (Entry<String, Double> entry : score.entrySet()) {
				System.out.println(entry.getKey() + " = " + entry.getValue());
			}

			int cnt = 0;
			System.out.println("\nClustered Data Output Files: \n");
			for (Dataset clust : clusters) {
				cnt++;
				FileHandler.exportDataset(clust, new File("Output/Cluster-" + cnt + ".data"), false, ",");
				System.out.println("Output/Cluster-" + cnt + ".data");
			}

			while (!shlClustbox.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			shlClustbox.dispose();
			//display.dispose();

			return;
		}

		/* Run the Iterative KMeans/KMedoids algorithms */
		else if (formElements.containsKey("iterativeKMeans") || formElements.containsKey("iterativeKMedoids"))

		{
			int k;
			/* If K is given only run best Centroids assessment */
			if (formElements.containsKey("noOfClusters")) {
				k = Integer.parseInt((String) formElements.get("noOfClusters"));
				//Instance[] bestCentroids = bestCentroids(k);
				bestCentroids(k);
				
				System.out.println("\nClustered Data Output Files: \n");
				int cnt = 0;
				for (Dataset clust : bestResult.bestClusters) {
					cnt++;
					FileHandler.exportDataset(clust, new File("Output/Cluster-" + cnt + ".data"), false, ",");
					System.out.println("Dumped cluster data to Output/Cluster-" + cnt + ".data");
				}
				
				while (!shlClustbox.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
				shlClustbox.dispose();
				
				return;
				
			} else {
				int KMIN = 2;
				int KMAX = 30;//data.size() / 3;

				double bestScore = -1; // Initialize of least possible score
				Dataset[] bestClusters = null;
				for (k = KMIN; k < KMAX; k++) {
					bestCentroids(k);
//					Instance[] bestCentroids = bestCentroids(k);
//					if (formElements.containsKey("iterativeKMedoids")) {
//						km = new CBKMedoids(k, 100, dm, bestCentroids);
//					} else {
//						km = new CBKMeans(k, 100, dm, bestCentroids);
//					}
//					Dataset[] tempClusters = km.cluster(data);
//					// double tempScore = CIdx.score(tempClusters);
//					double temp = intraCluster(tempClusters, dm);
//					double tempScore = getAvgSilhouetteValues(tempClusters, dm);// tempEval.score(tempClusters);//getAvgSilhouetteValues(tempClusters,
//																				// dm);
//					/* Save the first run output as best data */
//					if (k == KMIN) {
//						bestScore = tempScore;
//						bestClusters = tempClusters;
//					}
//					System.out.println("Score with k=" + k + " is " + tempScore);
//					fWrite.append(String.valueOf(tempScore));
//					if (k < KMAX - 1)
//						fWrite.append("\n");
//					// if( tempEval.compareScore(bestScore, tempScore)) {
//					if (tempScore > bestScore) {
//						bestScore = tempScore;
//						bestClusters = tempClusters;
//					}
				}
				
				fWrite.flush();
				fWrite.close();
				System.out.println("Best Silhouette Score is: " + bestResult.bestScore + " for K = " + bestResult.bestK);
				System.out.println("\nClustered Data Output Files: \n");
				int cnt = 0;
				for (Dataset clust : bestResult.bestClusters) {
					cnt++;
					FileHandler.exportDataset(clust, new File("Output/Cluster-" + cnt + ".data"), false, ",");
					System.out.println("Dumped cluster data to Output/Cluster-" + cnt + ".data");
				}
				while (!shlClustbox.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
				shlClustbox.dispose();
			}
		}

	}

	private HashMap<String, Double> getEvalScores(HashMap<String, String> formElements, Dataset[] clustering) {
		HashMap<String, Double> score = new HashMap<String, Double>();
		List<String> metrics = getEvalMetrics(formElements);
		for (String eval : metrics) {
			if (eval.equals("sse")) {
				ClusterEvaluation sse = new SumOfSquaredErrors();
				score.put("Sum Of Squared Errors", sse.score(clustering));
			} else if (eval.equals("scs")) {
				ClusterEvaluation scs = new SumOfCentroidSimilarities();
				score.put("Sum Of Centroid Similarities", scs.score(clustering));
			} else if (eval.equals("aic")) {
				ClusterEvaluation aic = new AICScore();
				score.put("AIC Score", aic.score(clustering));
			} else if (eval.equals("bic")) {
				ClusterEvaluation bic = new BICScore();
				score.put("BIC Score", bic.score(clustering));
			} else if (eval.equals("cindex")) {
				ClusterEvaluation cindex = new CIndex(dm);
				score.put("C Index", cindex.score(clustering));
			} else if (eval.equals("dbIndex")) {
				score.put("Davies Bouldin Score", dbEval.score(clustering));
			} else if (eval.equals("sc")) {
				score.put("Silhouette Coefficient", getAvgSilhouetteValues(clustering, dm));
			}
		}
		return score;
	}

	private List<String> getEvalMetrics(HashMap<String, String> formElements) {
		// TODO Auto-generated method stub
		List<String> eval = new ArrayList<String>();
		for (Entry<String, String> elements : formElements.entrySet()) {
			if (elements.getKey().equals("sse")) {
				eval.add("sse");
			} else if (elements.getKey().equals("scs")) {
				eval.add("scs");
			} else if (elements.getKey().equals("aic")) {
				eval.add("aic");
			} else if (elements.getKey().equals("bic")) {
				eval.add("bic");
			} else if (elements.getKey().equals("cindex")) {
				eval.add("cindex");
			} else if (elements.getKey().equals("dbIndex")) {
				eval.add("dbIndex");
			} else if (elements.getKey().equals("sc")) {
				eval.add("sc");
			}
		}
		return eval;
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

	protected void CreateOuputDir(String dirStr) {
		File dir = new File(dirStr);
		if (dir.exists()) {
			String[] entries = dir.list();
			for (String s : entries) {
				File currentFile = new File(dir.getPath(), s);
				currentFile.delete();
			}
		} else {
			dir.mkdir();
		}
	}

	protected void bestCentroids(int k) {
		System.out.println("Creating threads for best Centroids evaluation for K = " + k);
		ExecutorService executor = Executors.newFixedThreadPool(bestC_THREADS);
		for(int i = 0; i < 5; i++){
			Runnable worker = new bestCentroidsC(k);
			executor.execute(worker);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
			 
		}
		//System.out.println("Best Silheoutte score for K = " + k + " is: " + bestResult.bestScore);
		
//		Instance[] bestCentroids = null;
//		double bestScore = 0;
//		Dataset[] bestClusters;
//		for (int i = 0; i < 100; i++) {
//			Random rg = new Random(System.currentTimeMillis());
//			Dataset Centroids = DatasetTools.bootstrap(data, k, rg);
//			Clusterer km;
//			ArrayList<Instance> initCentroids = new ArrayList<Instance>();
//			for (Instance ins : Centroids) {
//				initCentroids.add(ins);
//			}
//			Instance[] tempCentroids = initCentroids.toArray(new Instance[0]);
//			km = new CBKMeans(k, 100, dm, tempCentroids);
//			Dataset[] tempClusters = km.cluster(data);
//			double tempScore = getAvgSilhouetteValues(tempClusters, dm);
//			// System.out.println("bestC: " + tempScore);
//			if (tempScore > bestScore) {
//				bestScore = tempScore;
//				bestCentroids = tempCentroids;
//			}
//		}
//		return bestCentroids;

	}

	protected double getAvgSilhouetteValues(Dataset[] datas, DistanceMeasure dm) {
		ArrayList<Double> sillhouettes = new ArrayList<Double>();
		Double s = 0.0;
		for (int i = 0; i < datas.length; i++) {
			for (int j = 0; j < datas[i].size(); j++) {
				Instance x = datas[i].instance(j);
				double Dw = 0, avgDw = 0, De = 0, avgDe = 0, nearDe = Double.MAX_VALUE;
				for (int k = 0; k < datas[i].size(); k++) {
					// if(k == j)
					// continue;
					Instance y = datas[i].instance(k);
					if (!y.equals(x))
						Dw += dm.measure(x, datas[i].instance(k));
				}
				avgDw = Dw / (datas[i].size() - 1);

				for (int l = 0; l < datas.length; l++) {
					if (l == i)
						continue;
					De = 0;
					for (int m = 0; m < datas[l].size(); m++) {
						Instance y = datas[l].instance(m);
						// if(datas[l].contains(x))
						// continue;
						De += dm.measure(x, datas[l].instance(m));
					}
					avgDe = De / datas[l].size();
					if (avgDe < nearDe)
						nearDe = avgDe;
				}

				if (nearDe < avgDw)
					s = (nearDe - avgDw) / avgDw;
				else
					s = (nearDe - avgDw) / nearDe;

				if (Double.isNaN(s))
					s = 0.0;

				sillhouettes.add(s);

			}
		}

		return calculateAverage(sillhouettes);

	}

	protected double calculateAverage(ArrayList<Double> Sills) {
		Double sum = 0.0;
		Double avg = 0.0;
		if (!Sills.isEmpty()) {
			for (Double s : Sills) {
				sum += s;
			}
			avg = sum / Sills.size();
		}
		return avg.doubleValue();
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

	protected HashMap<String, String> getProperty() {
		Properties prop = new Properties();
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			FileInputStream inputStream = new FileInputStream(new File("data/test.prop"));
			prop.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Some issue finding or loading file....!!! " + e.getMessage());

		}
		for (String key : prop.stringPropertyNames()) {
			map.put(key, prop.getProperty(key));
		}
		return map;
	}
	
	
	public static class bestResult {
		private static int bestK;
		private static volatile double bestScore = -1;
		private static Instance[] bestCentroids;
		private static Dataset[] bestClusters;
		private static final Object mutex = new Object();
		
		
		public static void CompareAndUpdate(int k, double tempScore, Instance[] tempCentroids, Dataset[] tempClusters){
			synchronized (mutex){
				if(tempScore > bestScore){
					//System.out.println("Updating Value");
					bestScore = tempScore;
					bestK = k;
					bestCentroids = tempCentroids.clone();
					bestClusters = tempClusters.clone();	
				}
			}
		
		}
		
	
	}
	
	
	public class bestCentroidsC implements Runnable {
		private final int k;
 
		bestCentroidsC(int k) {
			this.k = k;
		}
 
		@Override
		public void run() {
 
			Random rg = new Random(System.currentTimeMillis());
			Dataset Centroids = DatasetTools.bootstrap(data, k, rg);
			Clusterer km;
			ArrayList<Instance> initCentroids = new ArrayList<Instance>();
			for (Instance ins : Centroids) {
				initCentroids.add(ins);
			}
			Instance[] tempCentroids = initCentroids.toArray(new Instance[0]);
			km = new CBKMeans(k, 100, dm, tempCentroids);
			Dataset[] tempClusters = km.cluster(data);
			double tempScore = getAvgSilhouetteValues(tempClusters, dm);
			//System.out.println("bestC: " + tempScore);
//			if (tempScore > bestScore) {
//				bestScore = tempScore;
//				bestCentroids = tempCentroids;
//			}
			bestResult.CompareAndUpdate(k, tempScore, tempCentroids, tempClusters);
			
		}
	}
	
//	public static void println(String x) {
//		final Object mutex = new Object();
//	    synchronized (mutex) {
//	        System.out.println(x);
//	    }
//	}
	
	
	
	

}