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

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ListIterator;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import org.scijava.Context;
import org.scijava.command.CommandService;

/**
 * TODO
 *
 * @author Kurt De Vos
 */
public class FissionTrackCounter extends JFrame implements ActionListener, ItemListener
{

	private static final String ADD = "Add";
	private static final String REMOVE = "Remove";
	private static final String RENAME = "Rename";
	private static final String INITIALIZE = "Initialize";
	private static final String OPTIONS = "Options";
	private static final String RESULTS = "Results";
	private static final String UNDO = "Undo";
	private static final String REDO = "Redo";
	private static final String DELMODE = "Delete Mode";
	private static final String UNIMODE = "Unique ID";
	private static final String KEEPORIGINAL = "Keep Original";
	private static final String SHOWNUMBERS = "Show Numbers";
	private static final String SHOWALL = "Show All";
	private static final String RESET = "Reset";
	private static final String RESETCAIXS = "ResetCAxis";

	private static final String EXPORTMARKERS = "Save Markers";
	private static final String LOADMARKERS = "Load Markers";
	private static final String EXPORTIMG = "Export Image";
	private static final String MEASURE = "Mount/Mica Count";

	private static final String MEASURE2 = "2D/3D Distances";
	private static final String ROIMODIFY = "Drag ROI";

	private static final String TYPE_COMMAND_PREFIX = "type";

	private Vector<FissionTrackCntrMarkerVector> typeVector;
	private Vector<JRadioButton> dynRadioVector;
	private final Vector<JTextField> txtFieldVector;
	private FissionTrackCntrMarkerVector markerVector;
	private FissionTrackCntrMarkerVector currentMarkerVector;
	private int currentMarkerIndex;
		
	// Map<key,value> for storing metadata to write with WriteXML
	private Map<String,String> metaData = new HashMap<>();

	private JPanel dynPanel;

	private JPanel dynButtonPanel;
	private JPanel statButtonPanel;
	private JPanel autoPanel;
	private JPanel autoButtonPanel;
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
	private JButton addButton;
	private JButton removeButton;
	private JButton renameButton;
	private JButton initializeButton;
//	private JButton optionsButton;
	private JButton resultsButton;
	private JButton unDoButton;
	private JButton reDoButton;
	private JButton resetButton;
	private JButton resetCButton;
	private JButton exportButton;
	private JButton loadButton;
	private JButton exportimgButton;
	private JButton measureButton;
	private JButton measure2Button;

	private boolean keepOriginal = false;

	private FissionTrackCntrImageCanvas ic;

	private ImagePlus img;
	private ImagePlus counterImg;

	private GridLayout dynGrid;

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
		autoPanel = new JPanel();
		autoPanel.setBorder(BorderFactory.createTitledBorder("Automatic"));
		autoPanel.setLayout(gb);

		// this panel keeps the radiobuttons
		autoButtonPanel = new JPanel();
		GridLayout grid = new GridLayout(2, 1);
		grid.setVgap(2);
		autoButtonPanel.setLayout(grid);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.anchor = GridBagConstraints.NORTHWEST;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.ipadx = 0;
		gbc1.gridy = 1;
		gb.setConstraints(autoPanel, gbc1);
		autoPanel.add(autoButtonPanel);

		autoButtonPanel.add(makeAutoRadioButton("AiTracktive"));
		autoButtonPanel.add(makeAutoRadioButton("Skeletracks"));
		getContentPane().add(autoPanel);




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

		// create a "static" panel to hold control buttons
		statButtonPanel = new JPanel();
		statButtonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
		statButtonPanel.setLayout(gb);

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
		uniqueID.setSelected(true);
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
		addButton = makeButton(ADD, "add a counter type");
		gb.setConstraints(addButton, gbc);
		//statButtonPanel.add(addButton); REMOVED THE ADD BUTTON

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		removeButton = makeButton(REMOVE, "remove last counter type");
		gb.setConstraints(removeButton, gbc);
		//statButtonPanel.add(removeButton); REMOVED THE REMOVE BUTTON

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		renameButton = makeButton(RENAME, "rename selected counter type");
		renameButton.setEnabled(false);
		gb.setConstraints(renameButton, gbc);
		//statButtonPanel.add(renameButton); REMOVED THE RENAME BUTTON

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
		reDoButton = makeButton(REDO,"delete last marker");
		reDoButton.setEnabled(false);
		gb.setConstraints(reDoButton, gbc);
		statButtonPanel.add(reDoButton);

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
		gbc.gridwidth = GridBagConstraints.REMAINDER;

//		optionsButton = makeButton(OPTIONS, "show options dialog");
//		gb.setConstraints(optionsButton, gbc);
//		statButtonPanel.add(optionsButton);

//		gbc = new GridBagConstraints();
//		gbc.anchor = GridBagConstraints.NORTHWEST;
//		gbc.fill = GridBagConstraints.BOTH;
//		gbc.gridx = 0;
//		gbc.gridwidth = GridBagConstraints.REMAINDER;
//		gbc.gridwidth = GridBagConstraints.REMAINDER;
//		resultsButton = makeButton(RESULTS, "show results in results table");
//		resultsButton.setEnabled(false);
//		gb.setConstraints(resultsButton, gbc);
//		statButtonPanel.add(resultsButton);



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

