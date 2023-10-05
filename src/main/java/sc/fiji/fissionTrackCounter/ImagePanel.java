package sc.fiji.fissionTrackCounter;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

/**
 * @author Ziya Ye
 */
public class ImagePanel extends JPanel implements ActionListener {

    private JPanel radioPanel;
    private ButtonGroup group;
    private ArrayList<JRadioButton> buttons = new ArrayList<>();
    private FissionTrackCounter counter;

    public ImagePanel(FissionTrackCounter counter) {
        this.counter = counter;
        radioPanel = new JPanel();
        group = new ButtonGroup();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));

        setLayout(new BorderLayout());
        add(radioPanel, BorderLayout.NORTH);
    }

    public void addButton(String name) {
        JRadioButton button = new JRadioButton(name);
        button.addActionListener(this);
        radioPanel.add(button);
        group.add(button);
        buttons.add(button);
        radioPanel.revalidate();
        radioPanel.repaint();
        button.setSelected(true);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        int index = buttons.indexOf(event.getSource()); // get the index of the selected button
        counter.setMarkerVector(index);
        counter.setStoredRoi(index);
        counter.setCounterImg(index);
        counter.setIc(index);
        ImagePlus img = counter.getImg(index);
        Window imgWindow = img.getWindow();
        imgWindow.toFront();
        counter.populateTxtFields();

    }

    public void setCounter(FissionTrackCounter counter) {
        this.counter = counter;
    }
    public void reset() {
        group.clearSelection();
        buttons.clear();
        radioPanel.removeAll();
    }

    public JPanel getRadioPanel() {
        return radioPanel;
    }

    public ButtonGroup getGroup() {
        return group;
    }
}