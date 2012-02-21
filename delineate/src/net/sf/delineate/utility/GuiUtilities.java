/*
 * GuiUtilities.java
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

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.EventQueue;

/**
 * GUI helper methods.
 * @author robmckinnon@users.sourceforge.net
 */
public class GuiUtilities {

    private static JFrame frame;

    public static JButton initButton(String text, String actionKey, int shortcutKey, int modifiers, JComponent component, AbstractAction action) {
        setKeyBinding(actionKey, shortcutKey, modifiers, action, component);

        JButton button = new JButton(action);
        button.setText(text);
        button.setMnemonic(shortcutKey);
        return button;
    }

    public static JButton initButton(String text, String actionKey, int shortcutKey, JComponent component, AbstractAction action) {
        return initButton(text, actionKey, shortcutKey, KeyEvent.CTRL_MASK, component, action);
    }

    public static void setKeyBinding(String actionKey, int key, int modifiers, AbstractAction action, JComponent component) {
        ActionMap actionMap = component.getActionMap();
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(key, modifiers);
        inputMap.put(keyStroke, actionKey);
        actionMap.put(actionKey, action);
    }

    public static void showMessageInEventQueue(final String message, final String title) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                showMessage(message, title);
            }
        });
    }

    public static void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void setFrame(JFrame frame) {
        GuiUtilities.frame = frame;
    }

}
