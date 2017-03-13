package com.clustbox.clustering;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class CBMain {

	public HashMap<String, String> formElements = new HashMap<String, String>();

	private Text dataFile;
	private Text noOfClusters;
	private Text centroidsSource;

	private Button btnRun;
	private Button btnCentroidsSource;

	private Button simpleKMeans;
	private Button iterativeKMeans;
	private Button sameSizedKMeans;
	private Button simpleKMedoids;
	private Button iterativeKMedoids;

	private Button sc;
	private Button sse;
	private Button scs;
	private Button aic;
	private Button bic;
	private Button cindex;
	private Button dbIndex;

	private Combo similarityMeasure;

	private Group grpAdditionalMetrices;

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
		cb.setConf(shlClustbox);
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

	private void selSimilarity(Shell shlClustbox) {
		String[] similarityItems = { "     ---- Select Similarity Measure ----     ", "EuclideanDistance",
				"CosineSimilarity", "JaccardIndexSimilarity", "ManhattanDistance", "MinkowskiDistance",
				"NormalizedEuclideanDistance", "PearsonCorrelationCoefficient" };

		similarityMeasure = new Combo(shlClustbox, SWT.DROP_DOWN | SWT.READ_ONLY);
		similarityMeasure.setItems(similarityItems);
		similarityMeasure.select(0);
		similarityMeasure.setBounds(530, 90, 240, 25);
	}

	private void clustAlgo(Shell shlClustbox) {
		Group grpClustAlgo = new Group(shlClustbox, SWT.NONE);
		grpClustAlgo.setText("Clustering Algorithms");
		grpClustAlgo.setBounds(10, 150, 500, 180);

		simpleKMeans = new Button(grpClustAlgo, SWT.RADIO);
		simpleKMeans.setBounds(20, 40, 140, 16);
		simpleKMeans.setText("Simple K-means");
		selAlgo(simpleKMeans);

		iterativeKMeans = new Button(grpClustAlgo, SWT.RADIO);
		iterativeKMeans.setBounds(20, 80, 140, 16);
		iterativeKMeans.setText("Iterative K-means");
		selAlgo(iterativeKMeans);

		sameSizedKMeans = new Button(grpClustAlgo, SWT.RADIO);
		sameSizedKMeans.setBounds(20, 120, 140, 16);
		sameSizedKMeans.setText("Same-sized K-means");
		selAlgo(sameSizedKMeans);

		simpleKMedoids = new Button(grpClustAlgo, SWT.RADIO);
		simpleKMedoids.setBounds(250, 40, 140, 16);
		simpleKMedoids.setText("Simple K-medoids");
		selAlgo(simpleKMedoids);

		iterativeKMedoids = new Button(grpClustAlgo, SWT.RADIO);
		iterativeKMedoids.setBounds(250, 80, 140, 16);
		iterativeKMedoids.setText("Iterative K-medoids");
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
				} else if ((button.equals(iterativeKMeans) || button.equals(iterativeKMedoids))
						&& dataFile.getText().length() > 0) {
					btnRun.setEnabled(true);
				} else if (button.equals(sameSizedKMeans) && dataFile.getText().length() > 0
						&& noOfClusters.getText().length() > 0) {
					btnRun.setEnabled(true);
				} else {
					btnRun.setEnabled(false);
				}

			}
		});
	}

	private void setConf(Shell shlClustbox) {
		noOfClusters = new Text(shlClustbox, SWT.BORDER);
		noOfClusters.setBounds(20, 400, 230, 25);
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
		ModifyListener listener = runBtnStatus();
		validateClusterText(noOfClusters);
		noOfClusters.addModifyListener(listener);

		centroidsSource = new Text(shlClustbox, SWT.BORDER);
		centroidsSource.setBounds(270, 400, 210, 25);
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

		btnCentroidsSource = new Button(shlClustbox, SWT.NONE);
		btnCentroidsSource.setBounds(480, 400, 25, 25);
		btnCentroidsSource.setText("....");
		btnCentroidsSource.setEnabled(false);
		chooseFile(shlClustbox, btnCentroidsSource, centroidsSource);

		Label lblNoOfClusters = new Label(shlClustbox, SWT.NONE);
		lblNoOfClusters.setBounds(20, 380, 120, 15);
		lblNoOfClusters.setText("Number Of Clusters:");

		Label lblCentroidsSource = new Label(shlClustbox, SWT.NONE);
		lblCentroidsSource.setBounds(270, 380, 120, 15);
		lblCentroidsSource.setText("Centroids Source:");
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
		sc = new Button(grpAdditionalMetrices, SWT.CHECK);
		sc.setBounds(20, 35, 140, 16);
		sc.setText("Silhouette Coefficient");
		sc.setSelection(true);
		sc.setEnabled(false);

		sse = new Button(grpAdditionalMetrices, SWT.CHECK);
		sse.setBounds(20, 70, 140, 16);
		sse.setText("Sum Of Squared Errors");

		scs = new Button(grpAdditionalMetrices, SWT.CHECK);
		scs.setBounds(20, 105, 170, 16);
		scs.setText("Sum Of Centroid Similarities");

		aic = new Button(grpAdditionalMetrices, SWT.CHECK);
		aic.setBounds(20, 140, 93, 16);
		aic.setText("AIC");

		bic = new Button(grpAdditionalMetrices, SWT.CHECK);
		bic.setBounds(20, 175, 119, 16);
		bic.setText("BIC");

		cindex = new Button(grpAdditionalMetrices, SWT.CHECK);
		cindex.setBounds(20, 210, 93, 16);
		cindex.setText("C-index");

		dbIndex = new Button(grpAdditionalMetrices, SWT.CHECK);
		dbIndex.setBounds(20, 245, 170, 16);
		dbIndex.setText("Davies-Bouldin index");
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

				// for (Map.Entry<String, String> entry :
				// formElements.entrySet()) {
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
		grpSourceFile.setBounds(10, 60, 500, 69);

		dataFile = new Text(grpSourceFile, SWT.BORDER);
		dataFile.setBounds(100, 30, 270, 25);
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
				} else if ((iterativeKMeans.getSelection() || iterativeKMedoids.getSelection())
						&& dataFile.getText().length() > 0) {
					btnRun.setEnabled(true);
				} else if (sameSizedKMeans.getSelection()
						&& (dataFile.getText().length() > 0 && noOfClusters.getText().length() > 0)) {
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
