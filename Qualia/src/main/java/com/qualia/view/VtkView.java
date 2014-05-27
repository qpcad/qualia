package com.qualia.view;

import com.qualia.controller.VtkViewController;
import com.qualia.model.Metadata;
import com.qualia.model.OptionTableModel;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VtkView  extends JDialog {
    private Metadata mTargetMetadata;
    private VtkViewController mController;


    public VtkView(Metadata target, VtkViewController controller){
        mTargetMetadata = target;
        mController = controller;


        JPanel paneLeft = new JPanel();
        getContentPane().add(paneLeft, BorderLayout.WEST);

        final JXTable optionTable = new JXTable(new OptionTableModel());
        optionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        optionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        paneLeft.add(optionTable);

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

        JButton btnModule1 = new JButton("M1");
        btnModule1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                mController.onModule1BtnClicked(optionTable);
            }
        });
        toolBar.add(btnModule1);

        JButton btnModule2 = new JButton("M2");
        btnModule2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                mController.onModule2BtnClicked(optionTable);
            }
        });
        toolBar.add(btnModule2);

        JButton btnModule3 = new JButton("M3");
        btnModule3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }
        });
        toolBar.add(btnModule3);

        JPanel panelVtkRenderArea = new JPanel();
        paneCenter.add(panelVtkRenderArea, BorderLayout.CENTER);

        panelVtkRenderArea.setLayout(new GridLayout(2,2));

        VtkSliceRenderPanel panelTopLeft = new VtkSliceRenderPanel(target);
        panelTopLeft.setOrientation(VtkSliceRenderPanel.ORIENTATION_XY);
        panelVtkRenderArea.add(panelTopLeft);

        VtkVolumeRenderPanel panelTopRight = new VtkVolumeRenderPanel(target);
        panelVtkRenderArea.add(panelTopRight);

        VtkSliceRenderPanel panelBottomLeft = new VtkSliceRenderPanel(target);
        panelBottomLeft.setOrientation(VtkSliceRenderPanel.ORIENTATION_YZ);
        panelVtkRenderArea.add(panelBottomLeft);

        VtkSliceRenderPanel panelBottomRight = new VtkSliceRenderPanel(target);
        panelBottomRight.setModel(mTargetMetadata);
        panelBottomRight.setOrientation(VtkSliceRenderPanel.ORIENTATION_XZ);
        panelVtkRenderArea.add(panelBottomRight);
    }
}
