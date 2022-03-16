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

// Created on November 22, 2005, 5:58 PM

package sc.fiji.cellCounter;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ListIterator;
import java.util.Vector;

/**
 * TODO
 *
 * @author Kurt De Vos
 */
public class CellCntrImageCanvas extends ImageCanvas {

	private Vector<CellCntrMarkerVector> typeVector;
	private CellCntrMarkerVector currentMarkerVector;
	private final CellCounter cc;
	private final ImagePlus img;
	private boolean delmode = false;
	private boolean showNumbers = true;
	private boolean showAll = false;
	private final Font font = new Font("SansSerif", Font.PLAIN, 10);

	/** Creates a new instance of CellCntrImageCanvas */
	public CellCntrImageCanvas(final ImagePlus img,
		final Vector<CellCntrMarkerVector> typeVector, final CellCounter cc,
		final Vector<Roi> displayList)
	{
		super(img);
		this.img = img;
		this.typeVector = typeVector;
		this.cc = cc;
		if (displayList != null) this.setDisplayList(displayList);
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (IJ.spaceBarDown() || Toolbar.getToolId() == Toolbar.MAGNIFIER ||
			Toolbar.getToolId() == Toolbar.HAND)
		{
			super.mousePressed(e);
			return;
		}

		if (currentMarkerVector == null) {
			IJ.error("Select a counter type first!");
			return;
		}

		final int x = super.offScreenX(e.getX());
		final int y = super.offScreenY(e.getY());
		if (!delmode) {
			final CellCntrMarker m = new CellCntrMarker(x, y, img.getCurrentSlice());
			currentMarkerVector.addMarker(m);
		}
		else {
			final CellCntrMarker m =
				currentMarkerVector.getMarkerFromPosition(new Point(x, y), img
					.getCurrentSlice());
			currentMarkerVector.remove(m);
		}
		repaint();
		cc.populateTxtFields();
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		super.mouseReleased(e);
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		super.mouseMoved(e);
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		super.mouseExited(e);
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		super.mouseEntered(e);
		if (!IJ.spaceBarDown() | Toolbar.getToolId() != Toolbar.MAGNIFIER |
			Toolbar.getToolId() != Toolbar.HAND) setCursor(Cursor
			.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		super.mouseDragged(e);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		super.mouseClicked(e);
	}

	private Rectangle srcRect = new Rectangle(0, 0, 0, 0);

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		srcRect = getSrcRect();
		double xM = 0;
		double yM = 0;

		final Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(1f));
		g2.setFont(font);

