package com.qualia.controller;

import ITKTest.LungSegmentation;
import com.qualia.helper.ItkImageArchive;
import com.qualia.model.Metadata;
import com.qualia.model.OptionTableModel;
import com.qualia.view.VtkView;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkimageintensity.itkMaskImageFilterISS3IUC3ISS3;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import java.util.HashMap;

public class VtkViewController {
    Metadata mModel;
    VtkView dialog;

    public VtkViewController(JFrame frame, Metadata model) {
        mModel = model;
        dialog = new VtkView(model, this);
        dialog.pack();
        dialog.setVisible(true);

        dialog.setModel(model);
    }

    public void onModule1BtnClicked(JXTable optionTable){
        OptionTableModel model = (OptionTableModel) optionTable.getModel();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Specular", "1");
        map.put("Diffuse", "0.6");
        map.put("Ambiant", "0.5");

        model.setOptionMap(map);

        model.fireTableDataChanged();

        System.out.println("Lung Segmentation");
        /* Lung Segmentation */
        itkImageSS3 lungImage = ItkImageArchive.getInstance().getItkImage(mModel.uId);
        LungSegmentation lungSegmentation = new LungSegmentation();
        lungSegmentation.setLungImage(lungImage);
        lungSegmentation.run();

        /* Lung Masking */
        itkMaskImageFilterISS3IUC3ISS3 maskFilter = new itkMaskImageFilterISS3IUC3ISS3();

        maskFilter.SetInput1(lungImage);
        maskFilter.SetInput2(lungSegmentation.getLungMask());
        maskFilter.SetOutsideValue((short) -2000);
        maskFilter.Update();

        itkImageSS3 lungSegImage;
        lungSegImage = maskFilter.GetOutput();

        /* To Viewer */
        dialog.renderItkImage(lungSegImage);
    }

    public void onModule2BtnClicked(JXTable optionTable){
        OptionTableModel model = (OptionTableModel) optionTable.getModel();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Level", "2000");
        map.put("Balance", "0.7");

        model.setOptionMap(map);

        model.fireTableDataChanged();
    }


}
