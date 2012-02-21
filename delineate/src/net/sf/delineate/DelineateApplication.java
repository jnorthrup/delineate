/*
 * DelineateApplication.java - GUI for converting raster images to SVG using AutoTrace
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
package net.sf.delineate;

import net.sf.delineate.gui.RenderingListener;
import net.sf.delineate.gui.SettingsPanel;
import net.sf.delineate.gui.SvgViewerController;
import net.sf.delineate.gui.SpinnerSlider;
import net.sf.delineate.utility.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * GUI for converting raster images to SVG using AutoTrace
 * @author robmckinnon@users.sourceforge.net
 */
public class DelineateApplication {
    public static final String CONVERT_IMAGE_ACTION = "Convert";
    private static final JFrame frame = new JFrame("Delineate - raster to SVG converter");
    private final SvgViewerController svgViewerController;
    private List convertPanelList = new ArrayList(5);

    public DelineateApplication(String autotraceParameterFile, String potraceParameterFile) throws Exception {
        GuiUtilities.setFrame(frame);

        svgViewerController = initSvgViewerController();

        JTabbedPane tabbedPane = new JTabbedPane();
        addControlTab(autotraceParameterFile, tabbedPane, true);
        addControlTab(potraceParameterFile, tabbedPane, false);
//        JMenuBar menuBar = createMenuBar(svgViewerController);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                svgViewerController.getSvgViewerPanels(), tabbedPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(1);

        frame.setContentPane(splitPane);
        ImageIcon image = new ImageIcon("img/delineate-icon.png");
        frame.setIconImage(image.getImage());
        frame.setBounds(130, 0, 800, 740);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getGlassPane().addMouseListener(new MouseAdapter() {});
        frame.getGlassPane().addMouseMotionListener(new MouseMotionAdapter() {});
        frame.getGlassPane().addKeyListener(new KeyAdapter() {});

        frame.setVisible(true);
    }

    private void addControlTab(String parameterFile, JTabbedPane tabbedPane, boolean optionsPanel) throws Exception, IOException, SAXException {
        XPathTool xpathTool = new XPathTool(new File(parameterFile));
        String label = xpathTool.string("/parameters/command/label");
        String description = xpathTool.string("/parameters/command/description");
        String optimizer = xpathTool.string("/parameters/command/svg-optimizer");
        Class optimizerClass = Class.forName(optimizer);
        SvgOptimizer svgOptimizer = (SvgOptimizer)optimizerClass.newInstance();
        JPanel controlPanel = getControlPanel(xpathTool, optionsPanel, svgOptimizer);
        tabbedPane.addTab(label, null, controlPanel, description);
    }

    private JPanel getControlPanel(XPathTool xpathTool, boolean isOptionsPanel, final SvgOptimizer svgOptimizer) throws Exception {
        final SettingsPanel settingsPanel = new SettingsPanel(xpathTool);
        svgViewerController.addRenderingListener(settingsPanel);

        JPanel optionsPanel;
        if(isOptionsPanel) {
            optionsPanel = initOptionsPanel(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String optimizeType = e.getActionCommand();
                    svgOptimizer.setOptimizeType(optimizeType);
                }
            });
        } else {
            optionsPanel = new JPanel();
            optionsPanel.setBorder(BorderFactory.createTitledBorder("Brightness threshold"));
            optionsPanel.setToolTipText("Black/white cutoff; pixels brighter than threshold " +
                    "are converted to white, those below to black.");
            SpinnerNumberModel model = new SpinnerNumberModel(50, 0, 100, 1);
            SpinnerSlider spinnerSlider = new SpinnerSlider(model);
            optionsPanel.add(spinnerSlider.getSpinner());
            optionsPanel.add(spinnerSlider.getSlider());
            spinnerSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    SpinnerSlider spinnerSlider = (SpinnerSlider)e.getSource();
                    svgOptimizer.setThresholdPercent(spinnerSlider.getSlider().getValue());
                }
            });
        }

        JPanel convertPanel = new JPanel();
        JButton button = initConvertButton(convertPanel, settingsPanel, svgOptimizer);
        convertPanel.add(button);
        this.convertPanelList.add(convertPanel);

        return createControlPanel(settingsPanel, optionsPanel, convertPanel);
    }

    private SvgViewerController initSvgViewerController() {
        SvgViewerController svgViewerController = new SvgViewerController();

        svgViewerController.addRenderingListener(new RenderingListener() {
            public void renderingCompleted() {
                enableGui();
            }

            public void setColors(Color[] colors) {
            }
        });

        return svgViewerController;
    }

    private JPanel initOptionsPanel(ActionListener listener) {
        ButtonGroup buttonGroup = new ButtonGroup();
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Result options"));

        initRadio(SvgOptimizer.NO_GROUPS, listener, buttonGroup, panel,
                "Don't place paths in group elements. Each path element has its own style attribute.").setSelected(true);
        initRadio(SvgOptimizer.COLOR_GROUPS, listener, buttonGroup, panel,
                "Place paths in group elements based on color. Use with color count setting to reduce file size.");
        initRadio(SvgOptimizer.ONE_GROUP, listener, buttonGroup, panel,
                "Place all paths in one group element that defines styles common to all paths.");

//        Don't show style definition option, because resulting file doesn't render properly in SodiPodi, nor Mozilla
//        initRadio(SvgOptimizer.STYLE_DEFS, listener, buttonGroup, panel,
//            "Creates SVG style definitions, may reduce output file size if there are many paths and few colors. Use with the color count setting.");

        SpringUtilities.makeCompactGrid(panel, 1, 3, 2, 2, 2, 2);
        return panel;
    }

    private JRadioButton initRadio(String text, ActionListener listener, ButtonGroup buttonGroup, JPanel panel, String tooltip) {
        JRadioButton radio = new JRadioButton(text);
        radio.setToolTipText(tooltip);
        radio.setActionCommand(text);
        radio.addActionListener(listener);
        buttonGroup.add(radio);
        panel.add(radio);
        return radio;
    }

