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

// Created on December 27, 2005, 4:56 PM

package sc.fiji.fissionTrackCounter;

import ij.plugin.frame.PlugInFrame;

/**
 * @author Kurt De Vos
 * @author Ziya Ye
 */
public class Fission_Track_Counter extends PlugInFrame {

	/** Creates a new instance of Fission_Track_Counter */
	public Fission_Track_Counter() {
		super("Fission Track Counter");
		new FissionTrackCounter();
	}

	@Override
	public void run(final String arg) {}


//	public Fission_Track_Counter() {
//		super("Fission Track Counter");
//
//		// Create instances of the FissionTrackCounter and ImagePanel classes
//		FissionTrackCounter counterFrame = new FissionTrackCounter();
//		ImagePanel imageFrame = new ImagePanel();
//
//		// Show the frames
//		counterFrame.setVisible(true);
//		imageFrame.setVisible(true);
//	}
//
//	@Override
//	public void run(final String arg) {}
//
//	public static void main(String[] args) {
//		new Fission_Track_Counter();
//	}

}