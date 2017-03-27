package com.clustbox.clustering;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.wb.swt.SWTResourceManager;

public class CBMain {

	public HashMap<String, String> formElements = new HashMap<String, String>();

	private Text dataFile, noOfClusters, centroidsSource, kMin, kMax;

	private Button btnRun, btnCentroidsSource, simpleKMeans, iterativeKMeans, sameSizedKMeans, simpleKMedoids,
			iterativeKMedoids, sc, sse, scs, aic, bic, cindex, dbIndex;

	private Combo similarityMeasure, iterativeOpt;

	private Group grpAdditionalMetrices;

	private Label lblKmin, lblKmax;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		Shell shlClustbox = new Shell();
		shlClustbox.setMinimumSize(new Point(800, 600));
		shlClustbox.setSize(450, 300);
		shlClustbox.setText("CLUSTBOX");
		CBMain cb = new CBMain();
		cb.dataFile(shlClustbox);
		cb.selSimilarity(shlClustbox);
		cb.clustAlgo(shlClustbox);
		cb.confAlgo(shlClustbox);
		cb.evalMetrics(shlClustbox);
		cb.exitWindow(shlClustbox);
		cb.runCB(shlClustbox);
		cb.resetWindow(shlClustbox);
		CLabel lblClustbox = new CLabel(shlClustbox, SWT.NONE);
		lblClustbox.setBounds(10, 10, 760, 20);
		lblClustbox.setText(
				"CLUSTBOX: A black box of center-based clustering algorithms. To run the utility please enter the below form.");
		CLabel lblImpNote = new CLabel(shlClustbox, SWT.NONE);
		lblImpNote.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		lblImpNote.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		lblImpNote.setBounds(10, 30, 760, 15);
		lblImpNote.setText("Important: note that the data source file and algorithm selection is mandatory. ");

