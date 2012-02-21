/*
 * RuntimeUtility.java - GUI for converting raster images to SVG using AutoTrace
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

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Runtime helper methods.
 * @author robmckinnon@users.sourceforge.net
 */
public class RuntimeUtility {

    public static void execute(String command) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        waitForCompletion(process);
    }

    public static BufferedReader execute(String[] commandArray) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(commandArray, null, null);
        waitForCompletion(process);
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    private static void waitForCompletion(Process process) throws InterruptedException, IOException {
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        process.waitFor();
        if(errorReader.ready()) {
            StringBuffer buffer = new StringBuffer(errorReader.readLine());
            while(errorReader.ready()) {
                buffer.append('\n' + errorReader.readLine());
            }
            errorReader.close();
            String message = buffer.toString();
            if(message.indexOf("premature end of file") == -1) {
                throw new RuntimeException(message);
            }
        }
    }

    public static String getOutput(String[] commandArray) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(commandArray, null, null);

        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        process.waitFor();
        StringBuffer buffer = new StringBuffer();

        if(outputReader.ready()) {
            buffer.append(outputReader.readLine());
            while(outputReader.ready()) {
                buffer.append('\n' + outputReader.readLine());
            }
        }

        return buffer.toString();
    }

}
