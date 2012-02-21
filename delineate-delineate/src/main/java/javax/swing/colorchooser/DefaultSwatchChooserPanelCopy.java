/*
 * @(#)DefaultSwatchChooserPanelCopy.java	1.24 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing.colorchooser;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;


/**
 * The standard color swatch chooser.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @author Steve Wilson modified by Robert McKinnon
 */
public class DefaultSwatchChooserPanelCopy extends AbstractColorChooserPanel {

    SwatchPanelCopy swatchPanel;
    RecentSwatchPanelCopy recentSwatchPanel;
    MouseListener mainSwatchListener;
    MouseListener recentSwatchListener;

    private static String recentStr = UIManager.getString("ColorChooser.swatchesRecentText");
    private Color[] colors;

    public DefaultSwatchChooserPanelCopy() {
        super();
    }

    public String getDisplayName() {
        return "Palette";
    }

    /**
     * Provides a hint to the look and feel as to the
     * <code>KeyEvent.VK</code> constant that can be used as a mnemonic to
     * access the panel. A return value <= 0 indicates there is no mnemonic.
     * <p>
     * The return value here is a hint, it is ultimately up to the look
     * and feel to honor the return value in some meaningful way.
     * <p>
     * This implementation looks up the value from the default
     * <code>ColorChooser.swatchesMnemonic</code>, or if it
     * isn't available (or not an <code>Integer</code>) returns -1.
     * The lookup for the default is done through the <code>UIManager</code>:
     * <code>UIManager.get("ColorChooser.swatchesMnemonic");</code>.
     *
     * @return KeyEvent.VK constant identifying the mnemonic; <= 0 for no
     *         mnemonic
     * @see #getDisplayedMnemonicIndex
     * @since 1.4
     */
    public int getMnemonic() {
        return KeyEvent.VK_P;
    }

    static int getInt(Object key, int defaultValue) {
        Object value = UIManager.get(key);

        if (value instanceof Integer) {
            return ((Integer)value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            } catch (NumberFormatException nfe) {}
        }
        return defaultValue;
    }

    /**
     * Provides a hint to the look and feel as to the index of the character in
     * <code>getDisplayName</code> that should be visually identified as the
     * mnemonic. The look and feel should only use this if
     * <code>getMnemonic</code> returns a value > 0.
     * <p>
     * The return value here is a hint, it is ultimately up to the look
     * and feel to honor the return value in some meaningful way. For example,
     * a look and feel may wish to render each
     * <code>AbstractColorChooserPanel</code> in a <code>JTabbedPane</code>,
     * and further use this return value to underline a character in
     * the <code>getDisplayName</code>.
     * <p>
     * This implementation looks up the value from the default
     * <code>ColorChooser.rgbDisplayedMnemonicIndex</code>, or if it
     * isn't available (or not an <code>Integer</code>) returns -1.
     * The lookup for the default is done through the <code>UIManager</code>:
     * <code>UIManager.get("ColorChooser.swatchesDisplayedMnemonicIndex");</code>.
     *
     * @return Character index to render mnemonic for; -1 to provide no
     *                   visual identifier for this panel.
     * @see #getMnemonic
     * @since 1.4
     */
    public int getDisplayedMnemonicIndex() {
        return getInt("ColorChooser.swatchesDisplayedMnemonicIndex", -1);
    }

    public Icon getSmallDisplayIcon() {
        return null;
    }

    public Icon getLargeDisplayIcon() {
        return null;
    }

    /**
     * The background color, foreground color, and font are already set to the
     * defaults from the defaults table before this method is called.
     */
    public void installChooserPanel(JColorChooser enclosingChooser) {
        super.installChooserPanel(enclosingChooser);
    }

    public void setColors(Color[] colors) {
        if(colors == null) {
            swatchPanel.initColors();
        } else {
            this.colors = colors;
            if(swatchPanel != null) {
                swatchPanel.setColors(colors);
            }
        }
    }

