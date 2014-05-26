package com.qualia.view;

import com.qualia.model.Metadata;
import com.qualia.model.OptionTableModel;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import java.awt.*;

public class VtkView  extends JDialog {
    Metadata mTargetMetadata;

    public VtkView(Metadata target){
        mTargetMetadata = target;


        JPanel paneLeft = new JPanel();
        getContentPane().add(paneLeft, BorderLayout.WEST);

        JXTable optionTable = new JXTable(new OptionTableModel());
        optionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        optionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        paneLeft.add(optionTable);

        JPanel paneCenter = new JPanel();
        paneCenter.setLayout(new BorderLayout());
        getContentPane().add(paneCenter, BorderLayout.CENTER);

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
