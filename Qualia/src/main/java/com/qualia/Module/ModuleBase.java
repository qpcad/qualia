package com.qualia.Module;

import org.itk.itkcommon.itkImageSS3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public abstract class ModuleBase extends JToolBar implements Runnable {
    String name = "";
    itkImageSS3 mOutput = null;
    HashMap<String, String> optionMap;

    JPanel mPaneConfig;
    JProgressBar mProgressBar;

    ModuleBase() {
        optionMap = new HashMap<String, String>();

        this.setBorderPainted(true);
        this.setOrientation(JToolBar.VERTICAL);
    }

    public void addToConfigPanel(Component component) {
        mPaneConfig.add(component);
    }

    public void initializePanel() {
        this.setToolTipText(name);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(name);
        this.add(label);

        mPaneConfig = new JPanel();
        mPaneConfig.setLayout(new BoxLayout(mPaneConfig, BoxLayout.Y_AXIS));
        this.add(mPaneConfig);

        mProgressBar = new JProgressBar();
        mProgressBar.setStringPainted(true);
        this.add(mProgressBar);

        JButton btnApply = new JButton("Apply");
        btnApply.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                applyModule();
            }
        });
        this.add(btnApply);
    }

    public void applyModule() {
        // Run Module
        Thread th = new Thread(this);
        th.start();
    }

    public HashMap<String, String> getOptionMap() {
        return optionMap;
    }

    public itkImageSS3 getOutput() {
        return mOutput;
    }

    public int getProgress() {
        return mProgressBar.getValue();
    }

    public void setProgress(int progress) {
        mProgressBar.setValue(progress);
    }

    @Override
    public abstract void run();
}
