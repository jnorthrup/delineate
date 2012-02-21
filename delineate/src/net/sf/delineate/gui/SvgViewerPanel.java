/*
 * SvgViewerPanel.java
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
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.w3c.dom.svg.SVGDocument;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Panel for viewing SVG files.
 * @author robmckinnon@users.sourceforge.net
 */
public class SvgViewerPanel {

    public static final String VIEW_SOURCE_ACTION = "ViewSource";

    private final ScrollableJSVGCanvas svgCanvas = new ScrollableJSVGCanvas();
    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel sizeLabel = new JLabel("");
    private final ViewSourceAction viewSourceAction = new ViewSourceAction();
    private final JPopupMenu popupMenu = new JPopupMenu();
    private SvgOptimizer svgOptimizer;

    private List renderingListenerList = new ArrayList();
    private JScrollBar horizontalScrollBar;
    private JScrollBar verticalScrollBar;
    private JPanel viewerPanel;
    private ActionMap controllerActionMap;
    private String uri;

    private boolean optimize = false;
    private int pathCount = 0;
    private int modifier;

    public SvgViewerPanel(String resultText, int modifier) {
        this.modifier = modifier;
        installListeners(resultText);
        installActions();
        viewerPanel = createViewerPanel();
    }

    public JPanel getViewerPanel() {
        return viewerPanel;
    }

    public void setSvgOptimizer(SvgOptimizer svgOptimizer) {
        this.svgOptimizer = svgOptimizer;
    }

    private SvgOptimizer getSvgOptimizer() {
        return svgOptimizer;
    }

    public void closeViewSourceFrame() {
        viewSourceAction.closeFrame();
        viewSourceAction.setSourceUrl(null);
    }

    public int getPathCount() {
        return pathCount;
    }

    public void setPathCount(int pathCount) {
        this.pathCount = pathCount;
    }

    private void installActions() {
        InputMap inputMap = svgCanvas.getInputMap();
        KeyStroke[] keys = inputMap.keys();

        for(int i = 0; i < keys.length; i++) {
            KeyStroke key = keys[i];
            inputMap.remove(key);
        }

        ActionMap actionMap = svgCanvas.getActionMap();
        actionMap.put(SvgViewerPanel.VIEW_SOURCE_ACTION, viewSourceAction);
    }

