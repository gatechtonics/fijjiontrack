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

import java.awt.Color;

import org.scijava.options.OptionsPlugin;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.awt.AWTColors;
import org.scijava.util.ColorRGB;
import org.scijava.util.Colors;

/**
 * Options for the Cell Counter plugin.
 *
 * @author Curtis Rueden
 */
@Plugin(type = OptionsPlugin.class, label = "Cell Counter Options",
	attrs = { @Attr(name = "legacy-only") })
public class FissionTrackCounterOptions extends OptionsPlugin {

	// -- Fields --

	@Parameter
	private ColorRGB color1 = Colors.CYAN;

	@Parameter
	private ColorRGB color2 = Colors.RED;

	@Parameter
	private ColorRGB color3 = Colors.ROYALBLUE;

	@Parameter
	private ColorRGB color4 = Colors.LIGHTGREEN;

	@Parameter
	private ColorRGB color5 = Colors.ORANGE;

	@Parameter
	private ColorRGB color6 = Colors.PINK;

	@Parameter
	private ColorRGB color7 = Colors.RED;

	@Parameter
	private ColorRGB color8 = Colors.YELLOW;

	// -- Option accessors --

	public Color getColor(final int id) {
		switch (id) {
			case 1:
				return AWTColors.getColor(Colors.CYAN);
			case 2:
				return AWTColors.getColor(Colors.RED);
			case 3:
				return AWTColors.getColor(Colors.ROYALBLUE);
			case 4:
				return AWTColors.getColor(Colors.LIGHTGREEN);
			case 5:
				return AWTColors.getColor(color5);
			case 6:
				return AWTColors.getColor(color6);
			case 7:
				return AWTColors.getColor(color7);
			case 8:
				return AWTColors.getColor(color8);
			default:
				Color c;
				do {
					final int r = (int) (255 * Math.random());
					final int g = (int) (255 * Math.random());
					final int b = (int) (255 * Math.random());
					c = new Color(r, g, b);
				}
				while (c.equals(Color.blue) || //
					c.equals(Color.cyan) || //
					c.equals(Color.green) || //
					c.equals(Color.magenta) || //
					c.equals(Color.orange) || //
					c.equals(Color.pink) || //
					c.equals(Color.red) || //
					c.equals(Color.yellow));
				return c;
		}
	}

}