//    private JMenuBar createMenuBar(final SvgViewerController svgViewerController) {
//        JMenuBar menuBar = new JMenuBar();
//        menuBar.add(svgViewerController.getSvgViewerMenu());
//        return menuBar;
//    }

    private JPanel createControlPanel(final SettingsPanel settingsPanel, JPanel optionsPanel, JPanel buttonPanel) {
        JPanel controlPanel = new JPanel(new SpringLayout());
        controlPanel.add(settingsPanel.getPanel());
        int rows = 2;
        if(optionsPanel != null) {
            controlPanel.add(optionsPanel);
            rows = 3;
        }
        controlPanel.add(buttonPanel);
        SpringUtilities.makeCompactGrid(controlPanel, rows, 1, 6, 6, 6, 6);
        JPanel controlWrapperPanel = new JPanel();
        controlWrapperPanel.add(controlPanel);
        return controlWrapperPanel;
    }

    private JButton initConvertButton(final JPanel panel, final SettingsPanel settingsPanel, final SvgOptimizer svgOptimizer) {
        JButton button = GuiUtilities.initButton("Run", CONVERT_IMAGE_ACTION, KeyEvent.VK_ENTER, 0, panel, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                convert(settingsPanel, svgOptimizer);
            }
        });

        button.setMnemonic(KeyEvent.VK_R);
        return button;
    }

    private boolean assertTracingApplicationInstalled(String commandName, String command) {
        boolean installed = true;

        try {
            String output = RuntimeUtility.getOutput(new String[] {command, "-version"});
            System.out.println("Found: " + output);
        } catch(Exception e) {
            System.out.println(commandName + " not found");
            GuiUtilities.showMessage("You must set the path to " + commandName + " to run conversions.\n" +
                "See INSTALL.txt file for details.", commandName + " not found");
            installed = false;
        }

        return installed;
    }

    private void convert(final SettingsPanel settingsPanel, final SvgOptimizer svgOptimizer) {
        final File file = settingsPanel.getInputFile();

        if(file.exists()) {
            if(!assertTracingApplicationInstalled(settingsPanel.getCommandName(), settingsPanel.getCommandAsArray()[0])) {
                settingsPanel.showTracingApplicationSelectionDialog();
                if(!assertTracingApplicationInstalled(settingsPanel.getCommandName(), settingsPanel.getCommandAsArray()[0])) {
                    return;
                }
            }

            disableGui();
            svgViewerController.setSvgOptimizer(svgOptimizer);
            svgViewerController.setStatus("Converting...");

            new Thread() {
                public void run() {
                    try {
                        File convertedFile = null;

                        if(settingsPanel.getCommandName().equals("potrace")) {
                            convertedFile = ImageUtilities.convertToPbm(file, svgOptimizer.getThresholdPercent());
                            Dimension dimension = ImageUtilities.getDimension(convertedFile);
                            settingsPanel.setHeight(dimension.getHeight() / 72);
                            settingsPanel.setWidth(dimension.getWidth() / 72);
                        } else if(!(ImageUtilities.inBmpFormat(file) || ImageUtilities.inPnmFormat(file))) {
                            convertedFile = ImageUtilities.convertToPnm(file);
                        }

                        if(convertedFile != null && convertedFile.exists()) {
                            settingsPanel.setInputFile(convertedFile);
                        }
                        final String outputFile = settingsPanel.getOutputFile();
                        svgViewerController.movePreviousSvg(outputFile);

                        String[] commandArray = settingsPanel.getCommandAsArray();
                        System.out.println(settingsPanel.getCommand());

                        RuntimeUtility.execute(commandArray);

                        if(convertedFile != null && convertedFile.exists()) {
                            settingsPanel.setInputFile(file);
                            convertedFile.delete();
                        }

                        svgOptimizer.setBackgroundColor(settingsPanel.getBackgroundColor());
                        svgOptimizer.setCenterlineEnabled(settingsPanel.getCenterlineEnabled());

                        svgViewerController.load(FileUtilities.getUri(outputFile));
                    } catch(Exception e) {
                        e.printStackTrace();
                        GuiUtilities.showMessageInEventQueue("An error occurred, cannot run conversion: \n"
                            + e.getMessage(), "Error");
                        enableGuiInEventThread();
                    }
                }
            }.start();

        } else {
            GuiUtilities.showMessage("Input file does not exist.", "Invalid input file");
            settingsPanel.selectInputTextField();
        }
    }

    private void enableGuiInEventThread() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                enableGui();
            }
        });
    }

    private void setConvertImageActionEnabled(boolean enabled) {
        for (Object aConvertPanelList : convertPanelList) {
            JPanel panel = (JPanel)aConvertPanelList;
            Action action = panel.getActionMap().get(CONVERT_IMAGE_ACTION);
            action.setEnabled(enabled);
        }
    }

    private void disableGui() {
        frame.getGlassPane().setVisible(true);
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setConvertImageActionEnabled(false);
    }

    private void enableGui() {
        setConvertImageActionEnabled(true);
        frame.getGlassPane().setVisible(false);
        frame.setCursor(Cursor.getDefaultCursor());
    }

    public static void main(String args[]) throws Exception {
        new DelineateApplication(args[0], args[1]);
    }

}