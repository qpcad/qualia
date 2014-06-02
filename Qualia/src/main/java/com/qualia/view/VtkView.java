package com.qualia.view;

import ITKTest.ImageProcessingUtils;
import com.qualia.controller.VtkViewController;
import com.qualia.helper.ItkImageArchive;
import com.qualia.model.Metadata;
import com.qualia.model.OptionTableModel;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkimageintensity.itkRescaleIntensityImageFilterISS3IUC3;
import org.itk.itklabelmap.itkLabelMap3;
import org.itk.itklabelmap.itkLabelMapToBinaryImageFilterLM3IUC3;
import org.itk.itkthresholding.itkThresholdImageFilterISS3;
import org.itk.itkvtkglue.itkImageToVTKImageFilterISS3;
import org.itk.itkvtkglue.itkImageToVTKImageFilterIUC3;
import org.jdesktop.swingx.JXTable;
import vtk.vtkConnectivityFilter;
import vtk.vtkImageData;
import vtk.vtkUnstructuredGrid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VtkView extends JDialog {
    private VtkViewController mController;
    private Metadata mModel;
    private VtkVolumeRenderPanel panelTopRight;
    private VtkSliceRenderPanel panelTopLeft;
    private VtkSliceRenderPanel panelBottomLeft;
    private VtkSliceRenderPanel panelBottomRight;


    public VtkView(Metadata model, VtkViewController controller) {
        mModel = model;
        mController = controller;

        JPanel paneLeft = new JPanel();
        getContentPane().add(paneLeft, BorderLayout.WEST);

        JPanel paneBoxLeft = new JPanel();
        paneBoxLeft.setLayout(new BoxLayout(paneBoxLeft, BoxLayout.Y_AXIS));
        paneLeft.add(paneBoxLeft);

        final JXTable optionTable = new JXTable(new OptionTableModel());
        optionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        optionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        paneBoxLeft.add(optionTable);

        JPanel paneCenter = new JPanel();
        paneCenter.setLayout(new BorderLayout());
        getContentPane().add(paneCenter, BorderLayout.CENTER);


        JToolBar toolBar = new JToolBar();
        paneCenter.add(toolBar, BorderLayout.NORTH);

        JButton btnSetting = new JButton("Viewer Setting");
        btnSetting.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
            }
        });
        toolBar.add(btnSetting);

        JButton btnModule1 = new JButton("Lung Segmentation");
        btnModule1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                mController.onModule1BtnClicked(optionTable);
            }
        });
        toolBar.add(btnModule1);

        JButton btnModule2 = new JButton("Nodule Candidate Detection");
        btnModule2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                mController.onModule2BtnClicked(optionTable);
            }
        });
        toolBar.add(btnModule2);

        JButton btnModule3 = new JButton("Nodule Classification");
        btnModule3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
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

    public void renderSlice(vtkImageData image) {
        panelTopLeft.render(image, VtkSliceRenderPanel.ORIENTATION_XY);
        panelBottomLeft.render(image, VtkSliceRenderPanel.ORIENTATION_YZ);
        panelBottomRight.render(image, VtkSliceRenderPanel.ORIENTATION_XZ);
    }

    public void renderVolume(vtkImageData image) {
        panelTopRight.render(image);
    }

    public void renderVolume(vtkUnstructuredGrid grid) {
        panelTopRight.render(grid);
    }

    vtkImageData itkImageToVtkVolume(itkImageSS3 input) {
        vtkImageData outputImage;

        ImageProcessingUtils.tic();

        itkThresholdImageFilterISS3 threshold_below = new itkThresholdImageFilterISS3();
        itkThresholdImageFilterISS3 threshold_above = new itkThresholdImageFilterISS3();
        itkImageToVTKImageFilterIUC3 itkVtkFilter = new itkImageToVTKImageFilterIUC3();
        itkRescaleIntensityImageFilterISS3IUC3 rescale = new itkRescaleIntensityImageFilterISS3IUC3();

        threshold_below.SetInput(input);
        threshold_above.SetInput(threshold_below.GetOutput());
        rescale.SetInput(threshold_above.GetOutput());
        itkVtkFilter.SetInput(rescale.GetOutput());

        threshold_below.ThresholdBelow((short) -2000);
        threshold_below.SetOutsideValue((short) -2000);

        threshold_above.ThresholdAbove((short) 1000);
        threshold_above.SetOutsideValue((short) 1000);


        itkVtkFilter.Update();

        outputImage = itkVtkFilter.GetOutput();
        ImageProcessingUtils.toc();

        return outputImage;
    }

    vtkUnstructuredGrid itkLabelMapToVtkData(itkLabelMap3 input) {
        vtkUnstructuredGrid outputGrid;
        vtkImageData outputImage;

        itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToBinaryImageFilter = new itkLabelMapToBinaryImageFilterLM3IUC3();
        itkImageToVTKImageFilterIUC3 itkVtkFilter = new itkImageToVTKImageFilterIUC3();

        ImageProcessingUtils.tic();

        labelMapToBinaryImageFilter.SetInput(input);
        itkVtkFilter.SetInput(labelMapToBinaryImageFilter.GetOutput());
        itkVtkFilter.Update();

        outputImage = itkVtkFilter.GetOutput();

        ImageProcessingUtils.toc();

        // vtk part TODO too slow and it is not w
        vtkConnectivityFilter connectivityFilter = new vtkConnectivityFilter();
        connectivityFilter.SetInputData(outputImage);
        //connectivityFilter.SetExtractionModeToAllRegions();
        //connectivityFilter.ColorRegionsOn();
        connectivityFilter.ScalarConnectivityOn();
        //connectivityFilter.SetScalarRange(1,255);
        System.out.println(connectivityFilter);

        connectivityFilter.Update();


        outputGrid = connectivityFilter.GetOutput();

        ImageProcessingUtils.toc();

        return outputGrid;
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
        renderSlice(itkImageToVtk(image));
        renderVolume(itkImageToVtkVolume(image));
    }

    public void renderItkImage(itkImageSS3 image, itkLabelMap3 labelMap) {
        renderSlice(itkImageToVtk(image));
        renderVolume(itkLabelMapToVtkData(labelMap));
    }
}