    protected void buildChooser() {

        JPanel superHolder = new JPanel(new BorderLayout());

        swatchPanel = new MainSwatchPanelCopy();
        swatchPanel.setColors(colors);
        swatchPanel.getAccessibleContext().setAccessibleName(getDisplayName());

        recentSwatchPanel = new RecentSwatchPanelCopy();
        recentSwatchPanel.getAccessibleContext().setAccessibleName(recentStr);

        mainSwatchListener = new MainSwatchListener();
        swatchPanel.addMouseListener(mainSwatchListener);
        recentSwatchListener = new RecentSwatchListener();
        recentSwatchPanel.addMouseListener(recentSwatchListener);


        JPanel mainHolder = new JPanel(new BorderLayout());
        Border border = new CompoundBorder(new LineBorder(Color.black),
            new LineBorder(Color.white));
        mainHolder.setBorder(border);
        mainHolder.add(swatchPanel, BorderLayout.CENTER);
        superHolder.add(mainHolder, BorderLayout.CENTER);

        JPanel recentHolder = new JPanel(new BorderLayout());
        recentSwatchPanel.addMouseListener(recentSwatchListener);
        recentHolder.setBorder(border);
        recentHolder.add(recentSwatchPanel, BorderLayout.CENTER);
        JPanel recentLabelHolder = new JPanel(new BorderLayout());
        recentLabelHolder.add(recentHolder, BorderLayout.CENTER);
        JLabel l = new JLabel(recentStr);
        l.setLabelFor(recentSwatchPanel);
        recentLabelHolder.add(l, BorderLayout.NORTH);
        JPanel recentHolderHolder = new JPanel(new CenterLayoutCopy());
        if(this.getComponentOrientation().isLeftToRight()) {
            recentHolderHolder.setBorder(new EmptyBorder(2, 10, 2, 2));
        } else {
            recentHolderHolder.setBorder(new EmptyBorder(2, 2, 2, 10));
        }
        recentHolderHolder.add(recentLabelHolder);
        superHolder.add(recentHolderHolder, BorderLayout.AFTER_LINE_ENDS);

        add(superHolder);

    }

    public void uninstallChooserPanel(JColorChooser enclosingChooser) {
        super.uninstallChooserPanel(enclosingChooser);
        swatchPanel.removeMouseListener(mainSwatchListener);
        recentSwatchPanel.removeMouseListener(recentSwatchListener);
        swatchPanel = null;
        recentSwatchPanel = null;
        mainSwatchListener = null;
        recentSwatchListener = null;
        removeAll();  // strip out all the sub-components
    }

    public void updateChooser() {

    }


    class RecentSwatchListener extends MouseAdapter implements Serializable {
        public void mousePressed(MouseEvent e) {
            Color color = recentSwatchPanel.getColorForLocation(e.getX(), e.getY());
            getColorSelectionModel().setSelectedColor(color);

        }
    }

    class MainSwatchListener extends MouseAdapter implements Serializable {
        public void mousePressed(MouseEvent e) {
            Color color = swatchPanel.getColorForLocation(e.getX(), e.getY());
            getColorSelectionModel().setSelectedColor(color);
            recentSwatchPanel.setMostRecentColor(color);

        }
    }

}


class SwatchPanelCopy extends JPanel {

    protected Color[] colors;
    protected Dimension swatchSize;
    protected Dimension numSwatches;
    protected Dimension gap;

    public SwatchPanelCopy() {
        initValues();
        initColors();
        setToolTipText(""); // register for events
        setOpaque(true);
        setBackground(Color.white);
        setRequestFocusEnabled(false);
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    public boolean isFocusTraversable() {
        return false;
    }

    protected void initValues() {

    }

    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        for(int row = 0; row < numSwatches.height; row++) {
            for(int column = 0; column < numSwatches.width; column++) {

                g.setColor(getColorForCell(column, row));
                int x;
                if((!this.getComponentOrientation().isLeftToRight()) &&
                    (this instanceof RecentSwatchPanelCopy)) {
                    x = (numSwatches.width - column - 1) * (swatchSize.width + gap.width);
                } else {
                    x = column * (swatchSize.width + gap.width);
                }
                int y = row * (swatchSize.height + gap.height);
                g.fillRect(x, y, swatchSize.width, swatchSize.height);
                g.setColor(Color.black);
                g.drawLine(x + swatchSize.width - 1, y, x + swatchSize.width - 1, y + swatchSize.height - 1);
                g.drawLine(x, y + swatchSize.height - 1, x + swatchSize.width - 1, y + swatchSize.height - 1);
            }
        }
    }

    public Dimension getPreferredSize() {
        int x = numSwatches.width * (swatchSize.width + gap.width) - 1;
        int y = numSwatches.height * (swatchSize.height + gap.height) - 1;
        return new Dimension(x, y);
    }

    protected void initColors() {


    }

    public String getToolTipText(MouseEvent e) {
        Color color = getColorForLocation(e.getX(), e.getY());
        return color.getRed() + ", " + color.getGreen() + ", " + color.getBlue();
    }

