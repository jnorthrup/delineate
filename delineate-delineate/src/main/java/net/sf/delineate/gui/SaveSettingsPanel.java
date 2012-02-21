/*
 * SaveSettingsPanel.java
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
import net.sf.delineate.utility.GuiUtilities;
import net.sf.delineate.utility.SettingUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Properties;
import java.util.Set;

/**
 * Controls for saving and loading user defined settings.
 * @author robmckinnon@users.sourceforge.net
 */
public class SaveSettingsPanel {

    private static final String SAVE_SETTINGS_ACTION = "SaveSettingsAction";
    private static final String LOAD_SETTINGS_ACTION = "LoadSettingsAction";
    private static final String DELETE_SETTINGS_ACTION = "DeleteSettingsAction";
    private static final String DEFAULT_SETTING_NAME = "default";

    private JPanel panel;
    private Command command;

    private Properties savedSettings;
    private JComboBox loadSettingsCombo;
    private boolean loadSettingsEnabled = true;

    public SaveSettingsPanel(Command command) throws Exception {
        this.command = command;
        panel = new JPanel();
        JButton deleteButton = initDeleteButton();

        panel.add(initSaveButton());
        panel.add(initLoadButton());
        panel.add(initLoadSettingsCombo());
        panel.add(deleteButton);
    }

    public JPanel getPanel() {
        return panel;
    }

    private void saveSettings() {
        String initialName = (String)loadSettingsCombo.getSelectedItem();

        if(initialName.equals(DEFAULT_SETTING_NAME)) {
            initialName = "";
        }
        String name = (String)JOptionPane.showInputDialog(this.panel, "Enter a name:", "Save settings", JOptionPane.PLAIN_MESSAGE, null, null, initialName);

        if(name != null) {
            if(name.length() == 0) {
                JOptionPane.showMessageDialog(this.panel, "You must enter a name to save settings.", "Invalid name.", JOptionPane.PLAIN_MESSAGE);
                saveSettings();
            } else if(name.equals(DEFAULT_SETTING_NAME)) {
                JOptionPane.showMessageDialog(this.panel, "The name " + DEFAULT_SETTING_NAME + " is reserved.", "Invalid name.", JOptionPane.PLAIN_MESSAGE);
                saveSettings();
            } else {
                boolean isNewSavedSetting = savedSettings.getProperty(name) == null;
                savedSettings.setProperty(name, command.getCommand());
                SettingUtilities.saveProperties(savedSettings, getSettingsFileName(), getSettingsHeader());

                if(isNewSavedSetting) {
                    loadSettingsCombo.addItem(name);
                }

                if(!name.equals(initialName)) {
                    loadSettingsEnabled = false;
                    loadSettingsCombo.setSelectedItem(name);
                    loadSettingsEnabled = true;
                }
            }
        }

    }

    private void deleteSettings() {
        String settingName = (String)loadSettingsCombo.getSelectedItem();
        int response = JOptionPane.showConfirmDialog(loadSettingsCombo, "Delete " + settingName + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);

        if(response == JOptionPane.YES_OPTION) {
            savedSettings.remove(settingName);
            SettingUtilities.saveProperties(savedSettings, getSettingsFileName(), getSettingsHeader());
            loadSettingsCombo.removeItem(settingName);
            loadSettingsCombo.setSelectedItem(DELETE_SETTINGS_ACTION);
        }
    }

    private JButton initDeleteButton() {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                deleteSettings();
            }
        };
        action.setEnabled(false);

        return GuiUtilities.initButton("Delete settings", DELETE_SETTINGS_ACTION, KeyEvent.VK_D, panel, action);
    }

    private JButton initLoadButton() {
        JButton button = GuiUtilities.initButton("Load:", LOAD_SETTINGS_ACTION, KeyEvent.VK_L, panel, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                loadSettingsCombo.requestFocus();
            }
        });
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));

        return button;
    }

    private JButton initSaveButton() {
        return GuiUtilities.initButton("Save settings", SAVE_SETTINGS_ACTION, KeyEvent.VK_S, panel, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                saveSettings();
            }
        });
    }

    private JComboBox initLoadSettingsCombo() {
        savedSettings = SettingUtilities.loadProperties(getSettingsFileName(), panel);
        savedSettings.setProperty(DEFAULT_SETTING_NAME, command.getCommand());

        Set keySet = savedSettings.keySet();
        String[] settingNames = (String[])keySet.toArray(new String[keySet.size()]);

        loadSettingsCombo = new JComboBox(settingNames);
        loadSettingsCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    loadSettings((String)loadSettingsCombo.getSelectedItem());
                }
            }
        });

        loadSettingsCombo.setSelectedItem(DEFAULT_SETTING_NAME);
        Dimension size = loadSettingsCombo.getPreferredSize();
        size.width = 72;
        loadSettingsCombo.setPreferredSize(size);
        loadSettingsCombo.setMaximumSize(size);
        return loadSettingsCombo;
    }

    private void loadSettings(String settingName) {
        Action deleteAction = panel.getActionMap().get(DELETE_SETTINGS_ACTION);

        if(settingName.equals(DEFAULT_SETTING_NAME)) {
            deleteAction.setEnabled(false);
        } else {
            deleteAction.setEnabled(true);
        }

        if(loadSettingsEnabled) {
            String commandSetting = savedSettings.getProperty(settingName);

            if(settingName.equals(DEFAULT_SETTING_NAME)) {
                command.setCommandDefaultValues();
            }

            command.setCommand(commandSetting);
        }
    }

    private String getSettingsFileName() {
        return "settings-" + command.getCommandName() + ".prop";
    }

    private String getSettingsHeader() {
        return "Delineate command settings for " + command.getCommandName() + " invocation - http//delineate.sourceforge.net";
    }

}