    private void installListeners(final String resultText) {
        svgCanvas.addMouseListener(new PopupListener(popupMenu));

        svgCanvas.addSVGDocumentLoaderListener(new
            SVGDocumentLoaderAdapter() {
                public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
                    sizeLabel.setText("");
                    setStatus("Loading...");
                }

                public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
                    getSvgOptimizer().addBackground(e.getSVGDocument());
                    getSvgOptimizer().adjustDimensions(e.getSVGDocument());

                    if(optimize) {
                        optimize();

                        if(getSvgOptimizer().groupByColor()) {
                            svgCanvas.stopProcessing();
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    setURI(uri);
                                }
                            });

                        }
                    }
                }
            });

        svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
            public void gvtBuildStarted(GVTTreeBuilderEvent e) {
                setStatus("Interpreting...");
            }
        });

        svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
            public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
                setStatus("Rendering...");
            }


            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                final File file = FileUtilities.getFile(uri);
                finishConversion(file, resultText);
            }

        });

    }

    private void optimize() {
        File file = FileUtilities.getFile(uri);
        getSvgOptimizer().optimize(file, getSvgDocument());

        for(Iterator iterator = renderingListenerList.iterator(); iterator.hasNext();) {
            RenderingListener renderingListener = (RenderingListener)iterator.next();
            renderingListener.setColors(getSvgOptimizer().getColors());
        }

        setPathCount(getSvgOptimizer().getPathCount());
        optimize = false;
    }

    private void finishConversion(File file, String resultText) {
        viewSourceAction.setSourceUrl(uri);

        Container ancestor = svgCanvas.getTopLevelAncestor();
        int top = ancestor.getInsets().top;
        viewSourceAction.setLocation(ancestor.getX() + (top / 2), ancestor.getY() + top);

        String fileSize = FileUtilities.getFileSize(file);
        setStatus(resultText + file.getName(), pathCount + " paths - " + fileSize);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                for(Iterator iterator = renderingListenerList.iterator(); iterator.hasNext();) {
                    RenderingListener renderingListener = (RenderingListener)iterator.next();
                    renderingListener.renderingCompleted();
                }
            }
        });
    }

    public void setStatus(String statusText, String fileInfoText) {
        sizeLabel.setText(fileInfoText);
        setStatus(statusText);
    }

    public void setStatus(String statusText) {
        statusLabel.setText(statusText);
//        statusLabel.repaint();
    }

    public Action getAction(String actionKey) {
        return svgCanvas.getActionMap().get(actionKey);
    }

    public void setURI(String uri) {
        setSvgDocument(uri, null);  // hack to prevent problem loading relative URI
        svgCanvas.setURI(uri);
    }

    public void setSvgDocument(String uri, SVGDocument svgDocument) {
        this.uri = uri;
        if(svgDocument == null) {
            svgCanvas.installSVGDocument(svgDocument);
        } else {
            svgCanvas.setSVGDocument(svgDocument);
        }
    }

    public JScrollBar getHorizontalScrollBar() {
        return horizontalScrollBar;
    }

    public JScrollBar getVerticalScrollBar() {
        return verticalScrollBar;
    }

    public void addAdjustmentListener(AdjustmentListener scrollListener) {
        horizontalScrollBar.addAdjustmentListener(scrollListener);
        verticalScrollBar.addAdjustmentListener(scrollListener);
    }

    private JPanel createViewerPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(sizeLabel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(svgCanvas);

        horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        verticalScrollBar = scrollPane.getVerticalScrollBar();

        panel.add(scrollPane);
        panel.add(statusPanel, BorderLayout.SOUTH);

        return panel;
    }

    public void addMenuItem(Action action, KeyStroke keyStroke) {
        JMenuItem menuItem = new JMenuItem(action);
        menuItem.setText((String)action.getValue(Action.NAME));
        menuItem.setAccelerator(keyStroke);
        popupMenu.add(menuItem);
    }

    public void addSeparator() {
        popupMenu.addSeparator();
    }

    public JMenuItem getMenuItem(String text, String actionKey, int shortcutKey) {
        Action action = getAction(actionKey);

        JMenuItem menuItem = new JMenuItem(action);
        menuItem.setText(text);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcutKey, modifier);
        menuItem.setAccelerator(keyStroke);
        return menuItem;
    }

    public void setControllerActionMap(ActionMap controllerActionMap) {
        this.controllerActionMap = controllerActionMap;
    }

    private void setActionsEnabled(boolean enabled) {
        Object[] keys = controllerActionMap.allKeys();
        for(int i = 0; i < keys.length; i++) {
            Object key = keys[i];
            controllerActionMap.get(key).setEnabled(enabled);
        }
    }

    public SVGDocument getSvgDocument() {
        return svgCanvas.getSVGDocument();
    }

    public void addRenderingListener(RenderingListener listener) {
        renderingListenerList.add(listener);
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    private class PopupListener extends MouseAdapter {
        private JPopupMenu popupMenu;

        public PopupListener(JPopupMenu popupMenu) {
            this.popupMenu = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if(e.isPopupTrigger()) {
                SVGDocument svgDocument = svgCanvas.getSVGDocument();

                if(svgDocument == null) {
                    setActionsEnabled(false);
                } else {
                    setActionsEnabled(true);
                    ViewSourceAction viewSourceAction = (ViewSourceAction)getAction(VIEW_SOURCE_ACTION);
                    viewSourceAction.setLocation(e.getX(), e.getY());
                }

                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}