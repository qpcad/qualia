package com.qualia.Module;

import com.qualia.view.VtkView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by taznux on 2014. 6. 24..
 */
public class ViewerSetting extends ModuleBase {
    VtkView mDialog;
    JRadioButton mRadioButton;

    public ViewerSetting(VtkView dialog) {
        name = "Viewer Setting";

        mDialog = dialog;

        initializePanel();

        mRadioButton = new JRadioButton("Volume Viewer");
        addToConfigPanel(mRadioButton);

        mRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setProgress(0);
            }
        });
    }

    @Override
    public void run() {
        setProgress(0);
        if (mRadioButton.isSelected()) {
            setProgress(30);
            mDialog.onVolumeViewer();
            setProgress(70);
        } else {
            setProgress(50);
            mDialog.offVolumeViewer();
        }
        setProgress(100);
    }
}
