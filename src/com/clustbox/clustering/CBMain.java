package com.clustbox.clustering;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.wb.swt.SWTResourceManager;

public class CBMain {

	public static HashMap<String, String> formElements = new HashMap<String, String>();
	private static Button btnRun;
	private static Text noOfClusters;
	private static Text centriodsSource;
	private static Button btnChooseCentriodFile;
	private static Text dataFile;
	private static boolean chkAlgo = false;

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

		Group grpSourceFile = new Group(shlClustbox, SWT.NONE);
		grpSourceFile.setText("Source File");
		grpSourceFile.setBounds(10, 60, 508, 69);

		dataFile = new Text(grpSourceFile, SWT.BORDER);
		dataFile.setBounds(140, 30, 275, 21);
		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (dataFile.getText().length() == 0)
					btnRun.setEnabled(false);
				else if (chkAlgo && dataFile.getText().length() > 0) {
					btnRun.setEnabled(true);
				}
			}
		};
		dataFile.addModifyListener(listener);

		Button btnChooseDataFile = new Button(grpSourceFile, SWT.NONE);
		btnChooseDataFile.setBounds(423, 30, 75, 25);
		btnChooseDataFile.setText("Choose File");
		btnChooseDataFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FileDialog dataFileDailog = new FileDialog(shlClustbox);
				// DirectoryDialog dlg = new DirectoryDialog(shlClustbox);

				// Set the initial filter path according
				// to anything they've selected or typed in
				dataFileDailog.setFilterPath(dataFile.getText());

				// Extension Name
				dataFileDailog.setFilterNames(new String[] { "Text Files", "Word Files", "PDF Files" });
				// Extension Type
				dataFileDailog.setFilterExtensions(new String[] { "*.txt", "*.doc", "*.pdf" });

				// Calling open() will open and run the dialog.
				// It will return the selected directory, or
				// null if user cancels
				String dir = dataFileDailog.open();
				if (dir != null) {
					// Set the text box to the new selection
					dataFile.setText(dir);
				}
			}
		});

		Label lbldataFile = new Label(grpSourceFile, SWT.NONE);
		lbldataFile.setBounds(10, 30, 125, 15);
		lbldataFile.setText("*Select Data File:");

		Group grpConfigurations = new Group(shlClustbox, SWT.NONE);
		grpConfigurations.setText("Configurations");
		grpConfigurations.setBounds(10, 150, 508, 303);

		Label lblNumberOfClusters = new Label(grpConfigurations, SWT.NONE);
		lblNumberOfClusters.setBounds(10, 150, 125, 15);
		lblNumberOfClusters.setText("Number Of Clusters:");

		noOfClusters = new Text(grpConfigurations, SWT.BORDER);
		noOfClusters.setBounds(140, 150, 185, 21);
		noOfClusters.addListener(SWT.Verify, new Listener() {
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
		noOfClusters.setEnabled(false);

		Label lblCentriodFile = new Label(grpConfigurations, SWT.NONE);
		lblCentriodFile.setBounds(10, 200, 125, 15);
		lblCentriodFile.setText("Centriods Source:");

		centriodsSource = new Text(grpConfigurations, SWT.BORDER);
		centriodsSource.setBounds(140, 200, 275, 21);
		centriodsSource.setEnabled(false);

		Button btnChooseCentriodFile = new Button(grpConfigurations, SWT.NONE);
		btnChooseCentriodFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dataFileDailog = new FileDialog(shlClustbox);
				// DirectoryDialog dlg = new DirectoryDialog(shlClustbox);

				// Set the initial filter path according
				// to anything they've selected or typed in
				dataFileDailog.setFilterPath(centriodsSource.getText());

				// Extension Name
				dataFileDailog.setFilterNames(new String[] { "Text Files", "Word Files", "PDF Files" });
				// Extension Type
				dataFileDailog.setFilterExtensions(new String[] { "*.txt", "*.doc", "*.pdf" });

				// Calling open() will open and run the dialog.
				// It will return the selected directory, or
				// null if user cancels
				String dir = dataFileDailog.open();
				if (dir != null) {
					// Set the text box to the new selection
					centriodsSource.setText(dir);
				}

			}
		});
		btnChooseCentriodFile.setBounds(423, 200, 75, 25);
		btnChooseCentriodFile.setText("Choose File");
		btnChooseCentriodFile.setEnabled(false);

		String[] similarityItems = { "EuclideanDistance", "CosineSimilarity", "JaccardIndexSimilarity",
				"ManhattanDistance", "MinkowskiDistance", "NormalizedEuclideanDistance",
				"PearsonCorrelationCoefficient" };
		Combo similarityMeasure = new Combo(grpConfigurations, SWT.NONE);
		similarityMeasure.setItems(similarityItems);
		similarityMeasure.select(0);
		similarityMeasure.setBounds(140, 30, 185, 23);

		Label lblSimilarity = new Label(grpConfigurations, SWT.NONE);
		lblSimilarity.setBounds(10, 30, 125, 15);
		lblSimilarity.setText("Similarity Measure:");

		Button bestK = new Button(grpConfigurations, SWT.CHECK);
		bestK.setBounds(10, 70, 125, 16);
		bestK.setText("Best K");
		bestK.setSelection(true);
		SelectionAdapter chkBtnListenerBestK = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					noOfClusters.setText("");
					noOfClusters.setEnabled(false);
				} else {
					noOfClusters.setEnabled(true);
				}
			}
		};
		bestK.addSelectionListener(chkBtnListenerBestK);

		Button bestCentroid = new Button(grpConfigurations, SWT.CHECK);
		bestCentroid.setBounds(10, 100, 125, 16);
		bestCentroid.setText("Best Centroid");
		bestCentroid.setSelection(true);
		SelectionAdapter chkBtnListenerBestC = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					centriodsSource.setText("");
					centriodsSource.setEnabled(false);
					btnChooseCentriodFile.setEnabled(false);
				} else {
					centriodsSource.setEnabled(true);
					btnChooseCentriodFile.setEnabled(true);
				}
			}
		};
		bestCentroid.addSelectionListener(chkBtnListenerBestC);

		Group grpAdditionalMetrices = new Group(shlClustbox, SWT.NONE);
		grpAdditionalMetrices.setText("Evaluation Metrics");
		grpAdditionalMetrices.setBounds(530, 150, 240, 303);

		Button sc = new Button(grpAdditionalMetrices, SWT.CHECK);
		sc.setBounds(20, 35, 150, 16);
		sc.setText("Silhouette Coefficient");
		sc.setSelection(true);
		sc.setEnabled(false);

		Button sse = new Button(grpAdditionalMetrices, SWT.CHECK);
		sse.setBounds(20, 70, 140, 16);
		sse.setText("Sum Of Squared Errors");

		Button scs = new Button(grpAdditionalMetrices, SWT.CHECK);
		scs.setBounds(20, 105, 170, 16);
		scs.setText("Sum Of Centroid Similarities");

		Button aic = new Button(grpAdditionalMetrices, SWT.CHECK);
		aic.setBounds(20, 140, 93, 16);
		aic.setText("AIC");

		Button bic = new Button(grpAdditionalMetrices, SWT.CHECK);
		bic.setBounds(20, 175, 119, 16);
		bic.setText("BIC");

		Button cindex = new Button(grpAdditionalMetrices, SWT.CHECK);
		cindex.setBounds(20, 210, 93, 16);
		cindex.setText("C-index");

		Button dbIndex = new Button(grpAdditionalMetrices, SWT.CHECK);
		dbIndex.setBounds(20, 245, 170, 16);
		dbIndex.setText("Davies-Bouldin index");

		Button kMeans = new Button(shlClustbox, SWT.RADIO);
		kMeans.setBounds(530, 95, 75, 16);
		kMeans.setText("K-Means");

		Button kMediod = new Button(shlClustbox, SWT.RADIO);
		kMediod.setBounds(610, 95, 75, 16);
		kMediod.setText("K-Mediod");

		Button sameSized = new Button(shlClustbox, SWT.RADIO);
		sameSized.setBounds(690, 95, 80, 16);
		sameSized.setText("Same-sized");

		SelectionAdapter radioBtnListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				chkAlgo = true;
				Button button = (Button) e.widget;
				if (button.getSelection() && dataFile.getText().length() > 0) {
					btnRun.setEnabled(true);
				} else {
					btnRun.setEnabled(false);
				}
			}
		};

		kMeans.addSelectionListener(radioBtnListener);
		kMediod.addSelectionListener(radioBtnListener);
		sameSized.addSelectionListener(radioBtnListener);

		Button btnExit = new Button(shlClustbox, SWT.NONE);
		btnExit.setBounds(695, 500, 75, 25);
		btnExit.setText("Exit");
		btnExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlClustbox.dispose();
			}
		});

		btnRun = new Button(shlClustbox, SWT.NONE);
		btnRun.setBounds(600, 500, 75, 25);
		btnRun.setText("Run");
		btnRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				formElements.put("dataFile", dataFile.getText());
				formElements.put("centriodsSource", centriodsSource.getText());
				formElements.put("noOfClusters", noOfClusters.getText());
				if (kMeans.getSelection()) {
					formElements.put("kMeans", kMeans.getText());
				}
				if (kMediod.getSelection()) {
					formElements.put("kMediod", kMediod.getText());
				}
				if (sameSized.getSelection()) {
					formElements.put("sameSized", sameSized.getText());
				}
				if (bestK.getSelection()) {
					formElements.put("bestK", bestK.getText());
				}
				if (bestCentroid.getSelection()) {
					formElements.put("bestCentroid", bestCentroid.getText());
				}
				formElements.put("sc", sc.getText());
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
				formElements.put("similarityMeasure", similarityMeasure.getText());
				for (Map.Entry<String, String> entry : formElements.entrySet()) {
					System.out.println("Keys: " + entry.getKey() + " Values: " + entry.getValue());
				}
			}
		});
		btnRun.setEnabled(false);

		Button btnReset = new Button(shlClustbox, SWT.NONE);
		btnReset.setBounds(504, 500, 75, 25);
		btnReset.setText("Reset");

		CLabel lblNewLabel = new CLabel(shlClustbox, SWT.NONE);
		lblNewLabel.setBounds(10, 10, 760, 20);
		lblNewLabel.setText(
				"CLUSTBOX: A black box of center-based clustering algorithms. To run the utility please enter the below form.");

		CLabel lblNewLabel_1 = new CLabel(shlClustbox, SWT.NONE);
		lblNewLabel_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		lblNewLabel_1.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		lblNewLabel_1.setBounds(10, 30, 760, 15);
		lblNewLabel_1.setText("Important: note that the data source file and algorithm selection is mandatory. ");
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dataFile.setText("");
				noOfClusters.setText("");
				noOfClusters.setEnabled(false);
				centriodsSource.setText("");
				centriodsSource.setEnabled(false);
				btnChooseCentriodFile.setEnabled(false);
				bestK.setSelection(true);
				bestCentroid.setSelection(true);
				sse.setSelection(false);
				scs.setSelection(false);
				aic.setSelection(false);
				bic.setSelection(false);
				cindex.setSelection(false);
				dbIndex.setSelection(false);
				kMeans.setSelection(false);
				kMediod.setSelection(false);
				sameSized.setSelection(false);
				similarityMeasure.select(0);
			}
		});

		shlClustbox.open();
		shlClustbox.layout();
		while (!shlClustbox.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
