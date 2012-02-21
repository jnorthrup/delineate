/*
 * Command.java
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
package net.sf.delineate.command;

import java.util.*;

/**
 * Represents tracing tool command.
 * @author robmckinnon@users.sourceforge.net
 */
public class Command {

    public static final String INPUT_FILE_PARAMETER = "input-file";
    public static final String OUTPUT_FILE_PARAMETER = "output-file";
    public static final String BACKGROUND_COLOR_PARAMETER = "background-color";
    public static final String CENTERLINE_PARAMETER = "centerline";

    private CommandChangeListener changeListener;
    private String commandName;
    private String tracingApplication;
    private String optionIndicator = "-";

    private Parameter[] parameters;

    /** key is function or name, value is parameter */
    private Map parameterMap = new HashMap();

    int parameterCount = 0;

    public Command(String commandName, String optionIndicator, int totalParameterCount, CommandChangeListener listener) {
        this.optionIndicator = optionIndicator;
        parameters = new Parameter[totalParameterCount];
        changeListener = listener;
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    public void addParameter(String name, boolean enabled, String value, String function) {
        if(parameterCount == parameters.length) {
            throw new IllegalStateException("Command can only hold " + parameters.length + " parameters.");
        }

        Parameter parameter = new Parameter(name, enabled, value, function);
        parameters[parameterCount] = parameter;
        parameterCount++;

        if(function.length() > 0) {
            parameterMap.put(function, parameter);
        }

        parameterMap.put(name, parameter);

        if(parameterCount == parameters.length) {
            Arrays.sort(parameters);
        }
    }

    public void setParameterEnabled(String name, boolean enabled, boolean notify) {
        Parameter parameter = getParameter(name);

        if(parameter.isEnabled() != enabled) {
            parameter.setEnabled(enabled);
        }
        if(notify) {
            changeListener.enabledChanged(parameter);
        }
    }

    public void setParameterValue(String name, String value, boolean notify) {
        Parameter parameter = getParameter(name);

        if(!parameter.getValue().equals(value)) {
//            if(name.equals(Command.INPUT_FILE_PARAMETER) || name.equals(Command.OUTPUT_FILE_PARAMETER)) {
//                value = FileUtilities.normalizeFileName(value);
//            }
            parameter.setValue(value);

            if(notify) {
                changeListener.valueChanged(parameter);
            }
        }
    }

    public String getCommand() {
        StringBuffer buffer = new StringBuffer(tracingApplication + " ");
        for (Parameter parameter : parameters) {
            String parameterSetting = parameter.parameterSetting(optionIndicator);
            buffer.append(parameterSetting);
        }
        String command = buffer.toString();
        return command;
    }

    public String[] getCommandAsArray() {
        List commandList = new ArrayList();

        commandList.add(tracingApplication);

        for (Parameter parameter : parameters) {
            String option = parameter.parameterOption(optionIndicator);

            if (option.length() > 0) {
                commandList.add(option);
            }

            String value = parameter.parameterOptionValue();
            if (value.length() > 0) {
                commandList.add(value);
            }
        }

        String[] commandArray = (String[])commandList.toArray(new String[commandList.size()]);
        return commandArray;
    }

    private Parameter getParameter(String name) {
        return (Parameter)parameterMap.get(name);
//        if(name.startsWith("input")) {
//            return parameters[parameters.length - 1];
//        } else {
//            int index = Arrays.binarySearch(parameters, name);
//            Parameter parameter = parameters[index];
//            return parameter;
//        }
    }

    public boolean getParameterEnabled(String name) {
        Parameter parameter = getParameter(name);
        if(parameter == null) {
            return false;
        } else {
            return parameter.isEnabled();
        }
    }

    public String getParameterValue(String name) {
        return getParameter(name).getValue();
    }

    public void setCommandDefaultValues() {
        for (Parameter parameter : parameters) {
            if (!parameter.isInputFileParameter() && !parameter.isOutputFileParameter()) {
                setParameterValue(parameter.getName(), parameter.getDefaultValue(), true);
            }
        }
    }

    public void setCommand(String command) {
        for (Parameter parameter : parameters) {
            if (!parameter.isInputFileParameter()) {
                setParameterEnabled(parameter.getName(), false, true);
            }
        }

        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        tokenizer.nextToken();
        String name = tokenizer.nextToken();
        while(tokenizer.hasMoreTokens()) {
            if(name.startsWith(optionIndicator)) {
                name = name.substring(optionIndicator.length());
                setParameterEnabled(name, true, true);

                String value = tokenizer.nextToken();
                if(value.charAt(0) != '-') {
                    if(!getParameter(name).isOutputFileParameter()) {
                        setParameterValue(name, value, true);
                    }

                    if(value.charAt(0) == '\"') {
                        while(value.charAt(value.length() - 1) != '\"') {
                            value = tokenizer.nextToken(); // absorb output "file name"
                        }
                    }

                    name = tokenizer.nextToken();

                } else {
                    name = value;
                }
            } else if(name.charAt(0) == '\"') {
                name += ' ' + tokenizer.nextToken();
            }
        }
    }

    public void setTracingApplication(String path) {
        this.tracingApplication = path;
    }

    /**
     * For listening to command changes.
     */
    public interface CommandChangeListener {
        void enabledChanged(Parameter parameter);
        void valueChanged(Parameter parameter);
    }

}
