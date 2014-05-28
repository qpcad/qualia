package com.qualia.view;

import com.qualia.controller.VtkViewController;
import com.qualia.helper.VtkImageArchive;
import com.qualia.model.Metadata;
import com.qualia.model.OptionTableModel;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkvtkglue.itkImageToVTKImageFilterISS3;
import org.jdesktop.swingx.JXTable;
import vtk.vtkImageData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VtkView  extends JDialog {
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

        panelVtkRenderArea.setLayout(new GridLayout(2,2));

        panelTopLeft = new VtkSliceRenderPanel(model);
        panelTopLeft.setPreferredSize(new Dimension(400,400));
        panelVtkRenderArea.add(panelTopLeft);

        panelTopRight = new VtkVolumeRenderPanel(model);
        panelTopRight.setPreferredSize(new Dimension(400,400));
        panelVtkRenderArea.add(panelTopRight);

        panelBottomLeft = new VtkSliceRenderPanel(model);
        panelBottomLeft.setPreferredSize(new Dimension(400,400));
        panelVtkRenderArea.add(panelBottomLeft);

        panelBottomRight = new VtkSliceRenderPanel(model);
        panelBottomRight.setPreferredSize(new Dimension(400,400));
        panelVtkRenderArea.add(panelBottomRight);
    }

    public void setModel(Metadata model) {
        mModel = model;
        vtkImageData image = VtkImageArchive.getInstance().getVtkImage(model.uId);

        render(image);
    }

    public void render(vtkImageData image) {
        panelTopRight.render(image);

        panelTopLeft.render(image, VtkSliceRenderPanel.ORIENTATION_XY);
        panelBottomLeft.render(image, VtkSliceRenderPanel.ORIENTATION_YZ);
        panelBottomRight.render(image, VtkSliceRenderPanel.ORIENTATION_XZ);
    }


    public void renderItkImage(itkImageSS3 image) {
        vtkImageData outputImage;

        itkImageToVTKImageFilterISS3 itkVtkFilter;

        itkVtkFilter = new itkImageToVTKImageFilterISS3();
        itkVtkFilter.SetInput(image);
        itkVtkFilter.Update();
        outputImage = itkVtkFilter.GetOutput();

        render(outputImage);
    }
}
