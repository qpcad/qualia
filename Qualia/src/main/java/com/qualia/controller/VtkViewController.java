package com.qualia.controller;

import com.qualia.Module.*;
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
    private AnnNoduleClassification mAnnModuleClassification;

    public VtkViewController(JFrame frame, Metadata model) {
        mModel = model;
        dialog = new VtkView(model, this);
        dialog.pack();
        dialog.setVisible(true);

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

        mAnnModuleClassification = new AnnNoduleClassification(dialog, mModuleSegmentation, mModuleDetection);
        mAnnModuleClassification.setVisible(false);
        dialog.paneModule.add(mAnnModuleClassification);

        dialog.setModel(model);
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

    public void onModule4BtnClicked() {
        mAnnModuleClassification.setVisible(!mAnnModuleClassification.isVisible());
    }
}
