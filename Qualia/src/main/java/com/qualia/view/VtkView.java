package com.qualia.view;

import ITKTest.ImageProcessingUtils;
import com.qualia.controller.VtkViewController;
import com.qualia.helper.ItkImageArchive;
import com.qualia.model.Metadata;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkimageintensity.itkAddImageFilterISS3ISS3ISS3;
import org.itk.itklabelmap.itkLabelMap3;
import org.itk.itklabelmap.itkLabelMapToBinaryImageFilterLM3IUC3;
import org.itk.itkthresholding.itkThresholdImageFilterISS3;
import org.itk.itkvtkglue.itkImageToVTKImageFilterISS3;
import org.itk.itkvtkglue.itkImageToVTKImageFilterIUC3;
import vtk.vtkImageData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VtkView extends JDialog {
    public JPanel paneModule;
    private VtkViewController mController;
    private Metadata mModel;
    private VtkVolumeRenderPanel panelTopRight;
    private VtkSliceRenderPanel panelTopLeft;
    private VtkSliceRenderPanel panelBottomLeft;
    private VtkSliceRenderPanel panelBottomRight;
    private boolean volumeViewerOn = false;
    private vtkImageData vtkImageData = null;
    private vtkImageData vtkVolumeImageData = null;


    public VtkView(Metadata model, VtkViewController controller) {
        mModel = model;
        mController = controller;

        paneModule = new JPanel();
        paneModule.setPreferredSize(new Dimension(300, 100));
        paneModule.setLayout(new BoxLayout(paneModule, BoxLayout.Y_AXIS));
        getContentPane().add(paneModule, BorderLayout.WEST);


        JPanel paneCenter = new JPanel();
        paneCenter.setLayout(new BorderLayout());
        getContentPane().add(paneCenter, BorderLayout.CENTER);


        JToolBar toolBar = new JToolBar();
        paneCenter.add(toolBar, BorderLayout.NORTH);

        JButton btnSetting = new JButton("Viewer Setting");
        btnSetting.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                mController.onModuleViewerSettingBtnClicked();
            }
        });
        toolBar.add(btnSetting);

        JButton btnModule1 = new JButton("Lung Segmentation");
        btnModule1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                mController.onModule1BtnClicked();
            }
        });
        toolBar.add(btnModule1);

        JButton btnModule2 = new JButton("Nodule Candidate Detection");
        btnModule2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                mController.onModule2BtnClicked();
            }
        });
        toolBar.add(btnModule2);

        JButton btnModule3 = new JButton("Nodule Classification");
        btnModule3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                mController.onModule3BtnClicked();
            }
        });
        toolBar.add(btnModule3);

        JPanel panelVtkRenderArea = new JPanel();
        paneCenter.add(panelVtkRenderArea, BorderLayout.CENTER);

        panelVtkRenderArea.setLayout(new GridLayout(2, 2));

        panelTopLeft = new VtkSliceRenderPanel(model);
        panelTopLeft.setPreferredSize(new Dimension(400, 400));
        panelVtkRenderArea.add(panelTopLeft);

        panelTopRight = new VtkVolumeRenderPanel(model);
        panelTopRight.setPreferredSize(new Dimension(400, 400));
        panelVtkRenderArea.add(panelTopRight);

        panelBottomLeft = new VtkSliceRenderPanel(model);
        panelBottomLeft.setPreferredSize(new Dimension(400, 400));
        panelVtkRenderArea.add(panelBottomLeft);

        panelBottomRight = new VtkSliceRenderPanel(model);
        panelBottomRight.setPreferredSize(new Dimension(400, 400));
        panelVtkRenderArea.add(panelBottomRight);
    }

    public void setModel(Metadata model) {
        mModel = model;
        itkImageSS3 image = ItkImageArchive.getInstance().getItkImage(model.uId);

        // volume viewer is set to black window
        panelTopRight.Render();

        // rendering input image
        renderItkImage(image);
    }

    public void renderSlice() {
        panelTopLeft.render(vtkImageData, VtkSliceRenderPanel.ORIENTATION_XY);
        panelBottomLeft.render(vtkImageData, VtkSliceRenderPanel.ORIENTATION_YZ);
        panelBottomRight.render(vtkImageData, VtkSliceRenderPanel.ORIENTATION_XZ);
    }

    public void renderVolume() {
        panelTopRight.render(vtkVolumeImageData);
    }

    vtkImageData itkImageToVtkVolume(itkImageSS3 input) {
        vtkImageData outputImage;

        ImageProcessingUtils.getInstance().tic();

        itkThresholdImageFilterISS3 threshold_below = new itkThresholdImageFilterISS3();
        itkAddImageFilterISS3ISS3ISS3 addImageFilter = new itkAddImageFilterISS3ISS3ISS3();
        itkImageToVTKImageFilterISS3 itkVtkFilter = new itkImageToVTKImageFilterISS3();

        threshold_below.SetInput(input);
        addImageFilter.SetInput(threshold_below.GetOutput());
        addImageFilter.SetConstant((short) 2000);
        itkVtkFilter.SetInput(addImageFilter.GetOutput());

        threshold_below.ThresholdBelow((short) -2000);
        threshold_below.SetOutsideValue((short) -2000);

        itkVtkFilter.Update();

        outputImage = itkVtkFilter.GetOutput();
        ImageProcessingUtils.getInstance().toc();

        return outputImage;
    }

    vtkImageData itkLabelMapToVtkVolume(itkLabelMap3 input) {
        vtkImageData outputImage;

        itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToBinaryImageFilter = new itkLabelMapToBinaryImageFilterLM3IUC3();
        itkImageToVTKImageFilterIUC3 itkVtkFilter = new itkImageToVTKImageFilterIUC3();

        labelMapToBinaryImageFilter.SetInput(input);
        itkVtkFilter.SetInput(labelMapToBinaryImageFilter.GetOutput());
        itkVtkFilter.Update();

        outputImage = itkVtkFilter.GetOutput();

        return outputImage;
    }

    vtkImageData itkImageToVtk(itkImageSS3 image) {
        vtkImageData outputImage;

        itkImageToVTKImageFilterISS3 itkVtkFilter;

        itkVtkFilter = new itkImageToVTKImageFilterISS3();
        itkVtkFilter.SetInput(image);
        itkVtkFilter.Update();
        outputImage = itkVtkFilter.GetOutput();

        return outputImage;
    }

    public void renderItkImage(itkImageSS3 image) {
        vtkImageData = itkImageToVtk(image);
        vtkVolumeImageData = itkImageToVtkVolume(image);

        renderSlice();
        if (isVolumeViewerOn()) renderVolume();
    }

    public void onVolumeViewer() {
        volumeViewerOn = true;
        renderVolume();
    }

    public void offVolumeViewer() {
        volumeViewerOn = false;
        panelTopRight.clear();
    }

    public boolean isVolumeViewerOn() {
        return volumeViewerOn;
    }
}
