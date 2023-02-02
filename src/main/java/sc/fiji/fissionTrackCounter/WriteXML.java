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

// Created on 23 November 2004, 22:56

package sc.fiji.fissionTrackCounter;

import ij.gui.Roi;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map;
import java.util.Set;

/**
 * TODO
 *
 * @author Kurt De Vos
 */
public class WriteXML {

	private OutputStream XMLFileOut;
	private OutputStream XMLBuffOut;
	private OutputStreamWriter out;

	/**
	 * Creates a new instance of ODWriteXMLODD
	 */
	public WriteXML(final String XMLFilepath) {
		try {
			XMLFileOut = new FileOutputStream(XMLFilepath); // add FilePath
			XMLBuffOut = new BufferedOutputStream(XMLFileOut);
			out = new OutputStreamWriter(XMLBuffOut, "UTF-8");
		}
		catch (final FileNotFoundException e) {
			System.out.println("File Not Found " + e.getMessage());
		}
		catch (final UnsupportedEncodingException e) {
			System.out.println("This VM does not support the UTF-8 character set. " +
				e.getMessage());
		}
	}

	public boolean writeXML(final String imgFilename,
							final Vector<FissionTrackCntrMarkerVector> typeVector,
							final int currentType,
							final Map<String,String> metaData, final Roi roi)
	{
		try {
			out.write("<?xml version=\"1.0\" ");
			out.write("encoding=\"UTF-8\"?>\r\n");
			out.write("<FissionTrackCounter_Marker_File>\r\n");

			// write the image properties
			out.write(" <Image_Properties>\r\n");
			out.write("     <Image_Filename>" + imgFilename + "</Image_Filename>\r\n");
			// add further metadata to properties
			final Set metaDataSet = metaData.entrySet();
			final Iterator iterator = metaDataSet.iterator();
			while(iterator.hasNext()) {
				Map.Entry entry = (Map.Entry)iterator.next();
				final String key = (entry.getKey()).toString();
				final String value = (entry.getValue()).toString();
				out.write("     <" + key + ">" + value + "</" + key + ">\r\n");
			}
			out.write(" </Image_Properties>\r\n");
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream temp = new ObjectOutputStream(byteArrayOutputStream);
			temp.writeObject(roi);
			temp.close();
			byte[] binaryRoiData = byteArrayOutputStream.toByteArray();

			// Write the binary ROI data to the XML file
			String binaryRoiDataString = DatatypeConverter.printBase64Binary(binaryRoiData);
			out.write("<ROI>" + binaryRoiDataString + "</ROI>\r\n");

			// write the marker data
			out.write(" <Marker_Data>\r\n");
			out.write("     <Current_Type>" + currentType + "</Current_Type>\r\n");
			final ListIterator<FissionTrackCntrMarkerVector> it = typeVector.listIterator();

			while (it.hasNext()) {
				final FissionTrackCntrMarkerVector markerVector = it.next();
				final int type = markerVector.getType();
				final String name = markerVector.getName();
				out.write("     <Marker_Type>\r\n");
				out.write("         <Type>" + type + "</Type>\r\n");
				out.write("         <Name>" + name + "</Name>\r\n");
				final ListIterator<FissionTrackCntrMarker> lit = markerVector.listIterator();
				while (lit.hasNext()) {
					final FissionTrackCntrMarker marker = lit.next();
					final int x = marker.getX();
					final int y = marker.getY();
					final int z = marker.getZ();
					final int ID = marker.getID();
					out.write("         <Marker>\r\n");
					out.write("             <MarkerX>" + x + "</MarkerX>\r\n");
					out.write("             <MarkerY>" + y + "</MarkerY>\r\n");
					out.write("             <MarkerZ>" + z + "</MarkerZ>\r\n");
					out.write("				<MarkerID>" + ID + "</MarkerID>\r\n");
					out.write("         </Marker>\r\n");
				}
				out.write("     </Marker_Type>\r\n");
			}

			out.write(" </Marker_Data>\r\n");
			out.write("</FissionTrackCounter_Marker_File>\r\n");

			out.flush(); // Don't forget to flush!
			out.close();
			return true;
		}
		catch (final IOException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

}