		shlClustbox.open();
		shlClustbox.layout();
		while (!shlClustbox.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void confAlgo(Shell shlClustbox) {
		Group grpConfigurations = new Group(shlClustbox, SWT.NONE);
		grpConfigurations.setText("Configurations");
		grpConfigurations.setBounds(10, 300, 500, 150);
		ModifyListener listener = runBtnStatus();

		noOfClusters = new Text(grpConfigurations, SWT.BORDER);
		noOfClusters.setBounds(10, 100, 230, 25);
		noOfClusters.setEnabled(false);
		noOfClusters.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// ... Handling simple K-means and K-medoids ..//
				if ((simpleKMeans.getSelection() || simpleKMedoids.getSelection())
						&& noOfClusters.getText().length() > 0) {
					centroidsSource.setText("");
					centroidsSource.setEnabled(false);
					btnCentroidsSource.setEnabled(false);
				} else if ((simpleKMeans.getSelection() || simpleKMedoids.getSelection())
						&& noOfClusters.getText().length() == 0) {
					centroidsSource.setEnabled(true);
					btnCentroidsSource.setEnabled(true);
				}
			}
		});
		validateClusterText(noOfClusters);
		noOfClusters.addModifyListener(listener);
		noOfClusters.setToolTipText("Number of clusters to produce.");

		centroidsSource = new Text(grpConfigurations, SWT.BORDER);
		centroidsSource.setBounds(250, 100, 210, 25);
		centroidsSource.setEnabled(false);
		centroidsSource.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {

				// ... Handling simple K-means and K-medoids ..//
				if ((simpleKMeans.getSelection() || simpleKMedoids.getSelection())
						&& centroidsSource.getText().length() > 0) {
					noOfClusters.setText("");
					noOfClusters.setEnabled(false);
				} else if ((simpleKMeans.getSelection() || simpleKMedoids.getSelection())
						&& centroidsSource.getText().length() == 0) {
					noOfClusters.setEnabled(true);
				}
			}
		});
		centroidsSource.addModifyListener(listener);
		centroidsSource.setToolTipText("Browse source file for centroids instances.");

		btnCentroidsSource = new Button(grpConfigurations, SWT.NONE);
		btnCentroidsSource.setBounds(465, 100, 25, 25);
		btnCentroidsSource.setText("....");
		btnCentroidsSource.setEnabled(false);
		chooseFile(shlClustbox, btnCentroidsSource, centroidsSource);

		Label lblNoOfClusters = new Label(grpConfigurations, SWT.NONE);
		lblNoOfClusters.setBounds(10, 85, 120, 15);
		lblNoOfClusters.setText("Number Of Clusters:");

		Label lblCentroidsSource = new Label(grpConfigurations, SWT.NONE);
		lblCentroidsSource.setBounds(250, 85, 120, 15);
		lblCentroidsSource.setText("Centroids Source:");

		iterativeOpt = new Combo(grpConfigurations, SWT.DROP_DOWN | SWT.READ_ONLY);
		String[] iterativeItems = { "Best centroid evaluation", "Best centroid and K evaluation" };

		iterativeOpt.setItems(iterativeItems);
		iterativeOpt.select(0);
		iterativeOpt.setBounds(10, 40, 200, 25);
		iterativeOpt.setEnabled(false);
		iterativeOpt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (iterativeOpt.getSelectionIndex() != 0) {
					lblKmin.setVisible(true);
					kMin.setVisible(true);
					kMin.setText("");
					lblKmax.setVisible(true);
					kMax.setVisible(true);
					kMax.setText("");
					noOfClusters.setText("");
					noOfClusters.setEnabled(false);
				} else {
					lblKmin.setVisible(false);
					kMin.setVisible(false);
					kMin.setText("");
					lblKmax.setVisible(false);
					kMax.setVisible(false);
					kMax.setText("");
					noOfClusters.setText("");
					noOfClusters.setEnabled(true);
				}
			}
		});
		iterativeOpt.addModifyListener(listener);
		iterativeOpt.setToolTipText("Evaluation Criterion:-\nBest Centroid Evaluation:-\nFor this variation "
				+ "best initial starting centroids are evaluated K\n\nBest Centroid and K Evaluation:-\nFor "
				+ "this variation best K is evaluated along with best centroids\n\nThe algorithm runs "
				+ "iteratively for [KMIN......KMAX] If KMIN and KMAX are not given following default values "
				+ "will be selected KMIN=2 KMAX=Size of data/10\n\nNote:-\nWhen no change in the best score "
				+ "is observe for the recent 5 runs of algorithm with increasing K, the iteration stops.");

		lblKmin = new Label(grpConfigurations, SWT.NONE);
		lblKmin.setBounds(250, 40, 40, 15);
		lblKmin.setText("K-MIN:");
		lblKmin.setVisible(false);

		kMin = new Text(grpConfigurations, SWT.BORDER);
		kMin.setText("");
		kMin.setBounds(300, 40, 40, 25);
		kMin.setVisible(false);
		kMin.setToolTipText("Minimum number of times clustering should run.");
		validateClusterText(kMin);

		lblKmax = new Label(grpConfigurations, SWT.NONE);
		lblKmax.setBounds(370, 40, 40, 15);
		lblKmax.setText("K-MAX:");
		lblKmax.setVisible(false);

		kMax = new Text(grpConfigurations, SWT.BORDER);
		kMax.setText("");
		kMax.setBounds(420, 40, 40, 25);
		kMax.setVisible(false);
		kMax.setToolTipText("Maximum number of times clustering should run.");
		validateClusterText(kMax);

	}

	private void selSimilarity(Shell shlClustbox) {
		String[] similarityItems = { "     ---- Select Similarity Measure ----     ", "EuclideanDistance",
				"CosineSimilarity", "JaccardIndexSimilarity", "ManhattanDistance", "MinkowskiDistance",
				"NormalizedEuclideanDistance", "PearsonCorrelationCoefficient" };

		similarityMeasure = new Combo(shlClustbox, SWT.DROP_DOWN | SWT.READ_ONLY);
		similarityMeasure.setItems(similarityItems);
		similarityMeasure.select(0);
		similarityMeasure.setBounds(530, 90, 240, 25);
		similarityMeasure.setToolTipText("Euclidean Distance:-\nThe Euclidean distance or Euclidean metric "
				+ "is the 'ordinary' distance between two points in Euclidean space. Formula:- dist((x, y), "
				+ "(a, b)) = v(x - a)² + (y - b)².\n\nCosine Similarity:-\nCosine similarity is a measure of "
				+ "similarity between two non-zero vectors of an inner product space. Formula:- cos(d1, d2) = "
				+ "(d1 · d2) / ||d1|| ||d2||.\n\nJaccard Index Similarity:-\nThe Jaccard coefficient measures "
				+ "similarity between finite sample sets, and is defined as the size of the intersection divided "
				+ "by the size of the union of the sample sets.\n\nManhattan Distance:-\nThe distance between two "
				+ "points in a grid based on a strictly horizontal and/or vertical path.\n\nMinkowski Distance:-\n"
				+ "The Minkowski distance is a metric in a normed vector space which can be considered as a generalization "
				+ "of both the Euclidean distance and the Manhattan distance.\n\nNormalized Euclidean Distance:-\n"
				+ "Normalized Euclidean Distance[u,v] is equivalent to 1/2*Norm[(u-Mean[u])-(v-Mean[v])]^2/(Norm[u-Mean[u]]^2+Norm[v-Mean[v]]^2)."
				+ "\n\nPearson Correlation Coefficient:-\nPearson Correlation measures the similarity in shape between "
				+ "two profiles.\nFormula: d = 1 - r\nwhere  r = Z(x)·Z(y)/n.\n\n*Euclidean Distance selected by default");
	}

	private void clustAlgo(Shell shlClustbox) {
		Group grpClustAlgo = new Group(shlClustbox, SWT.NONE);
		grpClustAlgo.setText("Clustering Algorithms");
		grpClustAlgo.setBounds(10, 150, 500, 120);

		simpleKMeans = new Button(grpClustAlgo, SWT.RADIO);
		simpleKMeans.setBounds(10, 35, 120, 16);
		simpleKMeans.setText("Simple K-means");
		simpleKMeans.setToolTipText(
				"Basic K-means clustering aims to partition the given input data into k clusters, where either value of k  (number of clusters field) or the initial centroids source(centroids source field) is given.");
		selAlgo(simpleKMeans);

		iterativeKMeans = new Button(grpClustAlgo, SWT.RADIO);
		iterativeKMeans.setBounds(10, 70, 120, 16);
		iterativeKMeans.setText("Iterative K-means");
		iterativeKMeans.setToolTipText(
				"Consists of two variation of k-means clustering evaluation i.e, Best Centroids evalauation,in which, for a given K value, the best initial centroids are evaluated based on the Silhouette Coefficient as the evalauation parameter.\nAnd another one is Best Centroids along with best K evaluation, in which, including the best centroids, the best possible number of clusters is evaluated using the same Silhouette Coefficient as the evalauation parameter.");
		selAlgo(iterativeKMeans);

		sameSizedKMeans = new Button(grpClustAlgo, SWT.RADIO);
		sameSizedKMeans.setBounds(360, 35, 120, 16);
		sameSizedKMeans.setText("Same-sized K-means");
		sameSizedKMeans.setToolTipText("K-means variation that produces clusters of the same size.");
		selAlgo(sameSizedKMeans);

		simpleKMedoids = new Button(grpClustAlgo, SWT.RADIO);
		simpleKMedoids.setBounds(180, 35, 120, 16);
		simpleKMedoids.setText("Simple K-medoids");
		simpleKMedoids.setToolTipText(
				"Basic K-medoids Basic k-Medoids clustering aims to partition the given input data into k clusters, where either value of k  (number of clusters field) or the initial centroids source(centroids source field) is given.");
		selAlgo(simpleKMedoids);

		iterativeKMedoids = new Button(grpClustAlgo, SWT.RADIO);
		iterativeKMedoids.setBounds(180, 70, 120, 16);
		iterativeKMedoids.setText("Iterative K-medoids");
		iterativeKMedoids.setToolTipText(
				"Consists of two variation of k-Medoids clustering evaluation i.e, Best Centroids evalauation,in which, for a given K value, the best initial centroids are evaluated based on the Silhouette Coefficient as the evalauation parameter.\nAnd another one is Best Centroids along with best K evaluation, in which, including the best centroids, the best possible number of clusters is evaluated using the same Silhouette Coefficient as the evalauation parameter.");
		selAlgo(iterativeKMedoids);
	}

	private void selAlgo(Button radioBtn) {
		radioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Button button = (Button) e.widget;
				if (button.equals(simpleKMeans)) {
					noOfClusters.setText("");
					noOfClusters.setEnabled(true);
					centroidsSource.setText("");
					centroidsSource.setEnabled(true);
					btnCentroidsSource.setEnabled(true);
					similarityMeasure.setEnabled(true);
					iterativeOpt.setEnabled(false);
					iterativeOpt.select(0);
					lblKmin.setVisible(false);
					kMin.setText("");
					kMin.setVisible(false);
					lblKmax.setVisible(false);
					kMax.setText("");
					kMax.setVisible(false);
					int cnt = 0;
					for (Control child : grpAdditionalMetrices.getChildren()) {
						cnt++;
						if (cnt > 1) {
							child.setEnabled(true);
						}
					}
				} else if (button.equals(iterativeKMeans)) {
					noOfClusters.setText("");
					noOfClusters.setEnabled(true);
					centroidsSource.setText("");
					centroidsSource.setEnabled(false);
					btnCentroidsSource.setEnabled(false);
					similarityMeasure.setEnabled(true);
					iterativeOpt.setEnabled(true);
					iterativeOpt.select(0);
					lblKmin.setVisible(false);
					kMin.setText("");
					kMin.setVisible(false);
					lblKmax.setVisible(false);
					kMax.setText("");
					kMax.setVisible(false);
					int cnt = 0;
					for (Control child : grpAdditionalMetrices.getChildren()) {
						cnt++;
						if (cnt > 1) {
							child.setEnabled(true);
						}
					}
				} else if (button.equals(sameSizedKMeans)) {
					noOfClusters.setText("");
					noOfClusters.setEnabled(true);
					centroidsSource.setText("");
					centroidsSource.setEnabled(false);
					btnCentroidsSource.setEnabled(false);
					similarityMeasure.setEnabled(false);
					iterativeOpt.setEnabled(false);
					iterativeOpt.select(0);
					lblKmin.setVisible(false);
					kMin.setText("");
					kMin.setVisible(false);
					lblKmax.setVisible(false);
					kMax.setText("");
					kMax.setVisible(false);
					for (Control child : grpAdditionalMetrices.getChildren()) {
						child.setEnabled(false);
					}

				} else if (button.equals(simpleKMedoids)) {
					noOfClusters.setText("");
					noOfClusters.setEnabled(true);
					centroidsSource.setText("");
					centroidsSource.setEnabled(true);
					btnCentroidsSource.setEnabled(true);
					similarityMeasure.setEnabled(true);
					iterativeOpt.setEnabled(false);
					iterativeOpt.select(0);
					lblKmin.setVisible(false);
					kMin.setText("");
					kMin.setVisible(false);
					lblKmax.setVisible(false);
					kMax.setText("");
					kMax.setVisible(false);
					int cnt = 0;
					for (Control child : grpAdditionalMetrices.getChildren()) {
						cnt++;
						if (cnt > 1) {
							child.setEnabled(true);
						}
					}
				} else if (button.equals(iterativeKMedoids)) {
					noOfClusters.setText("");
					noOfClusters.setEnabled(true);
					centroidsSource.setText("");
					centroidsSource.setEnabled(false);
					btnCentroidsSource.setEnabled(false);
					similarityMeasure.setEnabled(true);
					iterativeOpt.setEnabled(true);
					iterativeOpt.select(0);
					lblKmin.setVisible(false);
					kMin.setText("");
					kMin.setVisible(false);
					lblKmax.setVisible(false);
					kMax.setText("");
					kMax.setVisible(false);
					int cnt = 0;
					for (Control child : grpAdditionalMetrices.getChildren()) {
						cnt++;
						if (cnt > 1) {
							child.setEnabled(true);
						}
					}
				}
				if ((button.equals(simpleKMeans) || button.equals(simpleKMedoids)) && dataFile.getText().length() > 0
						&& (noOfClusters.getText().length() > 0 || centroidsSource.getText().length() > 0)) {
					btnRun.setEnabled(true);
				} else if (button.equals(sameSizedKMeans) && dataFile.getText().length() > 0
						&& noOfClusters.getText().length() > 0) {
					btnRun.setEnabled(true);
				} else if ((button.equals(iterativeKMeans) || button.equals(iterativeKMedoids))
						&& dataFile.getText().length() > 0 && iterativeOpt.getSelectionIndex() == 0
						&& noOfClusters.getText().length() > 0) {
					btnRun.setEnabled(true);
				} else if ((button.equals(iterativeKMeans) || button.equals(iterativeKMedoids))
						&& dataFile.getText().length() > 0 && iterativeOpt.getSelectionIndex() == 1) {
					btnRun.setEnabled(true);
				} else {
					btnRun.setEnabled(false);
				}

			}
		});

	}

	private void validateClusterText(Text textField) {
		textField.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});
	}

	private void evalMetrics(Shell shlClustbox) {
		grpAdditionalMetrices = new Group(shlClustbox, SWT.NONE);
		grpAdditionalMetrices.setText("Evaluation Metrics");
		grpAdditionalMetrices.setBounds(530, 150, 240, 300);
		createMetrics(grpAdditionalMetrices);
	}

	private void createMetrics(Group grpAdditionalMetrices) {
		Composite c = new Composite(grpAdditionalMetrices, SWT.NONE);
		c.setLayoutData(new GridData());
		c.setLayout(new FillLayout());

		sc = new Button(grpAdditionalMetrices, SWT.CHECK);
		sc.setBounds(20, 35, 140, 16);
		sc.setText("Silhouette Coefficient");
		sc.setToolTipText(
				"The silhouette value is a measure of how similar an object is to its own cluster (cohesion) compared to other clusters (separation).\n\n*Silhouette Coefficient by default selected.");
		sc.setSelection(true);
		sc.setEnabled(false);
		sc.getParent().setToolTipText(sc.getToolTipText());

		sse = new Button(grpAdditionalMetrices, SWT.CHECK);
		sse.setBounds(20, 70, 140, 16);
		sse.setText("Sum Of Squared Errors");
		sse.setToolTipText(
				"Sum of Squared Errors is the sum of the squared differences between each observation and its group's mean.\n\n*Silhouette Coefficient by default selected.");

		scs = new Button(grpAdditionalMetrices, SWT.CHECK);
		scs.setBounds(20, 105, 170, 16);
		scs.setText("Sum Of Centroid Similarities");
		scs.setToolTipText(
				"Sum Of Centroid Similarities is equivalent to average similarity of all pairs of data points from different clusters.\n\n*Silhouette Coefficient by default selected.");

		aic = new Button(grpAdditionalMetrices, SWT.CHECK);
		aic.setBounds(20, 140, 93, 16);
		aic.setText("AIC");
		aic.setToolTipText(
				"Measure of the relative quality of models for a given set of data.\n\n*Silhouette Coefficient by default selected.");

		bic = new Button(grpAdditionalMetrices, SWT.CHECK);
		bic.setBounds(20, 175, 119, 16);
		bic.setText("BIC");
		bic.setToolTipText(
				"The criterion for model selection among a finite set of models; the model with the lowest BIC is preferred.\n\n*Silhouette Coefficient by default selected.");

		cindex = new Button(grpAdditionalMetrices, SWT.CHECK);
		cindex.setBounds(20, 210, 93, 16);
		cindex.setText("C-index");
		cindex.setToolTipText(
				"The C-index measure to what extent the clustering put together th N points that are the closest across the K clusters, it ranges from 0.5 to 1.\n\n*Silhouette Coefficient by default selected.");

		String html = "This is an internal evaluation scheme, where the validation of how well the clustering has been done is made using quantities and features inherent to the dataset.\n\n*Silhouette Coefficient by default selected.";
		dbIndex = new Button(grpAdditionalMetrices, SWT.CHECK);
		dbIndex.setBounds(20, 245, 170, 16);
		dbIndex.setText("Davies-Bouldin index");
		dbIndex.setToolTipText(html);

	}

	private void runCB(Shell shlClustbox) {
		btnRun = new Button(shlClustbox, SWT.NONE);
		btnRun.setBounds(600, 500, 75, 25);
		btnRun.setText("Run");
		submitForm(shlClustbox);
		btnRun.setEnabled(false);
	}

	private void submitForm(Shell shlClustbox) {
		btnRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (!formElements.isEmpty()) {
					formElements.clear();
				}

				formElements.put("dataFile", dataFile.getText());

				if (noOfClusters.getEnabled() && noOfClusters.getText().length() > 0) {
					formElements.put("noOfClusters", noOfClusters.getText());
				}

				if (kMin.getVisible() && kMin.getText().length() > 0) {
					formElements.put("kMin", kMin.getText());
				}

				if (kMax.getVisible() && kMax.getText().length() > 0) {
					formElements.put("kMax", kMax.getText());
				}

				if (centroidsSource.getEnabled() && centroidsSource.getText().length() > 0) {
					formElements.put("centroidsSource", centroidsSource.getText());
				}

				if (simpleKMeans.getSelection()) {
					formElements.put("simpleKMeans", simpleKMeans.getText());
				}

				if (iterativeKMeans.getSelection()) {
					formElements.put("iterativeKMeans", iterativeKMeans.getText());
				}

				if (sameSizedKMeans.getSelection()) {
					formElements.put("sameSizedKMeans", sameSizedKMeans.getText());
				}

				if (simpleKMedoids.getSelection()) {
					formElements.put("simpleKMedoids", simpleKMedoids.getText());
				}
				if (iterativeKMedoids.getSelection()) {
					formElements.put("iterativeKMedoids", iterativeKMedoids.getText());
				}

				if (sc.getSelection()) {
					formElements.put("sc", sc.getText());
				}

				if (sse.getSelection()) {
					formElements.put("sse", sse.getText());
				}

				if (scs.getSelection()) {
					formElements.put("scs", scs.getText());
				}

				if (aic.getSelection()) {
					formElements.put("aic", aic.getText());
				}

				if (bic.getSelection()) {
					formElements.put("bic", bic.getText());
				}

				if (cindex.getSelection()) {
					formElements.put("cindex", cindex.getText());
				}
				if (dbIndex.getSelection()) {
					formElements.put("dbIndex", dbIndex.getText());
				}

				if (similarityMeasure.getSelectionIndex() == 0) {
					formElements.put("similarityMeasure", similarityMeasure.getItem(1));
				} else
					formElements.put("similarityMeasure", similarityMeasure.getText());

				if (iterativeOpt.getEnabled())
					formElements.put("iterativeOpt", iterativeOpt.getText());

				// for (Entry<String, String> entry : formElements.entrySet()) {
				// System.out.println("Keys: " + entry.getKey() + " Values: " +
				// entry.getValue());
				// }

				CBCluster cl = new CBCluster();
				try {
					cl.runClustering(formElements, shlClustbox);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
	}

	private void resetWindow(Shell shlClustbox) {
		Button btnReset = new Button(shlClustbox, SWT.NONE);
		btnReset.setBounds(504, 500, 75, 25);
		btnReset.setText("Reset");
		clearForm(btnReset);
	}

	private void clearForm(Button btnReset) {
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dataFile.setText("");
				noOfClusters.setText("");
				noOfClusters.setEnabled(false);
				centroidsSource.setText("");
				centroidsSource.setEnabled(false);
				btnCentroidsSource.setEnabled(false);
				simpleKMeans.setSelection(false);
				iterativeKMeans.setSelection(false);
				sameSizedKMeans.setSelection(false);
				simpleKMedoids.setSelection(false);
				iterativeKMedoids.setSelection(false);
				sse.setSelection(false);
				scs.setSelection(false);
				aic.setSelection(false);
				bic.setSelection(false);
				cindex.setSelection(false);
				dbIndex.setSelection(false);
				similarityMeasure.select(0);
				iterativeOpt.setEnabled(false);
				iterativeOpt.select(0);
				lblKmin.setVisible(false);
				kMin.setText("");
				kMin.setVisible(false);
				lblKmax.setVisible(false);
				kMax.setText("");
				kMax.setVisible(false);
			}
		});
	}

	private void exitWindow(Shell shlClustbox) {
		Button btnExit = new Button(shlClustbox, SWT.NONE);
		btnExit.setBounds(695, 500, 75, 25);
		btnExit.setText("Exit");
		btnExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlClustbox.dispose();
			}
		});
	}

	private void dataFile(Shell shlClustbox) {
		Group grpSourceFile = new Group(shlClustbox, SWT.NONE);
		grpSourceFile.setText("Source File");
		grpSourceFile.setBounds(10, 60, 500, 70);

		dataFile = new Text(grpSourceFile, SWT.BORDER);
		dataFile.setBounds(100, 30, 270, 25);
		dataFile.setToolTipText("Browse main data source file.");
		ModifyListener listener = runBtnStatus();
		dataFile.addModifyListener(listener);

		Button btnChooseDataFile = new Button(grpSourceFile, SWT.NONE);
		btnChooseDataFile.setBounds(380, 30, 75, 25);
		btnChooseDataFile.setText("Choose File");
		chooseFile(shlClustbox, btnChooseDataFile, dataFile);

		Label lbldataFile = new Label(grpSourceFile, SWT.NONE);
		lbldataFile.setBounds(10, 30, 90, 15);
		lbldataFile.setText("Select Data File:");
	}

	private ModifyListener runBtnStatus() {
		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if ((simpleKMeans.getSelection() || simpleKMedoids.getSelection()) && dataFile.getText().length() > 0
						&& (noOfClusters.getText().length() > 0 || centroidsSource.getText().length() > 0)) {
					btnRun.setEnabled(true);
				} else if (sameSizedKMeans.getSelection()
						&& (dataFile.getText().length() > 0 && noOfClusters.getText().length() > 0)) {
					btnRun.setEnabled(true);
				} else if ((iterativeKMeans.getSelection() || iterativeKMedoids.getSelection())
						&& dataFile.getText().length() > 0 && iterativeOpt.getSelectionIndex() == 0
						&& noOfClusters.getText().length() > 0) {
					btnRun.setEnabled(true);
				} else if ((iterativeKMeans.getSelection() || iterativeKMedoids.getSelection())
						&& dataFile.getText().length() > 0 && iterativeOpt.getSelectionIndex() == 1) {
					btnRun.setEnabled(true);
				} else {
					btnRun.setEnabled(false);
				}
			}
		};
		return listener;
	}

	private void chooseFile(Shell shlClustbox, Button btnChooseFile, Text textField) {
		btnChooseFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FileDialog dataFileDailog = new FileDialog(shlClustbox);
				// DirectoryDialog dlg = new DirectoryDialog(shlClustbox);

				// Set the initial filter path according
				// to anything they've selected or typed in
				dataFileDailog.setFilterPath(textField.getText());

				// Extension Name
				dataFileDailog.setFilterNames(new String[] { "CSV (Comma delimited) (*.csv)",
						"Adobe Bridge Data (*.data)", "Excel Workbook (*.xlsx)" });
				// Extension Type
				dataFileDailog.setFilterExtensions(new String[] { "*.csv", "*.data", "*.xlsx" });

				// Calling open() will open and run the dialog.
				// It will return the selected directory, or
				// null if user cancels
				String dir = dataFileDailog.open();
				if (dir != null) {
					// Set the text box to the new selection
					textField.setText(dir);
				}
			}
		});
	}
}
