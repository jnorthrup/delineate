/*
 * XPathTool.java - Util for evaluating XPath expressions
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Util for evaluating XPath expressions.
 *
 * @author robmckinnon@users.sourceforge.net
 */
public class XPathTool {

  private Document document;
  private String xpathPrefix = "";


  public static Document parse(File file) throws ParserConfigurationException, IOException, SAXException {
    return parse(new InputSource(new FileInputStream(file)));
  }

  /**
   * Creates parser, parses input source and returns resulting document.
   */
  public static Document parse(InputSource source) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);

    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(source);
  }


  /**
   * Returns string result of enclosing expression in a XPath <code>string</code> function and evaluating it.
   */
  public static String string(String expression, Document document) throws XPathExpressionException {
    return XPathFactory.newInstance().newXPath().evaluate("string(" + expression + ")", document);
//        XObject xObject = XPathAPI.eval(document, "string(" + expression + ")");
//        return xObject.xstr().toString();
  }


  /**
   * Returns string result of enclosing expression in a XPath <code>count</code> function and evaluating it.
   */
  public static int count(String expression, Document document) throws XPathExpressionException {
    return ((Number) XPathFactory.newInstance().newXPath().evaluate("count(" + expression + ")", document, XPathConstants.NUMBER)).intValue();
//        XObject xObject = XPathAPI.eval(document, "count(" + expression + ")");
//        return Integer.parseInt(xObject.xstr().toString());
  }


  /**
   * Returns string result of enclosing expression in a XPath <code>count</code> function and evaluating it.
   */
  public static boolean toBoolean(String expression, Document document) throws XPathExpressionException {
    String string = XPathTool.string(expression, document);
//        boolean bool = Boolean.getBoolean(string.trim()); doesn't work for some reason
    return string.equals("true");
  }

  public static double toDouble(String expression, Document document) throws XPathExpressionException {
    return new Double(XPathTool.string(expression, document));
  }

  public static int toInt(String expression, Document document) throws XPathExpressionException {
    return new Integer(XPathTool.string(expression, document));
  }

  public XPathTool(File file) throws ParserConfigurationException, IOException, SAXException {
    this.document = XPathTool.parse(file);
  }

  public XPathTool(String url) throws IOException, SAXException, ParserConfigurationException {
    URL u = new URL(url);
    File t = File.createTempFile("delineate", ".xml");
    InputStream inputStream = u.openStream();
    FileOutputStream fileOutputStream = new FileOutputStream(t);
    byte[] bytes = new byte[1024];
    int i = 0;
    while (true) {

      int b = inputStream.read();
      if (-1 == b)
        break;
      fileOutputStream.write(b&0xff);



    }
    inputStream.close();
    fileOutputStream.close();

    System.err.println("wrote "+t);
    t.deleteOnExit();
    this.document = XPathTool.parse(t);

  }

  public void setXpathPrefix(String xpathPrefix) {
    this.xpathPrefix = xpathPrefix;
  }

  public String string(String xpathSuffix) throws XPathExpressionException {
    return XPathTool.string(xpathPrefix + xpathSuffix, document);
  }


  public int count(String xpathSuffix) throws XPathExpressionException {
    return XPathTool.count(xpathPrefix + xpathSuffix, document);
  }


  public boolean toBoolean(String xpathSuffix) throws XPathExpressionException {
    return XPathTool.toBoolean(xpathPrefix + xpathSuffix, document);
  }


  public double toDouble(String xpathSuffix) throws XPathExpressionException {
    return XPathTool.toDouble(xpathPrefix + xpathSuffix, document);
  }

  public int toInt(String xpathSuffix) throws XPathExpressionException {
    return XPathTool.toInt(xpathPrefix + xpathSuffix, document);
  }

}
