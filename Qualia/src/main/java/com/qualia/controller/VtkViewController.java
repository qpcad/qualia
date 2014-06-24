package com.qualia.controller;

import com.qualia.Module.LungSegmentation;
import com.qualia.Module.NoduleClassification;
import com.qualia.Module.NoduleDetection;
import com.qualia.Module.ViewerSetting;
import com.qualia.model.Metadata;
import com.qualia.view.VtkView;

import javax.swing.*;

public class VtkViewController {
    Metadata mModel;
    VtkView dialog;

    private ViewerSetting mViewerSetting;
    private LungSegmentation mModuleSegmentation;
    private NoduleDetection mModuleDetection;
    private NoduleClassification mModuleClassification;

    public VtkViewController(JFrame frame, Metadata model) {
        mModel = model;
        dialog = new VtkView(model, this);
        dialog.pack();
        dialog.setVisible(true);

        dialog.setModel(model);

        mViewerSetting = new ViewerSetting(dialog);
        dialog.paneModule.add(mViewerSetting);
        dialog.paneModule.updateUI();

        mModuleSegmentation = new LungSegmentation(dialog, model);
        mModuleDetection = new NoduleDetection(dialog, mModuleSegmentation);
        mModuleClassification = new NoduleClassification(dialog, mModuleSegmentation, mModuleDetection);
    }

    public void onModule1BtnClicked() {
        dialog.paneModule.add(mModuleSegmentation);
        dialog.paneModule.updateUI();
    }

    public void onModule2BtnClicked() {
        if (mModuleSegmentation.getOutput() == null) onModule1BtnClicked();

        dialog.paneModule.add(mModuleDetection);
        dialog.paneModule.updateUI();
    }

    public void onModule3BtnClicked() {
        if (mModuleDetection.getOutput() == null) onModule2BtnClicked();

        dialog.paneModule.add(mModuleClassification);
        dialog.paneModule.updateUI();
    }

    public void onModuleViewerSettingBtnClicked() {
        dialog.paneModule.add(mViewerSetting);
        dialog.paneModule.updateUI();
    }
}
