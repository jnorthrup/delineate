/*
 * ViewSourceAction.java
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

import org.apache.batik.util.MimeTypeConstants;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.xml.XMLUtilities;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.EventQueue;
import java.awt.Event;
import java.io.InputStream;
import java.io.Reader;

/**
 * To view the source of the current document.
 */
public class ViewSourceAction extends AbstractAction {

    private String url;
    private int x;
    private int y;
    private final JFrame frame = new JFrame();
    private final JTextArea textArea = new JTextArea();

    public ViewSourceAction() {
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(textArea);
        ImageIcon image = new ImageIcon("img/delineate-icon.png");
        frame.setIconImage(image.getImage());
        frame.setContentPane(scrollPane);

        final String CLOSE_ACTION = "close";
        ActionMap actionMap = scrollPane.getActionMap();

        actionMap.put(CLOSE_ACTION, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeFrame();
            }
        });

        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CLOSE_ACTION);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK), CLOSE_ACTION);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK), CLOSE_ACTION);
    }

    public void setSourceUrl(String url) {
        this.url = url;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void closeFrame() {
        synchronized(frame) {
            if(frame != null) {
                frame.setVisible(false);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if(url == null) {
            return;
        }

        final ParsedURL parsedUrl = new ParsedURL(url);

        new Thread() {
            public void run() {
                char[] buffer = new char[4096];

                try {
                    InputStream inputStream = parsedUrl.openStream(MimeTypeConstants.MIME_TYPES_SVG);
                    Reader reader = XMLUtilities.createXMLDocumentReader(inputStream);
                    int length;

                    final Document document = new PlainDocument();

                    while((length = reader.read(buffer, 0, buffer.length)) != -1) {
                        document.insertString(document.getLength(), new String(buffer, 0, length), null);
                    }

                    reader.close();
                    inputStream.close();

                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            synchronized(frame) {
                                setDocument(document);
                            }
                        }
                    });
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    private void setDocument(final Document document) {
        textArea.setDocument(document);

        frame.setTitle(url);
        frame.setBounds(x, y, 700, 650);
        frame.setVisible(true);
    }
}
