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

// Created on December 13, 2005, 8:40 AM

package sc.fiji.fissionTrackCounter;

import ij.IJ;

import java.awt.Color;
import java.awt.Point;
import java.util.ListIterator;
import java.util.Vector;

import org.scijava.Context;
import org.scijava.options.OptionsService;

/**
 * TODO
 *
 * @author Kurt De Vos
 * @author Ziya Ye
 */
public class FissionTrackCntrMarkerVector extends Vector<FissionTrackCntrMarker> {

	private int type;
	private String name;
	private int num;//number of markers in the vector
	private int uniqueID;


	/** Creates a new instance of MarkerVector */
	public FissionTrackCntrMarkerVector(final int type) {
		this(type, "<Unnamed>");
	}

	/** Creates a new instance of MarkerVector */
	public FissionTrackCntrMarkerVector(final int type, final String name) {
		super();
		this.type = type;
		this.name = name;
		this.uniqueID = 0;
	}

	public void addMarker(final FissionTrackCntrMarker marker) {
		add(marker);
		uniqueID++;
	}

	public FissionTrackCntrMarker getMarker(final int n) {
		return get(n);
	}

	public int getVectorIndex(final FissionTrackCntrMarker marker) {
		return indexOf(marker);
	}

	public void removeMarker(final int n) {
		remove(n);
	}

	public void resetID(){
		this.uniqueID = 0;
	}

	public int getUniqueID() {return uniqueID;}

	public void removeLastMarker() {super.removeElementAt(size() - 1);}

	public FissionTrackCntrMarker getMarkerFromPosition(final Point p,
														final int sliceIndex)
	{
		final Vector<FissionTrackCntrMarker> v = new Vector<FissionTrackCntrMarker>();
		final ListIterator<FissionTrackCntrMarker> it = this.listIterator();
		while (it.hasNext()) {
			final FissionTrackCntrMarker m = it.next();
			if (m.getZ() == sliceIndex) {
				v.add(m);
			}
		}
		if(v.size() == 0) {
			return null;
		}
		FissionTrackCntrMarker currentsmallest = v.get(0);
		for (int i = 1; i < v.size(); i++) {
			final FissionTrackCntrMarker m2 = v.get(i);
			final Point p1 =
				new Point(currentsmallest.getX(), currentsmallest.getY());
			final Point p2 = new Point(m2.getX(), m2.getY());
			final boolean closer =
				Math.abs(p1.distance(p)) > Math.abs(p2.distance(p));
			if (closer) {
				currentsmallest = m2;
			}
		}

		return currentsmallest;
	}

	public int getType() {
		return type;
	}

	public void setType(final int type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}


}
