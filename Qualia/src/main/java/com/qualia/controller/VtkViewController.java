package com.qualia.controller;

import ITKTest.LungSegmentation;
import ITKTest.NoduleCandidatesDetection;
import com.qualia.helper.ItkImageArchive;
import com.qualia.model.Metadata;
import com.qualia.model.OptionTableModel;
import com.qualia.view.VtkView;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkcommon.itkImageUC3;
import org.itk.itkimageintensity.itkMaskImageFilterISS3IUC3ISS3;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import java.util.HashMap;

public class VtkViewController {
    Metadata mModel;
    VtkView dialog;

    itkImageSS3 lungImage;
    itkImageUC3 lungMaskImage;
    itkImageSS3 lungSegImage;

    public VtkViewController(JFrame frame, Metadata model) {
        mModel = model;
        dialog = new VtkView(model, this);
        dialog.pack();
        dialog.setVisible(true);

        dialog.setModel(model);
    }

    public void onModule1BtnClicked(JXTable optionTable) {
        OptionTableModel model = (OptionTableModel) optionTable.getModel();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Specular", "1");
        map.put("Diffuse", "0.6");
        map.put("Ambiant", "0.5");

        model.setOptionMap(map);

        model.fireTableDataChanged();

        System.out.println("Lung Segmentation");
        /* Lung Segmentation */
        lungImage = ItkImageArchive.getInstance().getItkImage(mModel.uId);
        LungSegmentation lungSegmentation = new LungSegmentation();
        lungSegmentation.setLungImage(lungImage);
        lungSegmentation.run();

        lungMaskImage = lungSegmentation.getLungMask();

        /* Lung Masking */
        itkMaskImageFilterISS3IUC3ISS3 maskFilter = new itkMaskImageFilterISS3IUC3ISS3();

        maskFilter.SetInput1(lungImage);
        maskFilter.SetInput2(lungMaskImage);
        maskFilter.SetOutsideValue((short) -2000);
        maskFilter.Update();

        lungSegImage = maskFilter.GetOutput();

        /* To Viewer */
        dialog.renderItkImage(lungSegImage);
    }

    public void onModule2BtnClicked(JXTable optionTable) {
        OptionTableModel model = (OptionTableModel) optionTable.getModel();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Level", "2000");
        map.put("Balance", "0.7");

        model.setOptionMap(map);

        model.fireTableDataChanged();

        if (lungImage == null || lungMaskImage == null)
            onModule1BtnClicked(optionTable);

        System.out.println("Nodule Candidates Detection");
        /* Nodule Candidates Detection */
        NoduleCandidatesDetection noduleCandidateDetection = new NoduleCandidatesDetection();
        noduleCandidateDetection.setLungImage(lungImage);
        noduleCandidateDetection.setLungMask(lungMaskImage);
        noduleCandidateDetection.run();


        /* To Viewer */
        dialog.renderItkImage(noduleCandidateDetection.getNoduleCandidatesLabel());
        //dialog.renderItkImage(lungSegImage, noduleCandidateDetection.getNoduleCandidates());
    }


}
