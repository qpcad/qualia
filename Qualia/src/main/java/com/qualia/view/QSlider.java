package com.qualia.view;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by taznux on 2014. 6. 24..
 */
public class QSlider extends JPanel {
    JLabel mSliderLabel = null;
    JSlider mSlider = null;
    JTextField mTextField = null;

    private int mValue = 0;

    public QSlider() {
        this.setLayout(new BorderLayout());
        mSliderLabel = new JLabel();
        add(mSliderLabel, BorderLayout.WEST);

        mSlider = new JSlider();
        mSlider.setPaintLabels(true);
        mSlider.setPaintTicks(true);
        mSlider.setPreferredSize(new Dimension(200, 50));
        add(mSlider, BorderLayout.CENTER);

        mTextField = new JTextField();
        mTextField.setPreferredSize(new Dimension(50, 16));
        mTextField.setHorizontalAlignment(JTextField.RIGHT);
        add(mTextField, BorderLayout.EAST);

        mSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                setValue(mSlider.getValue());
            }
        });

        mTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setValue(Integer.parseInt(mTextField.getText()));
            }
        });
    }

    public QSlider(int min, int max, int value, String label) {
        this();

        setLabel(label);
        setMinMax(min, max);
        setValue(value);
    }

    public void setLabel(String label) {
        mSliderLabel.setText(label);
        mSlider.setToolTipText(label);
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
        mSlider.setValue(value);
        mTextField.setText(String.valueOf(value));
    }

    public void setMaximum(int maximum) {
        mSlider.setMaximum(maximum);
    }

    public void setMinimum(int minimum) {
        mSlider.setMinimum(minimum);
    }

    public void setMinMax(int min, int max) {
        int majorTickSpacing = (int) Math.ceil((max - min) / 4);
        int minorTickSpacing = (int) Math.ceil(majorTickSpacing / 5.0);

        setMaximum(max);
        setMinimum(min);

        /*mSlider.setMajorTickSpacing(majorTickSpacing);
        mSlider.setMinorTickSpacing(minorTickSpacing);*/
    }

    public void addChangeListener(ChangeListener changeListener) {
        mSlider.addChangeListener(changeListener);
    }
}
