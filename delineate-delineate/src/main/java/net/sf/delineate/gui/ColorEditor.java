/*
 * SettingsPanel.java
 *
 * Copyright (C) 2003 Robert McKinnon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.delineate.gui;

import net.sf.delineate.command.Command;
import net.sf.delineate.utility.ColorUtilities;

import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.DefaultSwatchChooserPanelCopy;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Controls user defined settings.
 * @author robmckinnon@users.sourceforge.net
 */
public class ColorEditor {

    private final DefaultSwatchChooserPanelCopy colorPanel = new DefaultSwatchChooserPanelCopy();
    private final Set colorSet = new HashSet();
    private final JComboBox colorCombo = new JComboBox();
    private final JDialog colorDialog;
    private final Command command;

    private Color[] colors;

    public ColorEditor(Command command, String initialValue, JComponent parent, boolean enabled) {
        this.command = command;
        colorCombo.setEditable(true);
        colorCombo.setEnabled(enabled);
        colorCombo.setName(Command.BACKGROUND_COLOR_PARAMETER);
        colorCombo.setSelectedItem(initialValue);
        ColorComboBoxEditor colorEditor = new ColorComboBoxEditor();
        colorCombo.setEditor(colorEditor);
        final JTextField editorTextField = (JTextField)colorEditor.getEditorComponent();
        editorTextField.addKeyListener(initColorKeyListener());
        colorCombo.addItemListener(initColorItemListener(editorTextField));

        JColorChooser colorChooser = new JColorChooser();
        AbstractColorChooserPanel[] chooserPanels = colorChooser.getChooserPanels();
        chooserPanels = new AbstractColorChooserPanel[] {colorPanel, chooserPanels[0], chooserPanels[1], chooserPanels[2]};
        colorChooser.setChooserPanels(chooserPanels);
        colorChooser.setPreviewPanel(new JPanel());
        colorDialog = JColorChooser.createDialog(parent, "Choose background color", true, colorChooser, null, null);
    }

    public JComboBox getColorCombo() {
        return colorCombo;
    }

    private ItemListener initColorItemListener(final JTextField editor) {
        ItemListener itemListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    String colorText = (String)e.getItem();

                    if(colorText.length() == 6) {
                        Color color = ColorUtilities.getColor(colorText);
                        Color foreground = ColorUtilities.getForeground(color);

                        editor.setBackground(color);
                        editor.setForeground(foreground);
                        editor.setCaretColor(foreground);
                    }
                }
            }
        };

        return itemListener;
    }

    private KeyAdapter initColorKeyListener() {
        return new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                JTextField textField = (JTextField)event.getSource();
                String colorText = textField.getText();

                if(colorText.length() == 6) {
                    try {
                        if(!colorSet.contains(colorText)) {
                            colorCombo.insertItemAt(colorText, 0);
                            colorSet.add(colorText);
                        }

                        colorCombo.setSelectedItem(colorText);
                        command.setParameterValue(Command.BACKGROUND_COLOR_PARAMETER, colorText, false);
                    } catch(Exception e) {

                    }
                } else {
                    textField.setBackground(Color.white);
                    textField.setForeground(Color.black);
                }
            }
            };
    }

    public void chooseColor() {
        colorPanel.setColors(colors);
        colorDialog.show();

        Color color = colorPanel.getColorSelectionModel().getSelectedColor();
        if(color != null) {
            String colorText = ColorUtilities.getHexColor(color);
            setColor(colorText);
        }
    }

    public void setColor(String colorText) {
        try {
            ColorUtilities.getColor(colorText);

            if(!colorSet.contains(colorText)) {
                colorCombo.insertItemAt(colorText, 0);
                colorSet.add(colorText);
            }
            colorCombo.setSelectedItem(colorText);
            command.setParameterValue(Command.BACKGROUND_COLOR_PARAMETER, colorText, false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setColors(Color[] colors) {
        ColorUtilities.sortColors(colors);
        this.colors = colors;
    }

}

class ColorComboBoxEditor extends BasicComboBoxEditor {
    public ColorComboBoxEditor() {
        super();
        editor = new HexColorField();
    }

}

class HexColorField extends JTextField {

    protected Document createDefaultModel() {
        return new HexColorDocument();
    }

    static class HexColorDocument extends PlainDocument {

        public void insertString(int offset, String string, AttributeSet a)
            throws BadLocationException {

            if(string == null || offset > 5) {
                return;
            }

            char[] chars = string.toCharArray();

            for(int i = 0; i < chars.length; i++) {
                char character = chars[i];

                boolean lowerCaseHexChar = 'a' <= character && character <= 'f';
                boolean upperCaseHexChar = 'A' <= character && character <= 'F';
                boolean numericalHexChar = '0' <= character && character <= '9';

                if(!(lowerCaseHexChar || upperCaseHexChar || numericalHexChar)) {
                    return;
                }
            }

            super.insertString(offset, string.toUpperCase(), a);
        }
    }
}

