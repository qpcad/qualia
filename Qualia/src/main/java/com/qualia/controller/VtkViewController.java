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
        mViewerSetting.setVisible(true);
        dialog.paneModule.add(mViewerSetting);

        mModuleSegmentation = new LungSegmentation(dialog, model);
        mModuleSegmentation.setVisible(false);
        dialog.paneModule.add(mModuleSegmentation);

        mModuleDetection = new NoduleDetection(dialog, mModuleSegmentation);
        mModuleDetection.setVisible(false);
        dialog.paneModule.add(mModuleDetection);

        mModuleClassification = new NoduleClassification(dialog, mModuleSegmentation, mModuleDetection);
        mModuleClassification.setVisible(false);
        dialog.paneModule.add(mModuleClassification);
    }

    public void onModule1BtnClicked() {
        mModuleSegmentation.setVisible(!mModuleSegmentation.isVisible());
    }

    public void onModule2BtnClicked() {
        mModuleDetection.setVisible(!mModuleDetection.isVisible());
    }

    public void onModule3BtnClicked() {
        mModuleClassification.setVisible(!mModuleClassification.isVisible());
    }

    public void onModuleViewerSettingBtnClicked() {
        mViewerSetting.setVisible(!mViewerSetting.isVisible());
    }
}