		measure2Button =
				makeButton(MEASURE2, "Measure pixel intensity of marker points");
		measure2Button.setEnabled(false);
		gb.setConstraints(measure2Button, gbc);
		statButtonPanel.add(measure2Button);

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
		resetCButton = makeButton(RESETCAIXS, "only reset c-axis");
		resetCButton.setEnabled(false);
		gb.setConstraints(resetCButton, gbc);
		statButtonPanel.add(resetCButton);

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
		gbc.insets = new Insets(3, 0, 3, 0);
		separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setPreferredSize(new Dimension(1, 1));
		gb.setConstraints(separator, gbc);
		statButtonPanel.add(separator);

//		gbc = new GridBagConstraints();
//		gbc.anchor = GridBagConstraints.NORTHWEST;
//		gbc.fill = GridBagConstraints.BOTH;
//		gbc.gridx = 0;
//		gbc.gridwidth = GridBagConstraints.REMAINDER;
//		measureButton =
//			makeButton(MEASURE, "Measure pixel intensity of marker points");
//		measureButton.setEnabled(false);
//		gb.setConstraints(measureButton, gbc);
//		statButtonPanel.add(measureButton);
//
//		measure2Button =
//				makeButton(MEASURE2, "Measure pixel intensity of marker points");
//		measure2Button.setEnabled(false);
//		gb.setConstraints(measure2Button, gbc);
//		statButtonPanel.add(measure2Button);

		gbc = new GridBagConstraints();
		//gbc.anchor = GridBagConstraints.NORTHWEST;
		//gbc.fill = GridBagConstraints.NONE;
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
				tArea.setText("" + count);
			}
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
		markerVector = new FissionTrackCntrMarkerVector(id,markerName);
		typeVector.add(markerVector);
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
		reset();
		img = WindowManager.getCurrentImage();
		final boolean v139t = IJ.getVersion().compareTo("1.39t") >= 0;
		if (img == null) {
			IJ.noImage();
		}
		else if (img.getStackSize() == 1) {


			ImageProcessor ip = img.getProcessor();
			//ip.resetRoi();

			if (keepOriginal) ip = ip.crop();
			//if(true) ip = ip.crop();
			counterImg = new ImagePlus("Counter Window - " + img.getTitle(), ip);

			@SuppressWarnings("unchecked")
			Overlay displayList;
			if (v139t) {
				displayList = new Overlay();
				Roi roi = img.getRoi();
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
				ImageProcessor ip = stack.getProcessor(i);
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
				Roi roi = img.getRoi();
				displayList.add(roi);
				displayList.setStrokeColor(Color.white);

			}
			else{
				displayList = null;
			}
			ic = new FissionTrackCntrImageCanvas(counterImg, typeVector, this, displayList);
			new StackWindow(counterImg, ic);
		}
		
		Calibration cal = img.getCalibration();	//	to conserve voxel size of the original image
		counterImg.setCalibration(cal);
		
		// Extracting calibration data to write to XML
		metaData.put("X_Calibration", "" + cal.pixelWidth);
		metaData.put("Y_Calibration", "" + cal.pixelHeight);
		metaData.put("Z_Calibration", "" + cal.pixelDepth);
		metaData.put("Calibration_Unit", "" + cal.getUnit());
		
		if (!keepOriginal) {
			img.changes = false;
			img.close();
		}
		delCheck.setEnabled(true);
		roiCheck.setEnabled(true);
		numbersCheck.setEnabled(true);
		showAllCheck.setSelected(false);
		uniqueID.setSelected(true);
		delCheck.setSelected(false);
		if (counterImg.getStackSize() > 1) showAllCheck.setEnabled(true);
		addButton.setEnabled(true);
		removeButton.setEnabled(true);
		renameButton.setEnabled(true);
