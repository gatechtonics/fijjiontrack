/*
 * #%L
 * Cell Counter plugin for ImageJ.
 * %%
 * Copyright (C) 2001 - 2019 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package sc.fiji.fissionTrackCounter;

import ij.*;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import org.scijava.Context;
import org.scijava.options.OptionsService;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.*;

/**
 * TODO
 * @author Kurt De Vos
 * @author Ziya Ye
 */
public class FissionTrackCounter extends JFrame implements ActionListener, ItemListener
{

	private static final String REMOVE = "Remove";
	private static final String RENAME = "Rename";
	private static final String INITIALIZE = "Initialize";
	private static final String RESULTS = "Results";
	private static final String UNDO = "Undo";
	private static final String DELMODE = "Delete Mode";
	private static final String UNIMODE = "Unique ID";
	private static final String KEEPORIGINAL = "Keep Original";
	private static final String SHOWNUMBERS = "Show Numbers";
	private static final String SHOWALL = "Show All";
	private static final String RESET = "Reset";
	private static final String RESETCAIXS = "ResetCAxis";
	private static final String GRID = "Grid Panel";
	private boolean grid = false;
	private static final String EXPORTMARKERS = "Save Markers";
	private static final String LOADMARKERS = "Load Markers";
	private static final String EXPORTIMG = "Export Image";
	private static final String MEASURE = "Detail";
	private static final String MEASURE3 = "Summary";
	private static final String CHANGECOLOR = "Change Color";
	private static final String ROIMODIFY = "Drag ROI";
	private static final String TYPE_COMMAND_PREFIX = "type";
//	private static final String THRESHOLD = "Threshold";
//	private static final String AITRACKTIVE = "AiTracktive";
//	private static final String SKELETRACKS = "Skeletracks";
//	private static final String AUTOCOUNT = "AutoCount";
//	private static final String YOLO = "Yolo";

	private Vector<FissionTrackCntrMarkerVector> typeVector;
	private ArrayList<Vector<FissionTrackCntrMarkerVector>> allTypeVector = new ArrayList<>();
	private Vector<JRadioButton> dynRadioVector;
	private final Vector<JTextField> txtFieldVector;
	private FissionTrackCntrMarkerVector markerVector;
	private FissionTrackCntrMarkerVector currentMarkerVector;
	private int currentMarkerIndex;
	private Roi storedRoi;
	private ArrayList<Roi> storedRoiList = new ArrayList<>();
		
	// Map<key,value> for storing metadata to write with WriteXML
	private Map<String,String> metaData = new HashMap<>();
	private ArrayList<Map<String, String>> metaDataList = new ArrayList<>();
	private JPanel dynPanel;
	private JPanel dynButtonPanel;
	private JPanel statButtonPanel;
//	private JPanel autoPanel;
//	private JPanel autoButtonPanel;
	private JPanel dynTxtPanel;
	private JCheckBox delCheck;
	private JCheckBox roiCheck;
	private JCheckBox newCheck;
	private JCheckBox numbersCheck;
	private JCheckBox showAllCheck;
	//option for Mica/Mount Counter
	private JCheckBox uniqueID;

	private ButtonGroup radioGrp;
	private ButtonGroup autoButtonGrp;
	private JSeparator separator;
	private JButton removeButton;
	private JButton renameButton;
	private JButton initializeButton;
	private JButton thresholdButton;
	private JButton yoloButton;
	private JButton aiTracktiveButton;
	private JButton skeletracksButton;
	private JButton autoCountButton;

//	private JButton optionsButton;
	private JButton resultsButton;
	private JButton unDoButton;
//	private JButton reDoButton;
	private JButton resetButton;
	private JButton resetCButton;
	private JButton exportButton;
	private JButton loadButton;
	private JButton exportimgButton;
	private JButton measureButton;
	private JButton measure3Button;
	private JButton colorButton;
	private JLabel textRoi;

	private JButton gridButton;

	private boolean keepOriginal = false;

	private FissionTrackCntrImageCanvas ic;
	private ArrayList<FissionTrackCntrImageCanvas> icList = new ArrayList<>();
	private ArrayList<ImagePlus> imgList = new ArrayList<>();
	private ImagePlus img;
	private ArrayList<ImagePlus> counterImgList = new ArrayList<>();
	private ImagePlus counterImg;
	private GridLayout dynGrid;
	private FissionTrackCounterOptions options = new FissionTrackCounterOptions();
	private ImagePanel imagePanel = null;
	static FissionTrackCounter instance;


	public FissionTrackCounter() {
		super("Fijjiontrack");
		setResizable(false);
		typeVector = new Vector<FissionTrackCntrMarkerVector>();
		txtFieldVector = new Vector<JTextField>();
		dynRadioVector = new Vector<JRadioButton>();
		initGUI();
		populateTxtFields();
		instance = this;
	}

