package com.qualia.view;

import com.qualia.model.Metadata;

import javax.swing.*;
import java.awt.*;

public class VtkView  extends JDialog {
    Metadata mTargetMetadata;

    public VtkView(Metadata target){
        mTargetMetadata = target;

        getContentPane().setLayout(new GridLayout(2,2));

        VtkSliceRenderPanel panelTopLeft = new VtkSliceRenderPanel(target);
        panelTopLeft.setOrientation(VtkSliceRenderPanel.ORIENTATION_XY);
        getContentPane().add(panelTopLeft);

        VtkSliceRenderPanel panelTopRight = new VtkSliceRenderPanel(target);
        getContentPane().add(panelTopRight);

        VtkSliceRenderPanel panelBottomLeft = new VtkSliceRenderPanel(target);
        panelBottomLeft.setOrientation(VtkSliceRenderPanel.ORIENTATION_YZ);
        getContentPane().add(panelBottomLeft);

        VtkSliceRenderPanel panelBottomRight = new VtkSliceRenderPanel(target);
        panelBottomRight.setModel(mTargetMetadata);
        panelBottomRight.setOrientation(VtkSliceRenderPanel.ORIENTATION_XZ);
        getContentPane().add(panelBottomRight);
    }
}