    public Color getColorForLocation(int x, int y) {
        int column;
        if((!this.getComponentOrientation().isLeftToRight()) &&
            (this instanceof RecentSwatchPanelCopy)) {
            column = numSwatches.width - x / (swatchSize.width + gap.width) - 1;
        } else {
            column = x / (swatchSize.width + gap.width);
        }
        int row = y / (swatchSize.height + gap.height);
        return getColorForCell(column, row);
    }

    private Color getColorForCell(int column, int row) {
        Color defaultColor = UIManager.getColor("ColorChooser.swatchesDefaultRecentColor");
        int index = (row * numSwatches.width) + column;

        if(colors != null && index < colors.length) {
            return colors[index]; // (STEVE) - change data orientation here
        } else {
            return defaultColor;
        }
    }


}

class RecentSwatchPanelCopy extends SwatchPanelCopy {
    protected void initValues() {
        swatchSize = UIManager.getDimension("ColorChooser.swatchesRecentSwatchSize");
        numSwatches = new Dimension(5, 7);
        gap = new Dimension(1, 1);
    }


    protected void initColors() {
        Color defaultRecentColor = UIManager.getColor("ColorChooser.swatchesDefaultRecentColor");
        int numColors = numSwatches.width * numSwatches.height;

        colors = new Color[numColors];
        for(int i = 0; i < numColors; i++) {
            colors[i] = defaultRecentColor;
        }
    }

    public void setMostRecentColor(Color c) {

        System.arraycopy(colors, 0, colors, 1, colors.length - 1);
        colors[0] = c;
        repaint();
    }

}


class MainSwatchPanelCopy extends SwatchPanelCopy {

    protected void initValues() {
        swatchSize = UIManager.getDimension("ColorChooser.swatchesSwatchSize");
        numSwatches = new Dimension(31, 9);
        gap = new Dimension(1, 1);
    }

    protected void initColors() {
        int[] rawValues = initRawValues();
        int numColors = rawValues.length / 3;

        colors = new Color[numColors];
        for(int i = 0; i < numColors; i++) {
            colors[i] = new Color(rawValues[(i * 3)], rawValues[(i * 3) + 1], rawValues[(i * 3) + 2]);
        }
    }