	/** Show the GUI threadsafe */
	private static class GUIShower implements Runnable {

		final JFrame jFrame;

		public GUIShower(final JFrame jFrame) {
			this.jFrame = jFrame;
		}

		@Override
		public void run() {
			jFrame.pack();
			jFrame.setLocation(1000, 200);
			jFrame.setVisible(true);
		}
	}

	private void initGUI() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final GridBagLayout gb = new GridBagLayout();
		getContentPane().setLayout(gb);
		radioGrp = new ButtonGroup();// to group the radiobuttons

		autoButtonGrp = new ButtonGroup();

		dynGrid = new GridLayout(5, 1);
		//dynGrid.setVgap(2);

		//This panel is for automatic counting buttons
//		autoPanel = new JPanel();
//		autoPanel.setBorder(BorderFactory.createTitledBorder("Automatic"));
//		autoPanel.setLayout(gb);

//		// this panel keeps the radiobuttons
//		autoButtonPanel = new JPanel();
//		GridLayout grid = new GridLayout(2, 1);
//		grid.setVgap(2);
//		autoButtonPanel.setLayout(grid);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.anchor = GridBagConstraints.NORTHWEST;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.ipadx = 5;
		gbc1.gridy = 1;
//		gb.setConstraints(autoPanel, gbc1);
//		autoPanel.add(autoButtonPanel);
//		aiTracktiveButton = makeButton(AITRACKTIVE, "Apply AiTracktive to image");
//		autoButtonPanel.add(aiTracktiveButton);
//		skeletracksButton = makeButton(SKELETRACKS, "Apply Skeletracks to image");
//		autoButtonPanel.add(skeletracksButton);
//		thresholdButton = makeButton(THRESHOLD, "Apply threshold to image");
//		autoButtonPanel.add(thresholdButton);
//		autoCountButton = makeButton(AUTOCOUNT, "Auto Count fission tracks according to threshold");
//		autoButtonPanel.add(autoCountButton);
//		yoloButton = makeButton(YOLO, "Auto count fission tracks using YOLO");
//		autoButtonPanel.add(yoloButton);


//		getContentPane().add(autoPanel);




		// this panel will keep the dynamic GUI parts
		dynPanel = new JPanel();
		dynPanel.setBorder(BorderFactory.createTitledBorder("Manuel"));
		dynPanel.setLayout(gb);

		// this panel keeps the radiobuttons
		dynButtonPanel = new JPanel();
		dynButtonPanel.setLayout(dynGrid);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 10;
		gb.setConstraints(dynButtonPanel, gbc);
//		gb.setConstraints(autoButtonPanel,gbc);
		dynPanel.add(dynButtonPanel);

		// this panel keeps the score
		dynTxtPanel = new JPanel();
		dynTxtPanel.setLayout(dynGrid);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 5;
		gb.setConstraints(dynTxtPanel, gbc);
		dynPanel.add(dynTxtPanel);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.ipadx = 5;
		gb.setConstraints(dynPanel, gbc);
		getContentPane().add(dynPanel);

		JRadioButton btn1 = makeDynRadioButton(1);
		btn1.setText("Mount Count");
		dynButtonPanel.add(btn1);

		JRadioButton btn2 = makeDynRadioButton(2);
		btn2.setText("Mica count");
		dynButtonPanel.add(btn2);

		JRadioButton btn3 = makeDynRadioButton(3);
		btn3.setText("2d distance");
		dynButtonPanel.add(btn3);

		JRadioButton btn4 = makeDynRadioButton(4);
		btn4.setText("3d distance");
		dynButtonPanel.add(btn4);

		JRadioButton btn5 = makeDynRadioButton(5);
		btn5.setText("C-axis");
		dynButtonPanel.add(btn5);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.ipadx = 5;

		textRoi = new JLabel();
		textRoi.setText("ROI Area: ");
		dynPanel.add(textRoi, gbc);
//		dynTxtPanel.add()

