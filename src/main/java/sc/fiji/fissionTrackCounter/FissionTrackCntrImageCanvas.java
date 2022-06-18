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

package sc.fiji.fissionTrackCounter;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
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
import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.sqrt;
import static java.lang.Math.acos;


/**
 * TODO
 *
 * @author Kurt De Vos
 */
public class FissionTrackCntrImageCanvas extends ImageCanvas {

	private Vector<FissionTrackCntrMarkerVector> typeVector;
	private FissionTrackCntrMarkerVector currentMarkerVector;
	private int mountNum = 0;
	private int micaNum = 0;
	private final FissionTrackCounter cc;
	private final ImagePlus img;
	private boolean delmode = false;
	private boolean roimode = false;
	private boolean showNumbers = true;
	private boolean showAll = false;
	private final Font font = new Font("SansSerif", Font.PLAIN, 10);
	private double cAngle = -1;

	/** Creates a new instance of FissionTrackCntrImageCanvas */
	public FissionTrackCntrImageCanvas(final ImagePlus img, final Vector<FissionTrackCntrMarkerVector> typeVector,
									   final FissionTrackCounter cc, Overlay displayList)
	{
		super(img);
		this.img = img;
		this.typeVector = typeVector;
		this.cc = cc;
		//if (displayList != null) this.setDisplayList(displayList);
		if (displayList != null) this.setOverlay(displayList);
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (IJ.spaceBarDown() || Toolbar.getToolId() == Toolbar.MAGNIFIER ||
				Toolbar.getToolId() == Toolbar.HAND) {
			super.mousePressed(e);
			return;
		}

		if (currentMarkerVector == null) {
			IJ.error("Select a counter type first!");
			return;
		}

		final int x = super.offScreenX(e.getX());
		final int y = super.offScreenY(e.getY());
		Overlay overlay = img.getOverlay();
		Roi roi = overlay.get(0);
		if (!roimode) {
			if (!delmode) {
				if (roi!=null && !roi.containsPoint(x, y)) {
					IJ.error("The point is outside of ROI");
				} else {
					//Add one marker on screen
					if( currentMarkerVector.getType() != 5 || currentMarkerVector.getcAxis()) {
						System.out.println(currentMarkerVector.getcAxis());
						final FissionTrackCntrMarker m = new FissionTrackCntrMarker(x, y, img.getCurrentSlice());
						currentMarkerVector.addMarker(m);
					}

				}
			} else {

				Point p = new Point(x, y);
				FissionTrackCntrMarker currentsmallest = currentMarkerVector.getMarkerFromPosition(new Point(x, y), 1);

				for (int i = 1; i <= img.getStackSize(); i++) {
					FissionTrackCntrMarker m =
							currentMarkerVector.getMarkerFromPosition(new Point(x, y), i);
					if (m == null) {
						continue;
					}
					if (currentsmallest == null) {
						currentsmallest = m;
					}
					final Point p1 =
							new Point(currentsmallest.getX(), currentsmallest.getY());
					final Point p2 = new Point(m.getX(), m.getY());
					final boolean closer =
							Math.abs(p1.distance(p)) > Math.abs(p2.distance(p));
					if (closer) {
						currentsmallest = m;
					}
				}

				currentMarkerVector.remove(currentsmallest);
				currentMarkerVector.redNum();



				//Reduce variable mCount in FissonTrackCntrMarker
			}
			repaint();
			cc.populateTxtFields();
		}
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
		if (roimode && !delmode) {
			Overlay overlay = img.getOverlay();
			Roi roi = overlay.get(0);
			roi.setLocation(e.getX(), e.getY());
			roi.mouseDragged(e);
		}
		repaint();
		cc.populateTxtFields();
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

		final ListIterator<FissionTrackCntrMarkerVector> it = typeVector.listIterator();
		while (it.hasNext()) {
			final FissionTrackCntrMarkerVector mv = it.next();
			final int typeID = mv.getType();
			g2.setColor(mv.getColor());
			final ListIterator<FissionTrackCntrMarker> mit = mv.listIterator();
			boolean flag = false;
			//flag for c-axis
			boolean cflag = false;
			double x = 0;
			double y = 0;
			int count = 0;
			while (mit.hasNext()) {
				count ++;
				final FissionTrackCntrMarker m = mit.next();
				if (true || showAll) {
					xM = ((m.getX() - srcRect.x) * magnification);
					yM = ((m.getY() - srcRect.y) * magnification);

				}
				//2D and 3D distances
				if (typeID == 3 || typeID == 4 ) {
					if (flag) {
						g2.drawLine((int) x, (int) y, (int) xM, (int) yM);
						flag = false;
					} else {
						x = xM;
						y = yM;
						flag = true;
					}
					g2.fillOval((int) xM , (int) yM, 1, 1);
					g2.drawOval((int) xM , (int) yM, 1, 1);
				}
				//Mount/Micro Count
				else if (typeID == 1 || typeID == 2 ){
					g2.fillOval((int) xM - 2, (int) yM - 2, 4, 4);
					g2.drawOval((int) xM - 2, (int) yM - 2, 4, 4);


					System.out.println(typeID + ",");
					//Show the count number of the
					if (showNumbers) g2.drawString(Integer.toString(count),
							(int) xM + 3, (int) yM - 3);
				}
				//C-Axis
				else if (typeID == 5) {
					System.out.println(cflag);
					if (cflag) {
						g2.drawLine((int) x, (int) y, (int) xM, (int) yM);
						cAngle = 90 - atan(Math.abs(yM - y)/ Math.abs(xM - x))* 180 / PI;
						cflag = false;
						mv.setcAxis(false);
					} else {
						x = xM;
						y = yM;
						cflag = true;
					}

					g2.fillOval((int) xM , (int) yM, 1, 1);
					g2.drawOval((int) xM , (int) yM, 1, 1);


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

		final ListIterator<FissionTrackCntrMarkerVector> it = typeVector.listIterator();
		while (it.hasNext()) {
			final FissionTrackCntrMarkerVector mv = it.next();
			final int typeID = mv.getType();
			g2r.setColor(mv.getColor());
			final ListIterator<FissionTrackCntrMarker> mit = mv.listIterator();
			while (mit.hasNext()) {
				final FissionTrackCntrMarker m = mit.next();
				if (m.getZ() == img.getCurrentSlice()) {
					xM = m.getX();
					yM = m.getY();
					g2r.fillOval((int) xM - 2, (int) yM - 2, 4, 4);
					//Show the number of mount
					if (showNumbers) g2r.drawString(Integer.toString(typeID == 1? mountNum : micaNum),(int) xM + 3, (int) yM - 3 );

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

			final ListIterator<FissionTrackCntrMarkerVector> it = typeVector.listIterator();
			while (it.hasNext()) {
				final FissionTrackCntrMarkerVector mv = it.next();
				final int typeID = mv.getType();
				final ListIterator<FissionTrackCntrMarker> mit = mv.listIterator();
				while (mit.hasNext()) {
					final FissionTrackCntrMarker m = mit.next();
					if (m.getZ() == i && typeID <= 2) {
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

						String resultsRow = String.format("%s\t%d\t%d\t%d\t%f\t%d\t%d\t%d\t%.3f\t%.3f\t%.3f",typeID == 2?"Mica":"Mount",zM,yM,xM,value,channel,zPos,frame,xMcal,yMcal,zMcal);
						IJ.write(resultsRow);
						//IJ.write(typeID + "\t" + zM + "\t" + xM + "\t" + yM + "\t" + value + "\t" + channel + "\t" + zPos + "\t" + frame + "\t" + xMcal + "\t" + yMcal + "\t" +zMcal);

					}
				}
			}
		}
	}

	public void measure2() {
		Calibration cal = img.getCalibration();	
		String unit = cal.getUnit();
		String columnHeadings = String.format("Type\tDistance\tX(%s)\tY(%s)\tZ(%s)\t2D-Angle\tC-axis Angle",unit,unit,unit,unit);
		IJ.setColumnHeadings(columnHeadings);
		
		
		for (int i = 1; i <= img.getStackSize(); i++) {
			img.setSlice(i);
			final ImageProcessor ip = img.getProcessor();
			
			final ListIterator<FissionTrackCntrMarkerVector> it = typeVector.listIterator();
			it.next();
			it.next();
			while (it.hasNext()) {
				final FissionTrackCntrMarkerVector mv = it.next();
				final int typeID = mv.getType();
				System.out.println(typeID);
				if(typeID <= 4){
					final ListIterator<FissionTrackCntrMarker> mit = mv.listIterator();
					boolean flag = false;
					double x = 0;
					double y = 0;
					double z = 0;
					while (mit.hasNext()) {
						String type = typeID == 3 ? "2D": "3D";
						final FissionTrackCntrMarker m = mit.next();
						if (m.getZ() == i) {
							final int xM = m.getX();
							final int yM = m.getY();
							final int zM = m.getZ();
							final double value = ip.getPixelValue(xM, yM);

							//angle between vertical line (90degree) and the drawn line
							double vAngle = -1;
							//angle between drawn c-axis and drawn line


							int[] realPosArray = img.convertIndexToPosition(zM); // from the slice we get the array  [channel, slice, frame]
							final int channel 	= realPosArray[0];
							final int zPos		= realPosArray[1];
							final int frame 	= realPosArray[2];
							final double xMcal 	= xM * cal.pixelWidth ;
							final double yMcal 	= yM * cal.pixelHeight;
							final double zMcal 	= (zPos-1) * cal.pixelDepth; 		// zPos instead of zM , start at 1 while should start at 0.
							if (flag) {
								flag = false;
								double temp = Math.pow(xMcal - x, 2) + Math.pow(yMcal - y,2);
								double distance = Math.sqrt(type == "2D" ? temp: temp + Math.pow(zMcal - z, 2));
								//Math equation to get value of funciton
								//*Need to assign final after check
								vAngle = 90 - atan(Math.abs(yMcal - y)/ Math.abs(xMcal - x))* 180 / PI;
								System.out.println(vAngle);
								String angle = type == "2D" ? String.format("%.2f",vAngle) : "NA";
								String C_axisAngle = type == "2D" ? String.format("%.2f",cAngle) : "NA";
								String resultsRow = String.format("%s\t%.3f\t%.3f\t%.3f\t%.3f\t%s\t%s",type,distance,xMcal,yMcal,zMcal,angle,C_axisAngle);
								IJ.write(resultsRow);
							} else {
								flag = true;
								x = xMcal;
								y = yMcal;
								z = zMcal;
							}

//						String resultsRow = String.format("%s\t%.3f\t%.3f\t%.3f\t%.3f",type,distance,xMcal,yMcal,zMcal);
//						IJ.write(resultsRow);
							//IJ.write(typeID + "\t" + zM + "\t" + xM + "\t" + yM + "\t" + value + "\t" + channel + "\t" + zPos + "\t" + frame + "\t" + xMcal + "\t" + yMcal + "\t" +zMcal);

						}
					}

				}


			}
		}
	}


	public Vector<FissionTrackCntrMarkerVector> getTypeVector() {
		return typeVector;
	}

	public void setTypeVector(final Vector<FissionTrackCntrMarkerVector> typeVector) {
		this.typeVector = typeVector;
	}

	public FissionTrackCntrMarkerVector getCurrentMarkerVector() {
		return currentMarkerVector;
	}

	public void setCurrentMarkerVector(
		final FissionTrackCntrMarkerVector currentMarkerVector)
	{
		this.currentMarkerVector = currentMarkerVector;
	}

	public boolean isDelmode() {
		return delmode;
	}
	public boolean isRoimode() {
		return roimode;
	}

	public void setDelmode(final boolean delmode) {
		this.delmode = delmode;
	}
	public void setRoimode(final boolean roimode) {
		this.roimode = roimode;
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