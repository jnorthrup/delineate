/*
 * ColorUtilities.java
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

import java.awt.Color;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * Colour utility methods.
 * @author robmckinnon@users.sourceforge.net
 */
public class ColorUtilities {

    private static final int THRESHOLD = 3 * Integer.parseInt("66", 16) + 1;

    private static Map hexToColorMap = new HashMap(301);

    private static final Comparator comparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            Color color = (Color)o1;
            Color otherColor = (Color)o2;

            return color.getRGB() - otherColor.getRGB();
        }
    };

    public static Color getColor(String hexColor) {
        Color color;

        if(hexColor == null) {
            color = null;
        } else if(hexToColorMap.containsKey(hexColor)) {
            color = (Color)hexToColorMap.get(hexColor);
        } else {
            int red = Integer.parseInt(hexColor.substring(0, 2), 16);
            int green = Integer.parseInt(hexColor.substring(2, 4), 16);
            int blue = Integer.parseInt(hexColor.substring(4), 16);
            color = new Color(red, green, blue);
            hexToColorMap.put(hexColor, color);
        }

        return color;
    }

    public static Color getForeground(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int colorInt = red + green + blue;

        if((colorInt < THRESHOLD && green <= 153 && red <= 204) || (red < 112 && blue < 112 && green < 112)) {
            return Color.lightGray;
        } else {
            return Color.black;
        }
    }


    public static String getHexColor(Color color) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getHexString(color.getRed()));
        buffer.append(getHexString(color.getGreen()));
        buffer.append(getHexString(color.getBlue()));
        return buffer.toString();
    }

    private static String getHexString(int integer) {
        String hex = Integer.toHexString(integer).toUpperCase();

        if(hex.length() == 1) {
            hex = '0' + hex;
        }

        return hex;
    }

    public static void sortColors(Color[] colors) {
        Arrays.sort(colors, comparator);        
    }

}
