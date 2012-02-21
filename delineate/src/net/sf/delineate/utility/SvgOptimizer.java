/*
 * SvgOptimizer.java
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

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;

/**
 * SVG optimizer abstract class.
 * @author robmckinnon@users.sourceforge.net
 */
public abstract class SvgOptimizer {
    public static String NO_GROUPS = "no groups";
    public static String ONE_GROUP = "one group";
    public static String COLOR_GROUPS = "group by color";
    public static String STYLE_DEFS = "create style definitions";
    protected int pathCount = 0;
    protected String type = SvgOptimizer.NO_GROUPS;
    protected String background = null;
    private int thresholdPercent = 50;

    public int getPathCount() {
        return pathCount;
    }

    public Color[] getColors() {
        return null;
    }

    public void setBackgroundColor(String color) {
        background = color;
    }

    public void setCenterlineEnabled(boolean enabled) {
    }

    public void setOptimizeType(String type) {
        this.type = type;
    }

    public boolean groupByColor() {
        return false;
    }

    public void addBackground(SVGDocument document) {
    }

    public abstract void optimize(File file, SVGDocument svgDocument);

    private void writeWidthAndHeight(PrintWriter w, String width, String height) {
        w.print("width=\"");
        w.print(width);
        w.print("\" height=\"");
        w.print(height);
        w.print("\"");
    }

    private void writeViewBox(PrintWriter w, String width, String height) {
        w.print("viewBox=\"0 0 ");
        w.print(width);
        w.print(" ");
        w.print(height);
        w.print("\"");
    }

    protected void writeDocumentStart(PrintWriter w, SVGSVGElement rootElement) {
        String width = rootElement.getWidth().getBaseVal().getValueAsString();
        String height = rootElement.getHeight().getBaseVal().getValueAsString();

        w.println("<?xml version=\"1.0\" standalone=\"no\"?>");
        w.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");

        w.print("<svg xmlns=\"http://www.w3.org/2000/svg\" ");
//        writeWidthAndHeight(w, width, height);
        writeViewBox(w, width, height);
        w.println(">");

        if(background != null) {
            w.print("<rect fill=\"#");
            w.print(background);
            w.print("\" ");
            writeWidthAndHeight(w, width, height);
            w.println("/>");
        }
    }

    public void adjustDimensions(SVGDocument svgDocument) {
//        SVGSVGElement rootElement = svgDocument.getRootElement();
//        rootElement.removeAttribute("width");
//        rootElement.removeAttribute("height");
    }

    public void setThresholdPercent(int value) {
        thresholdPercent = value;
    }

    public int getThresholdPercent() {
        return thresholdPercent;
    }

}