//		resultsButton.setEnabled(true);
		unDoButton.setEnabled(true);
		resetButton.setEnabled(true);
		resetCButton.setEnabled(true);
		exportButton.setEnabled(true);
		exportimgButton.setEnabled(true);
		measureButton.setEnabled(true);
		measure2Button.setEnabled(true);

	}

	void validateLayout() {
		dynPanel.validate();
		dynButtonPanel.validate();
		autoPanel.validate();
		autoButtonPanel.validate();
		dynTxtPanel.validate();
		statButtonPanel.validate();
		validate();
		pack();
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();

		if (command.equals(ADD)) {
			final int i = dynRadioVector.size() + 1;
			dynGrid.setRows(i);
			dynButtonPanel.add(makeDynRadioButton(i));
			validateLayout();

			if (ic != null) ic.setTypeVector(typeVector);
		}
		else if (command.equals(REMOVE)) {
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
		else if (command.startsWith(TYPE_COMMAND_PREFIX)) { // COUNT
			currentMarkerIndex =
				Integer.parseInt(command.substring(TYPE_COMMAND_PREFIX.length())) - 1;
			//System.out.println(currentMarkerIndex);
			if (ic == null) {
				IJ.error("You need to initialize first");
				return;
			}
			// ic.setDelmode(false); // just in case
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
//		else if (command.equals(OPTIONS)) {
//			options();
//		}
		else if (command.equals(RESULTS)) {
			report();
		}
		else if (command.equals(EXPORTMARKERS)) {
			exportMarkers();
		}
		else if (command.equals(LOADMARKERS)) {
			if (ic == null) initializeImage();
			loadMarkers();
			validateLayout();
		}
		else if (command.equals(EXPORTIMG)) {
			ic.imageWithMarkers().show();
		}
		else if (command.equals(MEASURE)) {
			measure();
		} else if (command.equals(MEASURE2)) {
			measure2();
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

	public void measure2() {
		ic.measure2();
	}

 	public void reset() {

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
				mv.resetCAxis();
				mv.clear();
//				mv.resetNum();
			}
		}
		if (ic != null) ic.repaint();

	}

//	public void options() {
//		final Context c = (Context) IJ.runPlugIn("org.scijava.Context", "");
//		final CommandService commandService = c.service(CommandService.class);
//		commandService.run(FissionTrackCounterOptions.class, true);
//	}

	public void report() {
		String labels = "Slice\t";
		final boolean isStack = counterImg.getStackSize() > 1;
		// add the types according to the button vector!!!!
		final ListIterator<JRadioButton> it = dynRadioVector.listIterator();
		while (it.hasNext()) {
			final JRadioButton button = it.next();
			final String str = button.getText(); // System.out.println(str);
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
		if (storedfilename.equals(img.getTitle())) {
			final Vector<FissionTrackCntrMarkerVector> loadedvector = rxml.readMarkerData();
			typeVector = loadedvector;
			ic.setTypeVector(typeVector);
			final int index =
				Integer.parseInt(rxml.readImgProperties(ReadXML.CURRENT_TYPE));
			currentMarkerVector = typeVector.get(index);
			ic.setCurrentMarkerVector(currentMarkerVector);

			while (dynRadioVector.size() > typeVector.size()) {
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
			}
			
			// Adds radio buttons if too few initialized.
			while (dynRadioVector.size() < typeVector.size()) {
				final int i = dynRadioVector.size() + 1;
				dynGrid.setRows(i);
				final JRadioButton jrButton = new JRadioButton("Type " + i);
				jrButton.setActionCommand(TYPE_COMMAND_PREFIX + i);
				jrButton.addActionListener(this);
				dynRadioVector.add(jrButton);
				radioGrp.add(jrButton);
				dynTxtPanel.add(makeDynamicTextArea());
				dynButtonPanel.add(jrButton);
				typeVector.get(index);
				validateLayout();
			}
			
			// Sets radio button names to loaded names
			final ListIterator<FissionTrackCntrMarkerVector> it = typeVector.listIterator();
			while (it.hasNext()) {
				final int i = it.nextIndex();
				final FissionTrackCntrMarkerVector markerVector = it.next();
				final String name = markerVector.getName();
				final JRadioButton button = dynRadioVector.get(i);
				radioGrp.remove(button);
				button.setText(name);
				radioGrp.add(button);
			}
			
			final JRadioButton butt = dynRadioVector.get(index);
			butt.setSelected(true);
		}
		else {
			IJ.error("These Markers do not belong to the current image");
		}
	}

	public void exportMarkers() {
		String filePath =
			getFilePath(new JFrame(), "Save Marker File (.xml)", SAVE);
		if (!filePath.endsWith(".xml")) filePath += ".xml";
		final WriteXML wxml = new WriteXML(filePath);
		wxml.writeXML(img.getTitle(), typeVector, typeVector
			.indexOf(currentMarkerVector), metaData);
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
				final String filename = img.getTitle();
				fd.setFile("CellCounter_" +
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

}
