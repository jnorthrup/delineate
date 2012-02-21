/*
 * SettingUtilities.java
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
package net.sf.delineate.utility;

import javax.swing.*;
import java.io.*;
import java.util.Properties;

/**
 * Helper methods for saving and loading settings.
 * @author robmckinnon@users.sourceforge.net
 */
public class SettingUtilities {

    private static final String SETTINGS_DIR = "./settings/";

    public static void saveProperties(Properties properties, String fileName, String header) {
        File file = new File(SETTINGS_DIR + fileName);
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
            properties.store(outputStream, header);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties loadProperties(String fileName, JComponent parent) {
        File file = new File(SETTINGS_DIR + fileName);

        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "Cannot create " + fileName + " file: " + e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);
            }
        }

        Properties properties = new Properties();

        try {
            BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
            properties.load(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

}
