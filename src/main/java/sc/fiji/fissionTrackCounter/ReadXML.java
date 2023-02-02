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

// Created on 27 November 2004, 10:47

package sc.fiji.fissionTrackCounter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ij.gui.Roi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * TODO
 *
 * @author Kurt De Vos
 */
public class ReadXML {

	private boolean verbose;
	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private Document doc;
	private String str;
	public static final int IMAGE_FILE_PATH = 0;
	public static final int CURRENT_TYPE = 1;
	/**
	 * Creates a new instance of ODReadXMLODD
	 */
	public ReadXML(final String XMLFilePath) {
		setVerbose(verbose);
		try {
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
			doc = db.parse(new File(XMLFilePath));
			doc.getDocumentElement().normalize();
		}
		catch (final SAXException e) {
			System.out.println(e.getMessage());
			System.out.println(XMLFilePath + " is not well-formed.");
		}
		catch (final IOException e) {
			System.out.println("IOException " + e.getMessage());
		}
		catch (final ParserConfigurationException e) {
			System.out.println("ParserConfigurationException " + e.getMessage());
		}
	}

	public String readImgProperties(final int valueID) { // as URL
		switch (valueID) {
			case (IMAGE_FILE_PATH):
				str = readSingleValue(doc, "Image_Filename");
				break;
			case (CURRENT_TYPE):
				str = readSingleValue(doc, "Current_Type");
				break;
		}
		if (str != null) {
			return str;
		}
		return null;
	}
	public Roi readRoi() throws IOException {
		// Read the binary ROI data from the XML file
		String binaryRoiDataString = getNodeListFromTag(doc, "ROI").item(0).getTextContent();
		byte[] binaryRoiData = DatatypeConverter.parseBase64Binary(binaryRoiDataString);

		// Convert the binary ROI data to an ROI object
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(binaryRoiData);
		ObjectInputStream in = new ObjectInputStream(byteArrayInputStream);
		Roi roi = null;
		try {
			roi = (Roi) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return roi;

	}

	public Vector<FissionTrackCntrMarkerVector> readMarkerData() {
		final Vector<FissionTrackCntrMarkerVector> typeVector =
			new Vector<FissionTrackCntrMarkerVector>();
		String markerName = "";

		final NodeList markerTypeNodeList = getNodeListFromTag(doc, "Marker_Type");
		for (int i = 0; i < markerTypeNodeList.getLength(); i++) {
			final Element markerTypeElement = getElement(markerTypeNodeList, i);
			final NodeList typeNodeList =
				markerTypeElement.getElementsByTagName("Type");
			// Reads type name, with bypass for older format.
			final NodeList nameNodeList = 
				markerTypeElement.getElementsByTagName("Name");
			markerName = ("Type " + Integer.parseInt(readValue(typeNodeList, 0)));
			if(nameNodeList.getLength() > 0) {
				markerName = readValue(nameNodeList, 0);
			}
			final FissionTrackCntrMarkerVector markerVector =
					new FissionTrackCntrMarkerVector(Integer.parseInt(readValue(typeNodeList, 0)), markerName);
			final NodeList markerNodeList =
				markerTypeElement.getElementsByTagName("Marker");
			for (int j = 0; j < markerNodeList.getLength(); j++) {
				final Element markerElement = getElement(markerNodeList, j);
				final NodeList markerXNodeList =
					markerElement.getElementsByTagName("MarkerX");
				final NodeList markerYNodeList =
					markerElement.getElementsByTagName("MarkerY");
				final NodeList markerZNodeList =
					markerElement.getElementsByTagName("MarkerZ");
				final NodeList markerIDNodeList = markerElement.getElementsByTagName("MarkerID");
				final FissionTrackCntrMarker marker = new FissionTrackCntrMarker();
				marker.setX(Integer.parseInt(readValue(markerXNodeList, 0)));
				marker.setY(Integer.parseInt(readValue(markerYNodeList, 0)));
				marker.setZ(Integer.parseInt(readValue(markerZNodeList, 0)));
				marker.setID(Integer.parseInt(readValue(markerIDNodeList, 0)));
				markerVector.addMarker(marker);
			}
			typeVector.add(markerVector);
		}
		return typeVector;
	}

	private String readValue(final NodeList nodeList, final int index)
		throws NullPointerException
	{
		final Element element = getElement(nodeList, index);
		debugReport("Element = " + element.getNodeName());
		final NodeList elementNodeList = getChildNodes(element);
		final String str = getValue(elementNodeList, 0);
		return str;
	}

	private String readSingleValue(final Document doc, final String elementName) {
		NodeList nodeList = getNodeListFromTag(doc, elementName);
		final Element element = getElement(nodeList, 0);
		nodeList = getChildNodes(element);
		final String str = getValue(nodeList, 0);
		return str;
	}

	private NodeList getNodeListFromTag(final Document doc,
		final String elementName)
	{
		final NodeList nodeList = doc.getElementsByTagName(elementName);
		return nodeList;
	}

	private NodeList getChildNodes(final Element element) {
		final NodeList nodeList = element.getChildNodes();
		return nodeList;
	}

	private Element getElement(final NodeList nodeList, final int index) {
		final Element element = (Element) nodeList.item(index);
		return element;
	}

	private String getValue(final NodeList nodeList, final int index) {
		final String str = nodeList.item(index).getNodeValue().trim();
		return str;
	}

	public void debugReport(final String report) {
		if (verbose) System.out.println(report);
	}

	public void setVerbose(final boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isVerbose() {
		return verbose;
	}
}