		// create a "static" panel to hold control buttons
		statButtonPanel = new JPanel();
		statButtonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
		statButtonPanel.setLayout(gb);
//		autoButtonPanel.setLayout(gb);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		newCheck = new JCheckBox(KEEPORIGINAL);
		newCheck.setToolTipText("Keep original");
		newCheck.setSelected(false);
		newCheck.addItemListener(this);
		gb.setConstraints(newCheck, gbc);
		statButtonPanel.add(newCheck);
		//Check for consistent/inconsistent count for marker
		uniqueID = new JCheckBox(UNIMODE);
		uniqueID.setToolTipText("Unique Marker ID");
		uniqueID.setSelected(false);
		uniqueID.addItemListener(this);
		gb.setConstraints(uniqueID, gbc);
		statButtonPanel.add(uniqueID);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		initializeButton = makeButton(INITIALIZE, "Initialize image to count");
		gb.setConstraints(initializeButton, gbc);
		statButtonPanel.add(initializeButton);
		gridButton = makeButton(GRID, "Place grid over the image");
		gb.setConstraints(gridButton, gbc);
		gridButton.setEnabled(false);
		statButtonPanel.add(gridButton);
//


		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(3, 0, 3, 0);
		separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setPreferredSize(new Dimension(1, 1));
		gb.setConstraints(separator, gbc);
		statButtonPanel.add(separator);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		removeButton = makeButton(REMOVE, "remove last counter type");
		gb.setConstraints(removeButton, gbc);


		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		renameButton = makeButton(RENAME, "rename selected counter type");
		renameButton.setEnabled(false);
		gb.setConstraints(renameButton, gbc);


		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.insets = new Insets(3, 0, 3, 0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setPreferredSize(new Dimension(1, 1));
		gb.setConstraints(separator, gbc);
		statButtonPanel.add(separator);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		unDoButton = makeButton(UNDO,"delete last marker");
		unDoButton.setEnabled(false);
		gb.setConstraints(unDoButton, gbc);
		statButtonPanel.add(unDoButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
//		reDoButton.setEnabled(false);
//		gb.setConstraints(reDoButton, gbc);
//		statButtonPanel.add(reDoButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		delCheck = new JCheckBox(DELMODE);
		delCheck
			.setToolTipText("When selected\nclick on the marker\nyou want to remove");
		delCheck.setSelected(false);
		delCheck.addItemListener(this);
		delCheck.setEnabled(false);
		gb.setConstraints(delCheck, gbc);
		statButtonPanel.add(delCheck);

		roiCheck = new JCheckBox(ROIMODIFY);
		roiCheck.setToolTipText("When selected\nyou can move roi");
		roiCheck.setSelected(false);
		roiCheck.addItemListener(this);
		roiCheck.setEnabled(false);
		gb.setConstraints(roiCheck, gbc);
		statButtonPanel.add(roiCheck);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(3, 0, 3, 0);
		separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setPreferredSize(new Dimension(1, 1));
		gb.setConstraints(separator, gbc);
		statButtonPanel.add(separator);



		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		measureButton =
				makeButton(MEASURE, "Measure pixel intensity of marker points");
		measureButton.setEnabled(false);
		gb.setConstraints(measureButton, gbc);
		statButtonPanel.add(measureButton);


		measure3Button =
				makeButton(MEASURE3, "Measure frequency");
		measure3Button.setEnabled(false);
		gb.setConstraints(measure3Button, gbc);
		statButtonPanel.add(measure3Button);

		colorButton = makeButton(CHANGECOLOR, "Change color");
		colorButton.setEnabled(false);
		gb.setConstraints(colorButton, gbc);
		statButtonPanel.add(colorButton);



		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(3, 0, 3, 0);
		separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setPreferredSize(new Dimension(1, 1));
		gb.setConstraints(separator, gbc);
		statButtonPanel.add(separator);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		numbersCheck = new JCheckBox(SHOWNUMBERS);
		numbersCheck.setToolTipText("When selected, numbers are shown");
		numbersCheck.setSelected(true);
		numbersCheck.setEnabled(false);
		numbersCheck.addItemListener(this);
		gb.setConstraints(numbersCheck, gbc);
		statButtonPanel.add(numbersCheck);

		showAllCheck = new JCheckBox(SHOWALL);
		showAllCheck.setToolTipText("When selected, all stack markers are shown");
		showAllCheck.setSelected(false);
		showAllCheck.setEnabled(false);
		showAllCheck.addItemListener(this);
		gb.setConstraints(showAllCheck, gbc);
		statButtonPanel.add(showAllCheck);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		exportButton = makeButton(EXPORTMARKERS, "Save markers to file");
		exportButton.setEnabled(false);
		gb.setConstraints(exportButton, gbc);
		statButtonPanel.add(exportButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		loadButton = makeButton(LOADMARKERS, "Load markers from file");
		gb.setConstraints(loadButton, gbc);
		statButtonPanel.add(loadButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		exportimgButton = makeButton(EXPORTIMG, "Export image with markers");
		exportimgButton.setEnabled(false);
		gb.setConstraints(exportimgButton, gbc);
		statButtonPanel.add(exportimgButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		resetCButton = makeButton(RESETCAIXS, "only reset c-axis");
		resetCButton.setEnabled(false);
		gb.setConstraints(resetCButton, gbc);
		statButtonPanel.add(resetCButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		resetButton = makeButton(RESET, "reset all counters");
		resetButton.setEnabled(false);
		gb.setConstraints(resetButton, gbc);
		statButtonPanel.add(resetButton);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(3, 0, 3, 0);
		separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setPreferredSize(new Dimension(1, 1));
		gb.setConstraints(separator, gbc);
		statButtonPanel.add(separator);

		gbc = new GridBagConstraints();
		gbc.ipadx = 5;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gb.setConstraints(statButtonPanel, gbc);
		getContentPane().add(statButtonPanel);

		final Runnable runner = new GUIShower(this);
		setSize(new Dimension(10000, 50));
		setResizable(true);
		EventQueue.invokeLater(runner);
	}

	private JTextField makeDynamicTextArea() {
		final JTextField txtFld = new JTextField();
		txtFld.setHorizontalAlignment(SwingConstants.CENTER);
		txtFld.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		txtFld.setEditable(false);
		txtFld.setText("0");
		txtFieldVector.add(txtFld);
		return txtFld;
	}

	void populateTxtFields() {
		final ListIterator<FissionTrackCntrMarkerVector> it = typeVector.listIterator();
		while (it.hasNext()) {
			final int index = it.nextIndex();
			if (txtFieldVector.size() > index) {
				final FissionTrackCntrMarkerVector markerVector = it.next();
				final int count = markerVector.size();
				final JTextField tArea = txtFieldVector.get(index);
				if(markerVector.getType() >= 3) {
					tArea.setText("" + (int)count/2);
				}
				else {
					tArea.setText("" + count);
				}
			}
		}
		if (ic != null) {
			textRoi.setText("ROI Area: " + String.format("%.2e", ic.getRoiArea()/Math.pow(10, 8)));
		}
		validateLayout();
	}

	private JRadioButton makeDynRadioButton(final int id) {
		final JRadioButton jrButton = new JRadioButton("Type " + id);
		jrButton.setActionCommand(TYPE_COMMAND_PREFIX + id);
		jrButton.addActionListener(this);
		dynRadioVector.add(jrButton);
		radioGrp.add(jrButton);
		final String markerName = ("Type " + id);
//		markerVector = new FissionTrackCntrMarkerVector(id,markerName);
//
//		typeVector.add(markerVector);
		dynTxtPanel.add(makeDynamicTextArea());
		return jrButton;
	}

	private JRadioButton makeAutoRadioButton(final String id) {
		final JRadioButton jrButton = new JRadioButton(id);
		autoButtonGrp.add(jrButton);
		return jrButton;
	}

	private JButton makeButton(final String name, final String tooltip) {
		final JButton jButton = new JButton(name);
		jButton.setToolTipText(tooltip);
		jButton.addActionListener(this);
		return jButton;
	}

	private void initializeImage() {
		resetWithoutConfirmation();
		//resetCAxis();
		final boolean v139t = IJ.getVersion().compareTo("1.39t") >= 0;
		int[] ids = WindowManager.getIDList();
		if (imagePanel != null) {
			imagePanel.reset();
		}
		imagePanel = new ImagePanel(this);
		add(imagePanel);
		setVisible(true);
//		imagePanel.setVisible(true);
		for (int i = 0; i < ids.length; i++) {
			// Get the ImagePlus object for each window
			img = WindowManager.getImage(ids[i]);

			// Get the title of each window
			String title = img.getTitle();
			imagePanel.addButton(title);
//			imagePanel.setCurrentIndex(i);


			typeVector = new Vector<FissionTrackCntrMarkerVector>();
			for (int id = 1; id < 6; id++) {
				final String markerName = ("Type " + id);
				markerVector = new FissionTrackCntrMarkerVector(id,markerName);
				typeVector.add(markerVector);
			}
			processImage(img, v139t);
			img.close();
			this.allTypeVector.add(typeVector);
		}

		
		if (!keepOriginal) {
			img.changes = false;
		}
		delCheck.setEnabled(true);
		roiCheck.setEnabled(true);
		numbersCheck.setEnabled(true);
		showAllCheck.setSelected(false);
		uniqueID.setSelected(true);
		delCheck.setSelected(false);
		if (counterImg.getStackSize() > 1) showAllCheck.setEnabled(true);
		removeButton.setEnabled(true);
		renameButton.setEnabled(true);
//		resultsButton.setEnabled(true);
		unDoButton.setEnabled(true);
		resetButton.setEnabled(true);
		resetCButton.setEnabled(true);
		exportButton.setEnabled(true);
		exportimgButton.setEnabled(true);
		measureButton.setEnabled(true);
		measure3Button.setEnabled(true);
		colorButton.setEnabled(true);
		gridButton.setEnabled(true);
		initializeColor();

	}
	private void processImage(ImagePlus img, boolean v139t) {
		if (img == null) {
			IJ.noImage();
		}else {
			Roi roi = img.getRoi();
			storedRoi = roi;
			storedRoiList.add(roi);
			ImageProcessor ip = img.getProcessor();
			ip.setRoi(roi);
			ImageStatistics is = ip.getStatistics();
			double pixelCount = is.pixelCount;


// Get the calibration object and the unit of measurement
			Calibration cal = img.getCalibration();
			double RoiArea = (pixelCount * Math.pow(cal.pixelWidth, 2));

			if (img.getStackSize() == 1) {
				if (keepOriginal) ip = ip.crop();
				counterImg = new ImagePlus("Counter Window - " + img.getTitle(), ip);
				@SuppressWarnings("unchecked")
				Overlay displayList;
				if (v139t) {
					displayList = new Overlay();
					displayList.add(roi);
					displayList.setStrokeColor(Color.white);
				}
				else{
					displayList = null;
				}
				ic = new FissionTrackCntrImageCanvas(counterImg, typeVector, this, displayList);
				new ImageWindow(counterImg, ic);
			}
			else if (img.getStackSize() > 1) {
				final ImageStack stack = img.getStack();
				final int size = stack.getSize();
				final ImageStack counterStack = img.createEmptyStack();
				for (int i = 1; i <= size; i++) {
					ip = stack.getProcessor(i);
					if (keepOriginal) ip = ip.crop();
					counterStack.addSlice(stack.getSliceLabel(i), ip);
				}
				counterImg =
						new ImagePlus("Counter Window - " + img.getTitle(), counterStack);

				counterImg.setDimensions(img.getNChannels(), img.getNSlices(), img
						.getNFrames());
				if (img.isComposite()) {
					counterImg =
							new CompositeImage(counterImg, ((CompositeImage) img).getMode());
					((CompositeImage) counterImg).copyLuts(img);
				}
				counterImg.setOpenAsHyperStack(img.isHyperStack());
				@SuppressWarnings("unchecked")
				Overlay displayList;
				if (v139t) {
					displayList = new Overlay();
					roi = img.getRoi();
					displayList.add(roi);
					displayList.setStrokeColor(Color.white);

				}
				else{
					displayList = null;
				}
				ic = new FissionTrackCntrImageCanvas(counterImg, typeVector, this, displayList);
				new StackWindow(counterImg, ic);

			}
			cal = img.getCalibration();	//	to conserve voxel size of the original image
			counterImg.setCalibration(cal);

			// Extracting calibration data to write to XML
			metaData.put("X_Calibration", "" + cal.pixelWidth);
			metaData.put("Y_Calibration", "" + cal.pixelHeight);
			metaData.put("Z_Calibration", "" + cal.pixelDepth);
			metaData.put("Calibration_Unit", "" + cal.getUnit());
			metaDataList.add(metaData);
			metaData = new HashMap<>();
			ic.setRoiArea(RoiArea);
//			imgList.add(img);
			counterImgList.add(counterImg);
			icList.add(ic);
			textRoi.setText("ROI Area: " + String.format("%.2e", RoiArea/Math.pow(10, 8)));

		}

	}

	void validateLayout() {
		dynPanel.validate();
		dynButtonPanel.validate();
//		autoPanel.validate();
//		autoButtonPanel.validate();
		dynTxtPanel.validate();
		statButtonPanel.validate();
		validate();
		pack();
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();

		if (command.equals(REMOVE)) {
			if (dynRadioVector.size() > 1) {
				final JRadioButton rbutton = dynRadioVector.lastElement();
				dynButtonPanel.remove(rbutton);
				radioGrp.remove(rbutton);
				dynRadioVector.removeElementAt(dynRadioVector.size() - 1);
				dynGrid.setRows(dynRadioVector.size());
			}
			if (txtFieldVector.size() > 1) {
				final JTextField field = txtFieldVector.lastElement();
				dynTxtPanel.remove(field);
				txtFieldVector.removeElementAt(txtFieldVector.size() - 1);
			}
			if (typeVector.size() > 1) {
				typeVector.removeElementAt(typeVector.size() - 1);
			}
			validateLayout();

			if (ic != null) ic.setTypeVector(typeVector);
		}
		else if (command.equals(RENAME)) {
			if (currentMarkerIndex < 0) return; // no counter type selected
			final JRadioButton button = dynRadioVector.get(currentMarkerIndex);
			final String name =
				IJ.getString("Enter new counter name", button.getText());
			if (name == null || name.isEmpty()) return;
			radioGrp.remove(button);
			button.setText(name);
			radioGrp.add(button);
			currentMarkerVector.setName(name);
		}
		else if (command.equals(INITIALIZE)) {
			initializeImage();
		}
//		else if (command.equals(THRESHOLD)) {
//			if (ic == null) {
//				IJ.error("You need to initialize first");
//				return;
//			}
//				IJ.run("Threshold...");
//				IJ.setAutoThreshold(counterImg, "Default");
//		}
//		else if (command.equals(YOLO)) {
//			if (ic == null) {
//				IJ.error("You need to initialize first");
//				return;
//			}
//			else if (currentMarkerVector == null) {
//				IJ.error("Please Select a Counter Type First");
//			} else {
//				// add the yolo code here
//
//			}
//		}
//		else if (command.startsWith(AUTOCOUNT)) {
//			if (currentMarkerVector == null) {
//				IJ.error("Please Select a Counter Type First");
//			} else {
//				IJ.run(counterImg, "Analyze Particles...", "display add");
//				IJ.run("Set Measurements...", "area centroid fit shape stack redirect=None decimal=3");
//				ResultsTable resultTable = Analyzer.getResultsTable();
//
//				while (resultTable.size() == 0) {
//					resultTable = Analyzer.getResultsTable();
//				}
//				RoiManager roiManager = RoiManager.getInstance();
//				for(int i = 0; i < roiManager.getCount(); i++) {
//					roiManager.select(i);
//					roiManager.runCommand(counterImg, "Measure");
//					resultTable = Analyzer.getResultsTable();
//					while (resultTable.size() == 0) {
//						resultTable = Analyzer.getResultsTable();
//					}
//					Calibration cal = img.getCalibration();
//					int x = (int) (resultTable.getValue("X", i) / cal.pixelWidth);
//					int y = (int) (resultTable.getValue("Y", i) / cal.pixelHeight);
//					double major = (resultTable.getValue("Major", i) / cal.pixelWidth);
//					double minor = (resultTable.getValue("Minor", i) / cal.pixelHeight);
//					double angle = (resultTable.getValue("Angle", i) / cal.pixelHeight);
//					double area = (resultTable.getValue("Area", i) / cal.pixelWidth);
//					double circ = (resultTable.getValue("Circ.", i) / cal.pixelWidth);
//					System.out.println("Major: " + major);
//					System.out.println("Minor: " + minor);
//					System.out.println("Angle: " + angle);
//					System.out.println("Area: " + area);
//					System.out.println("Circ: " + circ);
//					if ((storedRoi == null || storedRoi.contains(x, y)) && 1 < area && area < 10 && circ > 0.5) {
//						final FissionTrackCntrMarker m = new FissionTrackCntrMarker(x, y, counterImg.getCurrentSlice());
//						currentMarkerVector.addMarker(m);
//						m.setID(currentMarkerVector.getUniqueID());
//					}
//				}
//				repaint();
//			}
//		}
		else if (command.startsWith(TYPE_COMMAND_PREFIX)) { // COUNT
			validateLayout();
			currentMarkerIndex =
				Integer.parseInt(command.substring(TYPE_COMMAND_PREFIX.length())) - 1;
			if (ic == null) {
				IJ.error("You need to initialize first");
				return;
			}
			currentMarkerVector = typeVector.get(currentMarkerIndex);
			ic.setCurrentMarkerVector(currentMarkerVector);
		}
		else if (command.equals(UNDO)) {
			ic.removeLastMarker();
		}
		else if (command.equals(RESET)) {
			reset();
		}
		else if (command.equals(RESETCAIXS)){
			resetCAxis();
		}
		else if (command.equals(RESULTS)) {
			report();
		}
		else if (command.equals(EXPORTMARKERS)) {
			exportMarkers();
		}
		else if (command.equals(LOADMARKERS)) {
			if (ic == null) {
				IJ.error("You need to initialize first");
				return;
			}
			loadMarkers();
			validateLayout();
		}
		else if (command.equals(EXPORTIMG)) {
			ic.imageWithMarkers().show();
		}
		else if (command.equals(MEASURE)) {
			measure();
		}else if(command.equals(MEASURE3)) {
			measure3();
		} else if (command.equals(CHANGECOLOR)) {
			int type = typeVector.indexOf(currentMarkerVector) + 1;
			changeColor(type);
			ic.repaint();
		} else if (command.equals(GRID)) {
			IJ.run("Grid...");
		}
		if (ic != null) ic.repaint();
		populateTxtFields();
	}

	@Override
	public void itemStateChanged(final ItemEvent e) {
		if (e.getItem().equals(delCheck)) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ic.setDelmode(true);
			}
			else {
				ic.setDelmode(false);
			}
		}
		//action for unique marker id
		else if(e.getItem().equals(uniqueID)){
			if(e.getStateChange() == ItemEvent.SELECTED){
				ic.setUniqueID(true);
			}
			else {
				ic.setUniqueID(false);
			}


		}
		else if(e.getItem().equals(roiCheck)) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ic.setRoimode(true);
			}
			else {
				ic.setRoimode(false);
			}
		}
		else if (e.getItem().equals(newCheck)) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				keepOriginal = true;
			}
			else {
				keepOriginal = false;
			}
		}
		else if (e.getItem().equals(numbersCheck)) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ic.setShowNumbers(true);
			}
			else {
				ic.setShowNumbers(false);
			}
			ic.repaint();
		}
		else if (e.getItem().equals(showAllCheck)) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				ic.setShowAll(true);
			}
			else {
				ic.setShowAll(false);
			}
			ic.repaint();
		}
	}

	public void measure() {
		ic.measure();
	}


	public void measure3() {
		ic.measure3();
	}

 	public void reset() {
		int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to reset?", "Confirmation", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			resetWithoutConfirmation();
		}
	}
	public void resetWithoutConfirmation() {
		resetCAxis();
		if (typeVector.size() < 1) {
			return;
		}
		final ListIterator<FissionTrackCntrMarkerVector> mit = typeVector.listIterator();
		while (mit.hasNext()) {
			final FissionTrackCntrMarkerVector mv = mit.next();
			mv.clear();
			mv.resetID();
		}
		if (ic != null) ic.repaint();
	}

	//Method to reset the C-Axis only
	public void resetCAxis() {
		if (typeVector.size() < 1) {
			return;
		}
		final ListIterator<FissionTrackCntrMarkerVector> mit = typeVector.listIterator();
		while (mit.hasNext()) {
			final FissionTrackCntrMarkerVector mv = mit.next();
			if (mv.getType() == 5) {
				if (!mit.hasNext()) {
					// The last element is being removed
					mit.remove();
					mit.add(new FissionTrackCntrMarkerVector(5, "Type 5"));
				}
			}
		}

		if (ic != null) ic.repaint();

	}

	public void report() {
		String labels = "Slice\t";
		final boolean isStack = counterImg.getStackSize() > 1;
		// add the types according to the button vector!!!!
		final ListIterator<JRadioButton> it = dynRadioVector.listIterator();
		while (it.hasNext()) {
			final JRadioButton button = it.next();
			final String str = button.getText();
			labels = labels.concat(str + "\t");
		}
		labels = labels.concat("\tC-pos\tZ-pos\tT-pos\t");						// add new columns containing C,Z,T positions
		
		IJ.setColumnHeadings(labels);
		String results = "";
		if (isStack) {
			for (int slice = 1; slice <= counterImg.getStackSize(); slice++) {
				
				int[] realPosArray = counterImg.convertIndexToPosition(slice); // from the slice we get the array  [channel, slice, frame]
				final int channel 	= realPosArray[0];
				final int zPos		= realPosArray[1];
				final int frame 	= realPosArray[2];
				
				results = "";
				final ListIterator<FissionTrackCntrMarkerVector> mit =
					typeVector.listIterator();
				final int types = typeVector.size();
				final int[] typeTotals = new int[types];
				while (mit.hasNext()) {
					final int type = mit.nextIndex();
					final FissionTrackCntrMarkerVector mv = mit.next();
					final ListIterator<FissionTrackCntrMarker> tit = mv.listIterator();
					while (tit.hasNext()) {
						final FissionTrackCntrMarker m = tit.next();
						if (m.getZ() == slice) {
							typeTotals[type]++;
						}
					}
				}
				results = results.concat(slice + "\t");
				for (int i = 0; i < typeTotals.length; i++) {
					results = results.concat(typeTotals[i] + "\t");
				}
				String cztPosition = String.format("%d\t%d\t%d\t",channel,zPos,frame);	// concat the c,z,t value position 
				results = results.concat(cztPosition);
				IJ.write(results);
			}
			IJ.write("");
		}
		results = "Total\t";
		final ListIterator<FissionTrackCntrMarkerVector> mit = typeVector.listIterator();
		while (mit.hasNext()) {
			final FissionTrackCntrMarkerVector mv = mit.next();
			final int count = mv.size();
			results = results.concat(count + "\t");
		}
		IJ.write(results);
	}

	public void loadMarkers() {
		final String filePath =
			getFilePath(new JFrame(), "Select Marker File", OPEN);
		final ReadXML rxml = new ReadXML(filePath);
		final String storedfilename =
			rxml.readImgProperties(ReadXML.IMAGE_FILE_PATH);
		if (storedfilename.equals(counterImg.getTitle())) {
			final Vector<FissionTrackCntrMarkerVector> loadedvector = rxml.readMarkerData();
			int index = allTypeVector.indexOf(typeVector);

			typeVector = loadedvector;
			ic.setTypeVector(typeVector);
			allTypeVector.set(index, typeVector);
			Roi roi = null;
			try {
				roi = rxml.readRoi();
			} catch (IOException e) {
				e.printStackTrace();
			}
			storedRoiList.set(index, roi);
			storedRoi = storedRoiList.get(index);
			counterImg.setRoi(roi);
			Overlay displayList = new Overlay();
			displayList.add(roi);
			displayList.setStrokeColor(Color.white);
			ic.setOverlay(displayList);
			index =
				Integer.parseInt(rxml.readImgProperties(ReadXML.CURRENT_TYPE));
			currentMarkerVector = typeVector.get(index);
			ic.setCurrentMarkerVector(currentMarkerVector);
			ImageProcessor ip = counterImg.getProcessor();
			ImageStatistics is = ip.getStatistics();
			double pixelCount = is.pixelCount;
			// Get the calibration object and the unit of measurement
			Calibration cal = counterImg.getCalibration();
			ic.setRoiArea(pixelCount * Math.pow(cal.pixelWidth, 2));
			textRoi.setText("ROI Area: " + String.format("%.2e", ic.getRoiArea()/Math.pow(10, 8)));

		}
		else {
			IJ.error("These Markers do not belong to the current image");
		}
        ic.repaint();
	}

	public void exportMarkers() {
		String filePath =
			getFilePath(new JFrame(), "Save Marker File (.xml)", SAVE);
		if (!filePath.endsWith(".xml")) filePath += ".xml";
		final WriteXML wxml = new WriteXML(filePath);
		wxml.writeXML(counterImg.getTitle(), typeVector, typeVector
			.indexOf(currentMarkerVector), metaData, storedRoi);

	}

	public static final int SAVE = FileDialog.SAVE, OPEN = FileDialog.LOAD;

	private String getFilePath(final JFrame parent, String dialogMessage,
		final int dialogType)
	{
		switch (dialogType) {
			case (SAVE):
				dialogMessage = "Save " + dialogMessage;
				break;
			case (OPEN):
				dialogMessage = "Open " + dialogMessage;
				break;
		}
		FileDialog fd;
		final String[] filePathComponents = new String[2];
		final int PATH = 0;
		final int FILE = 1;
		fd = new FileDialog(parent, dialogMessage, dialogType);
		switch (dialogType) {
			case (SAVE):
				final String filename = counterImg.getTitle();
				fd.setFile("FissionTrackCounter_" +
					filename.substring(0, filename.lastIndexOf(".") + 1) + "xml");
				break;
		}
		fd.setVisible(true);
		filePathComponents[PATH] = fd.getDirectory();
		filePathComponents[FILE] = fd.getFile();
		return filePathComponents[PATH] + filePathComponents[FILE];
	}

	public Vector<JRadioButton> getButtonVector() {
		return dynRadioVector;
	}

	public void setButtonVector(final Vector<JRadioButton> buttonVector) {
		this.dynRadioVector = buttonVector;
	}

	public FissionTrackCntrMarkerVector getCurrentMarkerVector() {
		return currentMarkerVector;
	}

	public void setCurrentMarkerVector(
		final FissionTrackCntrMarkerVector currentMarkerVector)
	{
		this.currentMarkerVector = currentMarkerVector;
	}

	public static void setType(final String type) {
		if (instance == null || instance.ic == null || type == null) return;
		final int index = Integer.parseInt(type) - 1;
		final int buttons = instance.dynRadioVector.size();
		if (index < 0 || index >= buttons) return;
		final JRadioButton rbutton = instance.dynRadioVector.elementAt(index);
		instance.radioGrp.setSelected(rbutton.getModel(), true);
		instance.currentMarkerVector = instance.typeVector.get(index);
		instance.ic.setCurrentMarkerVector(instance.currentMarkerVector);
	}

	public Color getColor(int type) {
		final Context c = (Context) IJ.runPlugIn("org.scijava.Context", "");
		final OptionsService optionsService = c.service(OptionsService.class);

		return options.getColor(type);
	}
	public void changeColor(int type){
		options.setColor(type);
	}
	public void initializeColor() {
		options.initializeColor();
	}
	public Vector<FissionTrackCntrMarkerVector> getMarkerVector() {return this.typeVector;}
	public void setMarkerVector(int index) {this.typeVector = this.allTypeVector.get(index);}
	public ArrayList<Vector<FissionTrackCntrMarkerVector>> getAllTypeVector() {return  this.allTypeVector;}
	public void setAllTypeVector(Vector<FissionTrackCntrMarkerVector> vector, int index) {
		allTypeVector.set(index, vector);
	}
	public void setStoredRoi(int index) {storedRoi = storedRoiList.get(index);}
	public void setCounterImg(int index) {counterImg = counterImgList.get(index);}
	public void setIc(int index) {ic = icList.get(index);}

	public ImagePlus getImg(int index) {return counterImgList.get(index);}

}
