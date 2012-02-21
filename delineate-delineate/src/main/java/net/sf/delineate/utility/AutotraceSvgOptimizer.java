/*
 * AutotraceSvgOptimizer.java
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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;
import org.apache.batik.dom.svg.SVGDOMImplementation;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

/**
 * SVG optimizer for AutoTrace output.
 * @author robmckinnon@users.sourceforge.net
 */
public class AutotraceSvgOptimizer extends SvgOptimizer {

    private boolean centerlineEnabled;
    private Map colorToStyleMap = new HashMap();
    private Map colorToPathsMap = new HashMap();
    private List colorList = new ArrayList();
    private Set colorSet = new HashSet();
    private Color[] colors;

    public Color[] getColors() {
        return colors;
    }

    public void setCenterlineEnabled(boolean enabled) {
        centerlineEnabled = enabled;
    }

    public void setOptimizeType(String type) {
        this.type = type;
    }

    public boolean groupByColor() {
        return type == COLOR_GROUPS;
    }

    public void addBackground(SVGDocument document) {
        if(background != null) {
            SVGSVGElement root = document.getRootElement();
            Element rectangle = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "rect");
            rectangle.setAttributeNS(null, "width", root.getWidth().getBaseVal().getValueAsString());
            rectangle.setAttributeNS(null, "height", root.getHeight().getBaseVal().getValueAsString());
            rectangle.setAttributeNS(null, "fill", '#' + background);

            Node firstChild = root.getFirstChild();
            root.insertBefore(rectangle, firstChild);
        }
    }

    public void optimize(File file, SVGDocument svgDocument) {
        try {
            long start = System.currentTimeMillis();

            SVGSVGElement rootElement = svgDocument.getRootElement();
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(file.getPath())));

            Map styleToColorMap = new HashMap();
            List styleList = new LinkedList();

            if(extractStyles()) {
                styleToColorMap = new HashMap();
                styleList = new LinkedList();
            }

            writeDocumentStart(w, rootElement);

            if(oneGroup()) {
                if(centerlineEnabled) {
                    w.println("<g fill=\"none\">");
                } else {
                    w.println("<g stroke=\"none\">");
                }
            }

            pathCount = writePaths(rootElement, styleList, styleToColorMap, w);

            if(oneGroup()) {
                w.println("</g>");
            }

            if(extractStyles()) {
                writeStyles(w, styleList, styleToColorMap);
            }

            w.println("</svg>");
            w.flush();
            w.close();

            System.out.println("optimizing took " + (System.currentTimeMillis() - start));

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void writeStyles(PrintWriter w, List codeList, Map codeToFillMap) {
        w.println("<defs>");
        w.println("<style type=\"text/css\"><![CDATA[");

        Iterator iterator = codeList.iterator();

        while(iterator.hasNext()) {
            String name = (String)iterator.next();
            w.print(".");
            w.print(name);
            if(centerlineEnabled) {
                w.print("{stroke:#");
            } else {
                w.print("{fill:#");
            }
            w.print(codeToFillMap.get(name).toString());
            w.println("}");
        }
        w.println("]]></style>");
        w.println("</defs>");
    }

    private int writePaths(SVGSVGElement rootElement, List styleList, Map styleToColorMap, PrintWriter w) {
        NodeList childNodes = rootElement.getChildNodes();
        int pathCount = 0;
        int styleCount = 0;
        String colorText = null;
        clearColorCollections();

        for(int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if(node instanceof SVGPathElement) {
                pathCount++;
                SVGPathElement path = (SVGPathElement)node;
                String styleText = path.getAttribute("style");

                if(centerlineEnabled) {
                    colorText = styleText.substring(8, 14);
                } else {
                    colorText = styleText.substring(6, 12);
                }

                if(pathCount <= 279 && !colorSet.contains(colorText)) {
                    Color color = ColorUtilities.getColor(colorText);
                    colorSet.add(color);
                }

                if(noGroups()) {
                    w.print("<path ");
                    if(centerlineEnabled) {
                        w.print("fill=\"none\" stroke=\"#");
                    } else {
                        w.print("stroke=\"none\" fill=\"#");
                    }
                    w.print(colorText);
                } else if(oneGroup()) {
                    if(centerlineEnabled) {
                        w.print("<path stroke=\"#");
                    } else {
                        w.print("<path fill=\"#");
                    }
                    w.print(colorText);
                } else if(extractStyles()) {
                    if(!colorToStyleMap.containsKey(colorText)) {
                        String style = getStyleName(styleCount);
                        styleList.add(style);
                        colorToStyleMap.put(colorText, style);
                        styleToColorMap.put(style, colorText);
                        styleCount++;
                    }

                    w.print("<path class=\"");
                    w.print((String)colorToStyleMap.get(colorText));
                }

                if(!groupByColor()) {
                    w.print("\" d=\"");
                }

                String pathText = path.getAttribute("d");
                int index = pathText.length() - 1;
                char c = pathText.charAt(index);

                if(c == 'z') {
                    do {
                        c = pathText.charAt(--index);
                    } while(Character.isDigit(c) || Character.isWhitespace(c));

                    if(c == 'L') {
                        pathText = pathText.substring(0, index) + 'z';
                    }
                }

                if(!groupByColor()) {
                    w.print(pathText);
                    w.println("\"/>");
                } else {
                    List list;
                    if(colorToPathsMap.containsKey(colorText)) {
                        list = (List)colorToPathsMap.get(colorText);
                    } else {
                        list = new ArrayList();
                        colorList.add(colorText);
                        colorToPathsMap.put(colorText, list);
                    }

                    list.add(pathText);
                }
            }
        }

        colors = (Color[])colorSet.toArray(new Color[colorSet.size()]);

        if(groupByColor()) {
            for(Iterator iterator = colorList.iterator(); iterator.hasNext();) {
                colorText = (String)iterator.next();
                List pathList = (List)colorToPathsMap.get(colorText);
                if(centerlineEnabled) {
                    w.print("<g fill=\"none\" stroke=\"#");
                } else {
                    w.print("<g stroke=\"none\" fill=\"#");
                }
                w.print(colorText);
                w.println("\">");
                for(Iterator i = pathList.iterator(); i.hasNext();) {
                    String pathText = (String)i.next();
                    w.print("<path d=\"");
                    w.print(pathText);
                    w.println("\"/>");
                    i.remove();
                }
                w.println("</g>");
                iterator.remove();
            }
        }

        return pathCount;
    }

    private void clearColorCollections() {
        colorSet.clear();

        if(extractStyles()) {
            colorToStyleMap.clear();
        } else if(groupByColor()) {
            colorList.clear();
            colorToPathsMap.clear();
//            Iterator iterator = colorToPathsMap.values().iterator();
//            while (iterator.hasNext()) {
//                List list = (List)iterator.next();
//                list.clear();
//            }
        }
    }

    private String getStyleName(int i) {
        int first = i / 51;

        if(first == 0) {
            if(i < 26) {
                return (new Character((char)(i + 97))).toString();
            } else {
                return (new Character((char)(i + 39))).toString();
            }
        } else {
            return getStyleName(first - 1) + getStyleName(i % 51);
        }
    }

    private boolean noGroups() {
        return type == NO_GROUPS;
    }

    private boolean oneGroup() {
        return type == ONE_GROUP;
    }

    private boolean extractStyles() {
        return type == STYLE_DEFS;
    }

}