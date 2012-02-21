/*
 * PotraceSvgOptimizer.java
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
import org.w3c.dom.svg.*;

import java.io.*;

/**
 * SVG optimizer for Potrace output.
 * @author robmckinnon@users.sourceforge.net
 */
public class PotraceSvgOptimizer extends SvgOptimizer {

    public void optimize(File file, SVGDocument svgDocument) {
        try {
            long start = System.currentTimeMillis();

            SVGSVGElement rootElement = svgDocument.getRootElement();
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(file.getPath())));

            writeDocumentStart(w, rootElement);

            pathCount = writePaths(rootElement, w);

            w.println("</svg>");
            w.flush();
            w.close();

            System.out.println("optimizing took " + (System.currentTimeMillis() - start));

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private int writePaths(SVGSVGElement rootElement, PrintWriter w) {
        NodeList childNodes = rootElement.getChildNodes();
        int pathCount = 0;

        for(int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if(node instanceof SVGGElement) {
                SVGGElement groupElement = (SVGGElement)node;
                String fill = groupElement.getAttribute("fill");
                String stroke = groupElement.getAttribute("stroke");
                String transform = groupElement.getAttribute("transform");
                w.println("<g transform=\"" + transform + "\" fill=\"" + fill + "\" stroke=\"" + stroke + "\">");
                childNodes = groupElement.getChildNodes();
            }
        }

        for(int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if(node instanceof SVGPathElement) {
                pathCount++;
                SVGPathElement path = (SVGPathElement)node;

                w.print("<path d=\"");
                String pathText = path.getAttribute("d");
                w.print(pathText);
                w.println("\"/>");
            }
        }

        w.println("</g>");

        return pathCount;
    }

    public void adjustDimensions(SVGDocument svgDocument) {
        SVGSVGElement rootElement = svgDocument.getRootElement();

        adjustDimension(rootElement.getWidth().getBaseVal());
        adjustDimension(rootElement.getHeight().getBaseVal());
    }

    private void adjustDimension(SVGLength baseVal) {
        String valueAsString = baseVal.getValueAsString();
        valueAsString = valueAsString.substring(0, valueAsString.indexOf('.'));
        baseVal.setValueAsString(valueAsString);
    }

}