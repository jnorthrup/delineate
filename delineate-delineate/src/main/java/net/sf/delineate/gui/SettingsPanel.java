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
import net.sf.delineate.command.Parameter;
import net.sf.delineate.utility.FileUtilities;
import net.sf.delineate.utility.GuiUtilities;
import net.sf.delineate.utility.XPathTool;
import net.sf.delineate.utility.SettingUtilities;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SpringUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.xpath.XPathExpressionException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Controls user defined settings.
 * @author robmckinnon@users.sourceforge.net
 */
public class SettingsPanel implements RenderingListener {

    private static final String INPUT_FILE_ACTION = "InputFileAction";
    private static final String OUTPUT_FILE_ACTION = "OutputFileAction";
    private static final String BACKGROUND_COLOR_ACTION = "BackgroundColorAction";

    private final Map textFieldMap = new HashMap(5);
    private final Map fileSizeLabelMap = new HashMap(5);
    private final Map checkBoxMap = new HashMap(23);
    private final Map spinnerSliderMap = new HashMap(23);

    private final JPanel panel;
    private Command command;
    private JFileChooser tracingAppFileChooser;
    private ColorEditor colorEditor;

    private final ChangeListener changeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();

            if(source instanceof SpinnerSlider) {
                SpinnerSlider spinnerSlider = (SpinnerSlider)source;
                command.setParameterValue(spinnerSlider.getName(), spinnerSlider.getValueAsString(), false);
            } else {
                JCheckBox checkBox = (JCheckBox)e.getSource();
                command.setParameterEnabled(checkBox.getName(), checkBox.isSelected(), false);
            }
        }
    };

    private final KeyAdapter textFieldKeyListener = new KeyAdapter() {
        public void keyReleased(KeyEvent e) {
            JTextField textField = ((JTextField)e.getSource());
            command.setParameterValue(textField.getName(), textField.getText(), false);
            setFileSizeText(textField.getName(), textField.getText());
        }
    };

    private static final String SETTINGS_FILE_NAME = "settings.prop";


    public SettingsPanel(XPathTool xpathTool) throws Exception {
        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Conversion settings"));

        panel.add(initContentPane(xpathTool), BorderLayout.NORTH);

        SaveSettingsPanel saveSettingsPanel = new SaveSettingsPanel(command);

        panel.add(saveSettingsPanel.getPanel(), BorderLayout.SOUTH);
    }

    public void renderingCompleted() {
        updateFileSize();
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getCommand() {
        return command.getCommand();
    }

    public void setHeight(double height) {
        command.setParameterValue("height", Double.toString(height), false);
    }

    public void setWidth(double width) {
        command.setParameterValue("width", Double.toString(width), false);
    }

    public String getBackgroundColor() {
        if(command.getParameterEnabled(Command.BACKGROUND_COLOR_PARAMETER)) {
            return command.getParameterValue(Command.BACKGROUND_COLOR_PARAMETER);
        } else {
            return null;
        }
    }

    public File getInputFile() {
        String inputFile = command.getParameterValue(Command.INPUT_FILE_PARAMETER);
        return new File(inputFile);
    }

    public void setInputFile(File file) {
        command.setParameterValue(Command.INPUT_FILE_PARAMETER, file.getPath(), true);
    }

    public String getOutputFile() {
        return command.getParameterValue(Command.OUTPUT_FILE_PARAMETER);
    }

    public void selectInputTextField() {
        JTextField textField = getTextField(Command.INPUT_FILE_PARAMETER);
        textField.selectAll();
        textField.requestFocus();
    }

    public void updateFileSize() {
        String file = command.getParameterValue(Command.OUTPUT_FILE_PARAMETER);
        setFileSizeText(Command.OUTPUT_FILE_PARAMETER, file);
    }

    private JPanel initContentPane(XPathTool xpathTool) throws XPathExpressionException {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setLayout(new SpringLayout());
        String commandName = xpathTool.string("/parameters/command/name");
        String optionIndicator = xpathTool.string("/parameters/command/option-indicator");

        int descriptionCount = xpathTool.count("/parameters/parameter/description");
        int parameterCount = xpathTool.count("/parameters/parameter");

        command = new Command(commandName, optionIndicator, parameterCount, new Command.CommandChangeListener() {
            public void enabledChanged(Parameter parameter) {
                String name = parameter.getName();
                JCheckBox checkBox = (JCheckBox)checkBoxMap.get(name);
                if(checkBox != null) checkBox.setSelected(parameter.isEnabled());
            }

            public void valueChanged(Parameter parameter) {
                String name = parameter.getName();

                if(name.equals(Command.BACKGROUND_COLOR_PARAMETER)) {
                    colorEditor.setColor(parameter.getValue());
                } else {
                    JTextField textField = (JTextField)textFieldMap.get(name);
                    if(textField != null) {
                        String path = parameter.getValue();
                        textField.setText(path);
                    }

                    SpinnerSlider spinnerSlider = (SpinnerSlider)spinnerSliderMap.get(name);
                    if(spinnerSlider != null) {
                        spinnerSlider.setValue(parameter.getValue());
                    }
                }
            }
        });

        loadTracingApplicationPath(panel);

        for(int type = 0; type < 3; type++) {
            for(int i = 0; i < parameterCount; i++) {
                String xpathPrefix = "/parameters/parameter[" + (i + 1) + "]/";
                xpathTool.setXpathPrefix(xpathPrefix);
                String name = xpathTool.string("name");
                boolean isFileParameter = name.endsWith("file");
                boolean isNumberParameter = xpathTool.count("range") == 1;

                switch(type) {
                    case 0:
                        if(isFileParameter)
                            addParameter(panel, xpathTool, xpathPrefix, name);
                        break;
                    case 1:
                        if(!isFileParameter && !isNumberParameter)
                            addParameter(panel, xpathTool, xpathPrefix, name);
                        break;
                    case 2:
                        if(!isFileParameter && isNumberParameter)
                            addParameter(panel, xpathTool, xpathPrefix, name);
                        break;
                }
            }
        }

        SpringUtilities.makeCompactGrid(panel, descriptionCount, 2, 6, 6, 6, 6);
        return panel;
    }

    private void loadTracingApplicationPath(final JPanel parent) {
        Properties properties = SettingUtilities.loadProperties(SETTINGS_FILE_NAME, parent);
        String commandPath = properties.getProperty(command.getCommandName());
        if(commandPath != null) {
            command.setTracingApplication(commandPath);
        }
    }

    public void showTracingApplicationSelectionDialog() {
        if(tracingAppFileChooser == null) {
            String dialogTitle = "Select location of " + command.getCommandName();

            this.tracingAppFileChooser = initFileChooser(dialogTitle);
        }

        int response = tracingAppFileChooser.showOpenDialog(panel);

        if(response == JFileChooser.APPROVE_OPTION) {
            File file = tracingAppFileChooser.getSelectedFile();
            String commandPath = file.getPath();
            command.setTracingApplication(commandPath);
            Properties properties = SettingUtilities.loadProperties(SETTINGS_FILE_NAME, panel);
            properties.setProperty(command.getCommandName(), commandPath);
            SettingUtilities.saveProperties(properties, SETTINGS_FILE_NAME, "Delineate tracing application settings - http//delineate.sourceforge.net");
        }
    }

    private JFileChooser initFileChooser(String dialogTitle) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Select");
        fileChooser.setApproveButtonToolTipText("Select file");
        fileChooser.setDialogTitle(dialogTitle);
        return fileChooser;
    }

    private void addParameter(JPanel panel, XPathTool xpath, String xpathPrefix, String name) throws XPathExpressionException {
        boolean optional = xpath.toBoolean("optional");
        boolean enabled = !optional || xpath.toBoolean("enabled");

        String value = xpath.string("default");
        String desc = xpath.string("description");
        String function = xpath.string("function");
        command.addParameter(name, enabled, value, function);

        if(desc.length() == 0) {
            return;
        }

        JComponent labelPanel;
        JComponent controlComponent;

        if(xpath.count("range") != 1) {
            labelPanel = initLabelPanel(optional, enabled, null, desc, name, function);
            controlComponent = initControlComponent(name, value, enabled, null);
        } else {
            xpath.setXpathPrefix(xpathPrefix + "range/");
            boolean useWholeNumbers = xpath.toBoolean("use-whole-numbers");
            String stepString = xpath.string("step").trim();

            SpinnerNumberModel model = initSpinnerModel(useWholeNumbers, xpath, value);
            SpinnerSlider spinnerSlider = initSpinnerSlider(model, name, enabled, desc, useWholeNumbers, stepString);

            labelPanel = initLabelPanel(optional, enabled, spinnerSlider, desc, name, function);
            controlComponent = initControlPanel(spinnerSlider);
        }

        panel.add(labelPanel);
        panel.add(controlComponent);
    }

    private JComponent initControlComponent(String name, String value, boolean enabled, String function) {
        if(value.length() > 0) {
            if(name.equals(Command.BACKGROUND_COLOR_PARAMETER)) {
                colorEditor = new ColorEditor(command, value, panel, enabled);
                return colorEditor.getColorCombo();
            } else {
                setFileSizeText(name, value);
                JTextField textField = new JTextField(value);
                textField.setName(name);
                textField.setColumns(15);
                textField.addKeyListener(textFieldKeyListener);
                if(function != null) {
                    textFieldMap.put(function, textField);
                }
                textFieldMap.put(name, textField);

                return textField;
            }

        } else {
            return new JLabel("");
        }
    }

    private JPanel initControlPanel(SpinnerSlider spinnerSlider) {
        JPanel controlPanel = new JPanel(new BorderLayout(0,0));

        JSpinner spinner = spinnerSlider.getSpinner();
        Dimension size = new Dimension(53, (int)spinner.getPreferredSize().getHeight());
        spinner.setPreferredSize(size);
        controlPanel.add(spinner, BorderLayout.WEST);
        controlPanel.add(spinnerSlider.getSlider(), BorderLayout.EAST);

        return controlPanel;
    }

    private SpinnerSlider initSpinnerSlider(SpinnerNumberModel model, String name, boolean enabled, String desc, boolean useWholeNumbers, String stepString) {
        SpinnerSlider spinnerSlider = new SpinnerSlider(model);
        spinnerSlider.setName(name);
        spinnerSlider.setEnabled(enabled);
        spinnerSlider.setTooltipText(desc);
        spinnerSlider.addChangeListener(changeListener);
        spinnerSliderMap.put(name, spinnerSlider);

        if(!useWholeNumbers) {
            int fractionalDigits = stepString.substring(stepString.indexOf('.') + 1).length();
            spinnerSlider.setFractionDigitsLength(fractionalDigits);
        }
        return spinnerSlider;
    }

    private JPanel initLabelPanel(boolean optional, boolean enabled, final SpinnerSlider spinnerSlider, String desc, final String name, String function) {
        String labelName = name.replace('-', ' ');
        boolean isFileParameter = function.equals(Command.INPUT_FILE_PARAMETER) || function.equals(Command.OUTPUT_FILE_PARAMETER);
        boolean isBgColorParameter = name.equals(Command.BACKGROUND_COLOR_PARAMETER);
        final JPanel panel = new JPanel(new BorderLayout());
        Component labelComponent = null;

        JButton button = null;
        if(!isFileParameter && !isBgColorParameter) {
            JLabel label = new JLabel(labelName);
            label.setToolTipText(desc);
            labelComponent = label;
        } else if(isBgColorParameter) {
            button = initColorChooserButton(labelName, enabled);
            labelComponent = button;
        } else if(isFileParameter) {
            labelComponent = initFileChooserButton(name, labelName, function);
        }

        panel.add(labelComponent, BorderLayout.WEST);

        if(optional) {
            JCheckBox checkBox = initCheckbox(name, desc, enabled, spinnerSlider, button);
            panel.add(checkBox, BorderLayout.EAST);
        } else if(isFileParameter) {
            JLabel label = new JLabel();
            fileSizeLabelMap.put(function, label);
            panel.add(label, BorderLayout.EAST);
        }

        return panel;
    }

    private JButton initColorChooserButton(String labelName, boolean enabled) {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                colorEditor.chooseColor();
            }
        };

        JButton button = GuiUtilities.initButton(labelName, BACKGROUND_COLOR_ACTION, KeyEvent.VK_B, panel, action);
        button.setToolTipText("Choose color");
        button.setEnabled(enabled);

        return button;
    }

    private JButton initFileChooserButton(final String name, final String labelName, String function) {
        final String prompt = function.equals(Command.INPUT_FILE_PARAMETER) ? "Select " : "Name ";

        AbstractAction action = new AbstractAction() {
            JFileChooser fileChooser = initFileChooser(prompt + labelName);

            public void actionPerformed(ActionEvent e) {
                int response = fileChooser.showOpenDialog((JComponent)e.getSource());

                if(response == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    JTextField textField = getTextField(name);
                    textField.setText(file.getPath());
                    setFileSizeText(name, file.getPath());

                    command.setParameterValue(textField.getName(), textField.getText(), false);
                }
            }
        };

        JButton button = null;

        if(function.equals(Command.INPUT_FILE_PARAMETER)) {
            button = GuiUtilities.initButton(labelName, INPUT_FILE_ACTION, KeyEvent.VK_I, panel, action);
        } else if(function.equals(Command.OUTPUT_FILE_PARAMETER)) {
            button = GuiUtilities.initButton(labelName, OUTPUT_FILE_ACTION, KeyEvent.VK_O, panel, action);
        }

        button.setToolTipText("Browse files");
        return button;
    }

    private void setFileSizeText(final String name, String filePath) {
        JLabel label = (JLabel)fileSizeLabelMap.get(name);

        if(label != null) {
            File file = FileUtilities.getFile(filePath);
            String fileSize = FileUtilities.getFileSize(file);
            label.setText(fileSize);
        }
    }

    private JCheckBox initCheckbox(final String name, String desc, boolean enabled, final SpinnerSlider spinnerSlider, final JButton button) {
        final JCheckBox checkBox = new JCheckBox("", false);
        checkBoxMap.put(name, checkBox);
        checkBox.setName(name);
        checkBox.setToolTipText(desc);
        checkBox.setSelected(enabled);
        checkBox.setFocusPainted(true);
        if(spinnerSlider != null) {
            checkBox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    spinnerSlider.setEnabled(checkBox.isSelected());
                }
            });
        } else if(name.equals(Command.BACKGROUND_COLOR_PARAMETER)) {
            checkBox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JComboBox combo = colorEditor.getColorCombo();
                    combo.setEnabled(checkBox.isSelected());
                    button.setEnabled(checkBox.isSelected());
                }
            });
        }
        checkBox.addChangeListener(changeListener);

        return checkBox;
    }

    private JTextField getTextField(String key) {
        return (JTextField)textFieldMap.get(key);
    }

    private SpinnerNumberModel initSpinnerModel(boolean useWholeNumbers, XPathTool xpath, String defaultValue) throws XPathExpressionException {
        SpinnerNumberModel model;
        if(useWholeNumbers) {
            int value = Integer.parseInt(defaultValue);
            int min = xpath.toInt("min");
            int max = xpath.toInt("max");
            int step = xpath.toInt("step");

            model = new SpinnerNumberModel(value, min, max, step);
        } else {
            double value = Double.parseDouble(defaultValue);
            double min = xpath.toDouble("min");
            double max = xpath.toDouble("max");
            double step = xpath.toDouble("step");

            model = new SpinnerNumberModel(value, min, max, step);
        }
        return model;
    }

    public void setColors(Color[] colors) {
        if(colorEditor != null && colors != null) {
            colorEditor.setColors(colors);
        }
    }

    public boolean getCenterlineEnabled() {
        return command.getParameterEnabled(Command.CENTERLINE_PARAMETER);
    }

    public String[] getCommandAsArray() {
        return command.getCommandAsArray();
    }

    public String getCommandName() {
        return command.getCommandName();
    }

}