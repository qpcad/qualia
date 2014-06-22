package com.qualia.controller;

import com.qualia.Module.LungSegmentation;
import com.qualia.Module.NoduleClassification;
import com.qualia.Module.NoduleDetection;
import com.qualia.model.Metadata;
import com.qualia.model.OptionTableModel;
import com.qualia.view.VtkView;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;

public class VtkViewController {
    Metadata mModel;
    VtkView dialog;

    private LungSegmentation mModuleLung;
    private NoduleDetection mModuleDetection;
    private NoduleClassification mModuleClassification;

    public VtkViewController(JFrame frame, Metadata model) {
        mModel = model;
        dialog = new VtkView(model, this);
        dialog.pack();
        dialog.setVisible(true);

        dialog.setModel(model);

        mModuleLung = new LungSegmentation(model);
        mModuleDetection = new NoduleDetection(mModuleLung);
        mModuleClassification = new NoduleClassification(mModuleLung, mModuleDetection);
    }

    public void onModule1BtnClicked(JXTable optionTable) {
        OptionTableModel model = (OptionTableModel) optionTable.getModel();

        model.setOptionMap(mModuleLung.getOptionMap());

        model.fireTableDataChanged();

        mModuleLung.applyModule();

        dialog.renderItkImage(mModuleLung.getOutput());
    }

    public void onModule2BtnClicked(JXTable optionTable) {
        OptionTableModel model = (OptionTableModel) optionTable.getModel();

        model.setOptionMap(mModuleDetection.getOptionMap());

        model.fireTableDataChanged();

        if((mModuleLung.getOutput()==null)){
            mModuleLung.applyModule();
        }

        mModuleDetection.applyModule();

        dialog.renderItkImage(mModuleDetection.getOutput());
    }


    public void onModule3BtnClicked(JXTable optionTable) {
        OptionTableModel model = (OptionTableModel) optionTable.getModel();

        model.setOptionMap(mModuleClassification.getOptionMap());

        model.fireTableDataChanged();

        if(mModuleLung.getOutput()==null) mModuleLung.applyModule();
        if(mModuleDetection.getOutput()==null) mModuleDetection.applyModule();

        mModuleClassification.applyModule();

        /* To Viewer */
        dialog.renderItkImage(mModuleClassification.getOutput());
    }
}
