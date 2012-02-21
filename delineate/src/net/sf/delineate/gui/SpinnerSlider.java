/*
 * SpinnerSlider.java
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

import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Component combining JSpinner and JSlider together.
 * @author robmckinnon@users.sourceforge.net
 */
public class SpinnerSlider {

    private SpinnerNumberModel model;
    private JSpinner spinner;
    private JSlider slider;
    private boolean useWholeNumbers;
    private double stepSize;
    private ChangeListener changeListener;
    private NumberFormat numberFormat;


    public SpinnerSlider(SpinnerNumberModel model) {
        this.model = model;

        initSpinner(model);
        initSlider(model);
    }

    public void setValue(String value) {
        if(useWholeNumbers) {
            model.setValue(Integer.valueOf(value));
        } else {
            model.setValue(Double.valueOf(value));
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeListener = listener;
    }

    public void setFractionDigitsLength(int fractionDigits) {
        if(numberFormat == null) {
            numberFormat = DecimalFormat.getInstance(Locale.ENGLISH);
        }

        numberFormat.setMaximumFractionDigits(fractionDigits);
        numberFormat.setMinimumFractionDigits(fractionDigits);
    }

    public void setName(String name) {
        spinner.setName(name);
    }

    public String getName() {
        return spinner.getName();
    }

    public void setEnabled(boolean enabled) {
        spinner.setEnabled(enabled);
        slider.setEnabled(enabled);
    }

    public JSpinner getSpinner() {
        return spinner;
    }

    public JSlider getSlider() {
        return slider;
    }

    public void setTooltipText(String text) {
        spinner.setToolTipText(text);
        slider.setToolTipText(text);
    }

    public String getValueAsString() {
        if(useWholeNumbers) {
            return model.getNumber().toString();
        } else {
            return numberFormat.format(model.getNumber().doubleValue());
        }
    }

    private void initSlider(SpinnerNumberModel model) {
        Number min = ((Number)model.getMinimum());
        Number max = ((Number)model.getMaximum());
        Number value = ((Number)model.getValue());

        Number stepSizeNumber = model.getStepSize();
        useWholeNumbers = stepSizeNumber instanceof Integer;

        if(useWholeNumbers) {
            slider = new JSlider(min.intValue(), max.intValue(), value.intValue());
            slider.setMinorTickSpacing(stepSizeNumber.intValue());
        } else {
            stepSize = stepSizeNumber.doubleValue();
            int minimum = (int)Math.round(min.doubleValue() / stepSize);
            int maximum = (int)Math.round(max.doubleValue() / stepSize);
            int intValue = (int)Math.round(value.doubleValue() / stepSize);
            slider = new JSlider(minimum, maximum, intValue);
            slider.setMinorTickSpacing(stepSizeNumber.intValue());
        }

        Dimension size = slider.getPreferredSize();
        size = new Dimension((int)(size.getWidth() * .60), (int)size.getHeight());
        slider.setPreferredSize(size);
        slider.setSnapToTicks(true);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateSpinnerState();
            }
        });
    }

    private void initSpinner(SpinnerNumberModel model) {
        spinner = new JSpinner(model);
        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateSliderState();
            }
        });
    }

    private void updateSliderState() {
        if(!slider.getValueIsAdjusting()) {
            if(useWholeNumbers) {
                int value = ((Number)model.getValue()).intValue();
                slider.setValue(value);
            } else {
                double value = ((Number)model.getValue()).doubleValue();
                int intValue = (int)Math.round(value / stepSize);
                slider.setValue(intValue);
            }

            if(changeListener != null) {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        }
    }

    private void updateSpinnerState() {
        int value = slider.getValue();

        if(useWholeNumbers) {
            model.setValue(new Integer(value));
        } else {
            double doubleValue = value * stepSize;
            model.setValue(new Double(doubleValue));
        }

        if(changeListener != null) {
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }

}
