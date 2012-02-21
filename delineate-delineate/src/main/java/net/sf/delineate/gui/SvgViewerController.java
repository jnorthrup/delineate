/*
 * SvgViewerController.java
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

import net.sf.delineate.utility.FileUtilities;
import net.sf.delineate.utility.SvgOptimizer;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Controls the SVG viewer panels.
 * @author robmckinnon@users.sourceforge.net
 */
public class SvgViewerController {
    private String uri;

    private SvgViewerPanel svgViewerB;
    private SvgViewerPanel svgViewerA;

    private JPanel panel = new JPanel(new BorderLayout());

    public SvgViewerController() {
        svgViewerA = new SvgViewerPanel("Result: ", InputEvent.CTRL_MASK);
        svgViewerB = new SvgViewerPanel("Previous result: ", InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, svgViewerA.getViewerPanel(), svgViewerB.getViewerPanel());
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);

        installListeners();
        installActions();

        panel.add(splitPane);
    }

    public void addRenderingListener(RenderingListener listener) {
        svgViewerA.addRenderingListener(listener);
    }

    private void installListeners() {
        ScrollListener scrollListenerA = new ScrollListener(svgViewerB.getHorizontalScrollBar(), svgViewerB.getVerticalScrollBar());
        ScrollListener scrollListenerB = new ScrollListener(svgViewerA.getHorizontalScrollBar(), svgViewerA.getVerticalScrollBar());

        svgViewerA.addAdjustmentListener(scrollListenerA);
        svgViewerB.addAdjustmentListener(scrollListenerB);
    }

    private void installActions() {
        int mask = 0;
        addAction("Zoom in", JSVGCanvas.ZOOM_IN_ACTION, KeyEvent.VK_ADD, mask, false);
        addAction("Zoom out", JSVGCanvas.ZOOM_OUT_ACTION, KeyEvent.VK_SUBTRACT, mask, false);
        addAction("Zoom in", JSVGCanvas.ZOOM_IN_ACTION, KeyEvent.VK_EQUALS, mask, true);
        addAction("Zoom out", JSVGCanvas.ZOOM_OUT_ACTION, KeyEvent.VK_MINUS, mask, true);
        addSeparator();

        addAction(null, JSVGCanvas.SCROLL_RIGHT_ACTION, KeyEvent.VK_RIGHT, 0, false);
        addAction(null, JSVGCanvas.SCROLL_LEFT_ACTION, KeyEvent.VK_LEFT, 0, false);
        addAction(null, JSVGCanvas.SCROLL_UP_ACTION, KeyEvent.VK_UP, 0, false);
        addAction(null, JSVGCanvas.SCROLL_DOWN_ACTION, KeyEvent.VK_DOWN, 0, false);

        addAction("Scroll right", JSVGCanvas.FAST_SCROLL_RIGHT_ACTION, KeyEvent.VK_RIGHT, Event.SHIFT_MASK, true);
        addAction("Scroll left", JSVGCanvas.FAST_SCROLL_LEFT_ACTION, KeyEvent.VK_LEFT, Event.SHIFT_MASK, true);
        addAction("Scroll up", JSVGCanvas.FAST_SCROLL_UP_ACTION, KeyEvent.VK_UP, Event.SHIFT_MASK, true);
        addAction("Scroll down", JSVGCanvas.FAST_SCROLL_DOWN_ACTION, KeyEvent.VK_DOWN, Event.SHIFT_MASK, true);
        addSeparator();

        addAction("Reset", JSVGCanvas.RESET_TRANSFORM_ACTION, KeyEvent.VK_R, mask, true);
        addSeparator();

        addSpecificAction("View source", SvgViewerPanel.VIEW_SOURCE_ACTION, KeyEvent.VK_U, mask, svgViewerA);
        addSpecificAction("View source", SvgViewerPanel.VIEW_SOURCE_ACTION, KeyEvent.VK_U, mask + Event.SHIFT_MASK, svgViewerB);

        svgViewerA.setControllerActionMap(panel.getActionMap());
        svgViewerB.setControllerActionMap(panel.getActionMap());
    }

    private void addSeparator() {
        svgViewerA.addSeparator();
        svgViewerB.addSeparator();
    }

    private void addAction(String name, String actionKey, int key, int modifiers, boolean addToMenu) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(key, modifiers);
        String actionMapKey = actionKey + key;
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionMapKey);
        ActionMap actionMap = panel.getActionMap();

        if(actionMap.get(actionMapKey) == null) {
            Action actionA = svgViewerA.getAction(actionKey);
            Action actionB = svgViewerB.getAction(actionKey);
            SvgViewAction action = new SvgViewAction(name, actionA, actionB);
            actionMap.put(actionKey + key, action);

            if(addToMenu) {
                svgViewerA.addMenuItem(action, keyStroke);
                svgViewerB.addMenuItem(action, keyStroke);
            }
        }
    }

    private void addSpecificAction(String name, String actionKey, int key, int modifiers, SvgViewerPanel viewer) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(key, modifiers);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionKey + modifiers);

        Action action = viewer.getAction(actionKey);
        action.putValue(Action.NAME, name);
        panel.getActionMap().put(actionKey + modifiers, action);

        viewer.addMenuItem(action, keyStroke);
    }

    private class SvgViewAction extends AbstractAction {
        private Action actionA;
        private Action actionB;

        public SvgViewAction(String name, Action actionA, Action actionB) {
            super(name);
            this.actionA = actionA;
            this.actionB = actionB;
        }

        public void actionPerformed(ActionEvent e) {
            actionA.actionPerformed(null);
            actionB.actionPerformed(null);
        }
    }

    public JComponent getSvgViewerPanels() {
        return panel;
    }

    public void movePreviousSvg(String newOutputFile) throws InterruptedException, InvocationTargetException {
        if(uri != null) {
            svgViewerA.closeViewSourceFrame();
            svgViewerB.closeViewSourceFrame();

            File file = FileUtilities.getFile(uri);
            final File previousFile = new File(FileUtilities.getTempDir(), file.getName() + '~');

            if(previousFile.exists()) {
                previousFile.delete();
            }

            if(file.getPath().equals(newOutputFile)) { // safe to rename last output file
                file.renameTo(previousFile);
            } else { // must copy last output file
                try {
                    FileUtilities.copy(file, previousFile);
                } catch(IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            svgViewerB.setPathCount(svgViewerA.getPathCount());
            final SVGDocument svgDocument = (SVGDocument)svgViewerA.getSvgDocument().cloneNode(true);

            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    String uri = FileUtilities.getUri(previousFile.getPath());
                    svgViewerB.setSvgDocument(uri, svgDocument);
                }
            });
        }
    }

    public void load(final String uri) {
        this.uri = uri;
        System.out.println("loading " + uri);
        svgViewerA.setOptimize(true);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                svgViewerA.setURI(uri);
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Batik");
        SvgViewerController app = new SvgViewerController();
        frame.setContentPane(app.getSvgViewerPanels());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setBounds(200, 100, 400, 400);
        frame.setVisible(true);
    }

    public void setStatus(String text) {
        svgViewerA.setStatus(text, "");
    }

    public Document getSvgDocument() {
        return svgViewerA.getSvgDocument();
    }

    public void setSvgOptimizer(SvgOptimizer svgOptimizer) {
        svgViewerA.setSvgOptimizer(svgOptimizer);
    }

    private class ScrollListener implements AdjustmentListener {
        private JScrollBar horizontalBar;
        private JScrollBar verticalBar;

        public ScrollListener(JScrollBar horizontalBar, JScrollBar verticalBar) {
            this.horizontalBar = horizontalBar;
            this.verticalBar = verticalBar;
        }

        public void adjustmentValueChanged(AdjustmentEvent e) {
            if(!e.getValueIsAdjusting()) {
                Adjustable adjustable = e.getAdjustable();
                int orientation = adjustable.getOrientation();

                if(orientation == Adjustable.HORIZONTAL) {
                    horizontalBar.setValue(adjustable.getValue());
                } else {
                    verticalBar.setValue(adjustable.getValue());
                }
            }
        }
    }

}