/*
 * Parameter.java
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

import net.sf.delineate.utility.FileUtilities;


/**
 * Represents a tracing tool command parameter.
 * @author robmckinnon@users.sourceforge.net
 */
public class Parameter implements Comparable {
    private String function;
    private String name;
    private boolean enabled;
    private String defaultValue;
    private String value;

    public Parameter(String name, boolean enabled, String value, String function) {
        this.name = name;
        this.enabled = enabled;
        this.defaultValue = value;
        this.value = value;
        this.function = function;
    }

    public boolean isInputFileParameter() {
        return function.equals(Command.INPUT_FILE_PARAMETER);
    }

    public boolean isOutputFileParameter() {
        return function.equals(Command.OUTPUT_FILE_PARAMETER);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getFunction() {
        return function;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getValue() {
        return value;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String parameterSetting(String optionIndicator) {
        if(enabled) {
            String settingValue = value;
            if(isInputFileParameter()) {
                return FileUtilities.normalizeFileName(value);
            } else {
                String option = optionIndicator + name + ' ';
                if(isOutputFileParameter()) {
                    settingValue = FileUtilities.normalizeFileName(value);
                }
                return (value.length() == 0) ? option : option + settingValue + ' ';
            }
        } else {
            return "";
        }
    }

    public String parameterOption(String optionIndicator) {
        if(enabled && !isInputFileParameter()) {
            return optionIndicator + name;
        } else {
            return "";
        }
    }

    public String parameterOptionValue() {
        if(enabled) {
            String settingValue = value;
            if(isInputFileParameter()) {
                return value;
            } else {
                if(isOutputFileParameter()) {
                    settingValue = value;
                }
                return (value.length() == 0) ? "" : settingValue;
            }
        } else {
            return "";
        }
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        if(obj != null && obj instanceof Parameter) {
            return name.equals(((Parameter)obj).name);
        } else {
            return false;
        }
    }

    public int compareTo(Object obj) {
        if(obj != null) {
            if(name.equals("input-file")) {
                return 1;
            } else if(obj instanceof Parameter) {
                return name.compareTo(((Parameter)obj).name);
            } else {
                return name.compareTo(obj.toString());
            }
        } else {
            throw new IllegalStateException("illegal state, this is not a Parameter " + String.valueOf(obj));
        }
    }

}