    private int[] initRawValues() {

        int[] rawValues = {
            255, 255, 255, // first row.
            204, 255, 255,
            204, 204, 255,
            204, 204, 255,
            204, 204, 255,
            204, 204, 255,
            204, 204, 255,
            204, 204, 255,
            204, 204, 255,
            204, 204, 255,
            204, 204, 255,
            255, 204, 255,
            255, 204, 204,
            255, 204, 204,
            255, 204, 204,
            255, 204, 204,
            255, 204, 204,
            255, 204, 204,
            255, 204, 204,
            255, 204, 204,
            255, 204, 204,
            255, 255, 204,
            204, 255, 204,
            204, 255, 204,
            204, 255, 204,
            204, 255, 204,
            204, 255, 204,
            204, 255, 204,
            204, 255, 204,
            204, 255, 204,
            204, 255, 204,
            204, 204, 204, // second row.
            153, 255, 255,
            153, 204, 255,
            153, 153, 255,
            153, 153, 255,
            153, 153, 255,
            153, 153, 255,
            153, 153, 255,
            153, 153, 255,
            153, 153, 255,
            204, 153, 255,
            255, 153, 255,
            255, 153, 204,
            255, 153, 153,
            255, 153, 153,
            255, 153, 153,
            255, 153, 153,
            255, 153, 153,
            255, 153, 153,
            255, 153, 153,
            255, 204, 153,
            255, 255, 153,
            204, 255, 153,
            153, 255, 153,
            153, 255, 153,
            153, 255, 153,
            153, 255, 153,
            153, 255, 153,
            153, 255, 153,
            153, 255, 153,
            153, 255, 204,
            204, 204, 204, // third row
            102, 255, 255,
            102, 204, 255,
            102, 153, 255,
            102, 102, 255,
            102, 102, 255,
            102, 102, 255,
            102, 102, 255,
            102, 102, 255,
            153, 102, 255,
            204, 102, 255,
            255, 102, 255,
            255, 102, 204,
            255, 102, 153,
            255, 102, 102,
            255, 102, 102,
            255, 102, 102,
            255, 102, 102,
            255, 102, 102,
            255, 153, 102,
            255, 204, 102,
            255, 255, 102,
            204, 255, 102,
            153, 255, 102,
            102, 255, 102,
            102, 255, 102,
            102, 255, 102,
            102, 255, 102,
            102, 255, 102,
            102, 255, 153,
            102, 255, 204,
            153, 153, 153, // fourth row
            51, 255, 255,
            51, 204, 255,
            51, 153, 255,
            51, 102, 255,
            51, 51, 255,
            51, 51, 255,
            51, 51, 255,
            102, 51, 255,
            153, 51, 255,
            204, 51, 255,
            255, 51, 255,
            255, 51, 204,
            255, 51, 153,
            255, 51, 102,
            255, 51, 51,
            255, 51, 51,
            255, 51, 51,
            255, 102, 51,
            255, 153, 51,
            255, 204, 51,
            255, 255, 51,
            204, 255, 51,
            153, 244, 51,
            102, 255, 51,
            51, 255, 51,
            51, 255, 51,
            51, 255, 51,
            51, 255, 102,
            51, 255, 153,
            51, 255, 204,
            153, 153, 153, // Fifth row
            0, 255, 255,
            0, 204, 255,
            0, 153, 255,
            0, 102, 255,
            0, 51, 255,
            0, 0, 255,
            51, 0, 255,
            102, 0, 255,
            153, 0, 255,
            204, 0, 255,
            255, 0, 255,
            255, 0, 204,
            255, 0, 153,
            255, 0, 102,
            255, 0, 51,
            255, 0, 0,
            255, 51, 0,
            255, 102, 0,
            255, 153, 0,
            255, 204, 0,
            255, 255, 0,
            204, 255, 0,
            153, 255, 0,
            102, 255, 0,
            51, 255, 0,
            0, 255, 0,
            0, 255, 51,
            0, 255, 102,
            0, 255, 153,
            0, 255, 204,
            102, 102, 102, // sixth row
            0, 204, 204,
            0, 204, 204,
            0, 153, 204,
            0, 102, 204,
            0, 51, 204,
            0, 0, 204,
            51, 0, 204,
            102, 0, 204,
            153, 0, 204,
            204, 0, 204,
            204, 0, 204,
            204, 0, 204,
            204, 0, 153,
            204, 0, 102,
            204, 0, 51,
            204, 0, 0,
            204, 51, 0,
            204, 102, 0,
            204, 153, 0,
            204, 204, 0,
            204, 204, 0,
            204, 204, 0,
            153, 204, 0,
            102, 204, 0,
            51, 204, 0,
            0, 204, 0,
            0, 204, 51,
            0, 204, 102,
            0, 204, 153,
            0, 204, 204,
            102, 102, 102, // seventh row
            0, 153, 153,
            0, 153, 153,
            0, 153, 153,
            0, 102, 153,
            0, 51, 153,
            0, 0, 153,
            51, 0, 153,
            102, 0, 153,
            153, 0, 153,
            153, 0, 153,
            153, 0, 153,
            153, 0, 153,
            153, 0, 153,
            153, 0, 102,
            153, 0, 51,
            153, 0, 0,
            153, 51, 0,
            153, 102, 0,
            153, 153, 0,
            153, 153, 0,
            153, 153, 0,
            153, 153, 0,
            153, 153, 0,
            102, 153, 0,
            51, 153, 0,
            0, 153, 0,
            0, 153, 51,
            0, 153, 102,
            0, 153, 153,
            0, 153, 153,
            51, 51, 51, // eigth row
            0, 102, 102,
            0, 102, 102,
            0, 102, 102,
            0, 102, 102,
            0, 51, 102,
            0, 0, 102,
            51, 0, 102,
            102, 0, 102,
            102, 0, 102,
            102, 0, 102,
            102, 0, 102,
            102, 0, 102,
            102, 0, 102,
            102, 0, 102,
            102, 0, 51,
            102, 0, 0,
            102, 51, 0,
            102, 102, 0,
            102, 102, 0,
            102, 102, 0,
            102, 102, 0,
            102, 102, 0,
            102, 102, 0,
            102, 102, 0,
            51, 102, 0,
            0, 102, 0,
            0, 102, 51,
            0, 102, 102,
            0, 102, 102,
            0, 102, 102,
            0, 0, 0, // ninth row
            0, 51, 51,
            0, 51, 51,
            0, 51, 51,
            0, 51, 51,
            0, 51, 51,
            0, 0, 51,
            51, 0, 51,
            51, 0, 51,
            51, 0, 51,
            51, 0, 51,
            51, 0, 51,
            51, 0, 51,
            51, 0, 51,
            51, 0, 51,
            51, 0, 51,
            51, 0, 0,
            51, 51, 0,
            51, 51, 0,
            51, 51, 0,
            51, 51, 0,
            51, 51, 0,
            51, 51, 0,
            51, 51, 0,
            51, 51, 0,
            0, 51, 0,
            0, 51, 51,
            0, 51, 51,
            0, 51, 51,
            0, 51, 51,
            51, 51, 51};
        return rawValues;
    }
}
