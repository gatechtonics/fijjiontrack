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

import javax.swing.*;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Options for the Cell Counter plugin.
 *
 * @author Curtis Rueden
 * @author Ziya Ye
 *
 */
@Plugin(type = OptionsPlugin.class, label = "Fission Track Counter Options",
	attrs = { @Attr(name = "legacy-only") })
public class FissionTrackCounterOptions extends JFrame{

	// -- Fields --

	@Parameter
	private Color color1;

	@Parameter
	private Color color2;

	@Parameter
	private Color color3;

	@Parameter
	private Color color4;

	@Parameter
	private Color color5;

	@Parameter
	private Color color6;

	@Parameter
	private Color color7;
	private boolean init;

	@Parameter
	private Color color8;
	private JColorChooser tcc = new JColorChooser();

	// -- Option accessors --

	public Color getColor(final int id) {

		switch (id) {
			case 1:
				return color1;
			case 2:
				return color2;
			case 3:
				return color3;
			case 4:
				return color4;
			case 5:
				return color5;
			case 6:
				return color6;
			case 7:
				return color7;
			case 8:
				return color8;
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

	public void initializeColor(){
		color1 = AWTColors.getColor(Colors.CYAN);
		color2 = AWTColors.getColor(Colors.BLUE);
		color3 = AWTColors.getColor(Colors.ROYALBLUE);
		color4 = AWTColors.getColor(Colors.LIGHTGREEN);
		color5 = AWTColors.getColor(Colors.ORANGE);
		color6 = AWTColors.getColor(Colors.PINK);
		color7 = AWTColors.getColor(Colors.RED);
		color8 = AWTColors.getColor(Colors.YELLOW);


	}
	public void setColor(final int id) {
		Color initialColor = AWTColors.getColor(Colors.CYAN);
		Color color=JColorChooser.showDialog(this,"Select a color",initialColor);

		switch (id) {
			case 1:
				this.color1 = color;
				break;
			case 2:
				this.color2 = color;
				break;
			case 3:
				this.color3 = color;
				break;
			case 4:
				this.color4 = color;
				break;
			case 5:
				this.color5 = color;
				break;
		}

	}


}