		final ListIterator<CellCntrMarkerVector> it = typeVector.listIterator();
		while (it.hasNext()) {
			final CellCntrMarkerVector mv = it.next();
			final int typeID = mv.getType();
			g2.setColor(mv.getColor());
			final ListIterator<CellCntrMarker> mit = mv.listIterator();
			while (mit.hasNext()) {
				final CellCntrMarker m = mit.next();
				final boolean sameSlice = m.getZ() == img.getCurrentSlice();
				if (sameSlice || showAll) {
					xM = ((m.getX() - srcRect.x) * magnification);
					yM = ((m.getY() - srcRect.y) * magnification);
					if (sameSlice) g2.fillOval((int) xM - 2, (int) yM - 2, 4, 4);
					else g2.drawOval((int) xM - 2, (int) yM - 2, 4, 4);
					if (showNumbers) g2.drawString(Integer.toString(typeID),
						(int) xM + 3, (int) yM - 3);
				}
			}
		}
	}

	public void removeLastMarker() {
		currentMarkerVector.removeLastMarker();
		repaint();
		cc.populateTxtFields();
	}

	public ImagePlus imageWithMarkers() {
		final Image image = this.createImage(img.getWidth(), img.getHeight());
		final Graphics gr = image.getGraphics();

		double xM = 0;
		double yM = 0;

		try {
			if (imageUpdated) {
				imageUpdated = false;
				img.updateImage();
			}
			final Image image2 = img.getImage();
			gr.drawImage(image2, 0, 0, img.getWidth(), img.getHeight(), null);
		}
		catch (final OutOfMemoryError e) {
			IJ.outOfMemory("Paint " + e.getMessage());
		}

		final Graphics2D g2r = (Graphics2D) gr;
		g2r.setStroke(new BasicStroke(1f));

		final ListIterator<CellCntrMarkerVector> it = typeVector.listIterator();
		while (it.hasNext()) {
			final CellCntrMarkerVector mv = it.next();
			final int typeID = mv.getType();
			g2r.setColor(mv.getColor());
			final ListIterator<CellCntrMarker> mit = mv.listIterator();
			while (mit.hasNext()) {
				final CellCntrMarker m = mit.next();
				if (m.getZ() == img.getCurrentSlice()) {
					xM = m.getX();
					yM = m.getY();
					g2r.fillOval((int) xM - 2, (int) yM - 2, 4, 4);
					if (showNumbers) g2r.drawString(Integer.toString(typeID),
						(int) xM + 3, (int) yM - 3);
				}
			}
		}

		@SuppressWarnings("unchecked")
		final Vector<Roi> displayList = getDisplayList();
		if (displayList != null && displayList.size() == 1) {
			final Roi roi = displayList.elementAt(0);
			if (roi.getType() == Roi.COMPOSITE) roi.draw(gr);
		}

		return new ImagePlus("Markers_" + img.getTitle(), image);
	}

	public void measure() {
		Calibration cal = img.getCalibration();	
		String unit = cal.getUnit();
		String columnHeadings = String.format("Type\tSlice\tX\tY\tValue\tC-pos\tZ-pos\tT-pos\tX(%s)\tY(%s)\tZ(%s)",unit,unit,unit);
		IJ.setColumnHeadings(columnHeadings);
		
		
		for (int i = 1; i <= img.getStackSize(); i++) {
			img.setSlice(i);
			final ImageProcessor ip = img.getProcessor();
			
			final ListIterator<CellCntrMarkerVector> it = typeVector.listIterator();
			while (it.hasNext()) {
				final CellCntrMarkerVector mv = it.next();
				final int typeID = mv.getType();
				final ListIterator<CellCntrMarker> mit = mv.listIterator();
				while (mit.hasNext()) {
					final CellCntrMarker m = mit.next();
					if (m.getZ() == i) {
						final int xM = m.getX();
						final int yM = m.getY();
						final int zM = m.getZ();
						final double value = ip.getPixelValue(xM, yM);
						
						int[] realPosArray = img.convertIndexToPosition(zM); // from the slice we get the array  [channel, slice, frame]
						final int channel 	= realPosArray[0];
						final int zPos		= realPosArray[1];
						final int frame 	= realPosArray[2];
						final double xMcal 	= xM * cal.pixelWidth ;
						final double yMcal 	= yM * cal.pixelHeight;
						final double zMcal 	= (zPos-1) * cal.pixelDepth; 		// zPos instead of zM , start at 1 while should start at 0.  
						
						String resultsRow = String.format("%d\t%d\t%d\t%d\t%f\t%d\t%d\t%d\t%.3f\t%.3f\t%.3f",typeID,zM,xM,yM,value,channel,zPos,frame,xMcal,yMcal,zMcal);
						IJ.write(resultsRow);
						//IJ.write(typeID + "\t" + zM + "\t" + xM + "\t" + yM + "\t" + value + "\t" + channel + "\t" + zPos + "\t" + frame + "\t" + xMcal + "\t" + yMcal + "\t" +zMcal);
						
					}
				}
			}
		}
	}

	public Vector<CellCntrMarkerVector> getTypeVector() {
		return typeVector;
	}

	public void setTypeVector(final Vector<CellCntrMarkerVector> typeVector) {
		this.typeVector = typeVector;
	}

	public CellCntrMarkerVector getCurrentMarkerVector() {
		return currentMarkerVector;
	}

	public void setCurrentMarkerVector(
		final CellCntrMarkerVector currentMarkerVector)
	{
		this.currentMarkerVector = currentMarkerVector;
	}

	public boolean isDelmode() {
		return delmode;
	}

	public void setDelmode(final boolean delmode) {
		this.delmode = delmode;
	}

	public boolean isShowNumbers() {
		return showNumbers;
	}

	public void setShowNumbers(final boolean showNumbers) {
		this.showNumbers = showNumbers;
	}

	public void setShowAll(final boolean showAll) {
		this.showAll = showAll;
	}

}
