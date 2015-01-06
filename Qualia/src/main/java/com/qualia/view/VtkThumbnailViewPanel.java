package com.qualia.view;

import vtk.vtkImageData;
import vtk.vtkImageViewer;
import vtk.vtkRenderWindowPanel;
import vtk.vtkRenderer;

import javax.swing.*;
import java.awt.*;


public class VtkThumbnailViewPanel extends JPanel {
    private vtkImageData image;
    private int mColorLevel = -500;
    private int mColorWindow = 3000;

    public VtkThumbnailViewPanel() {

    }

    public void render(vtkImageData input) {
        if (image == input) return;
        else image = input;

        removeAll();
        for (int sliceIdx = 0; sliceIdx < image.GetDimensions()[2]; sliceIdx++) {
            System.out.println(sliceIdx);
            vtkImageButton button = new vtkImageButton();
            button.setImage(image);
            button.setText(String.valueOf(sliceIdx));
            button.setPreferredSize(new Dimension(64, 64));
            add(button);
        }
    }

    public class vtkImageButton extends JButton {
        private vtkImageViewer mVtkImageViewer;
        private vtkRenderWindowPanel mRenderWindowPanel;

        public vtkImageButton() {
            mVtkImageViewer = new vtkImageViewer();
            mRenderWindowPanel = new vtkRenderWindowPanel();
            add(mRenderWindowPanel);
        }

        public void setImage(vtkImageData input) {
            if (mRenderWindowPanel != null && mVtkImageViewer != null) {
                mVtkImageViewer.SetInputData(input);

                mVtkImageViewer.SetColorLevel(mColorLevel);
                mVtkImageViewer.SetColorWindow(mColorWindow);


                vtkRenderer ren = mVtkImageViewer.GetRenderer();

                mRenderWindowPanel.GetRenderWindow().GetRenderers().RemoveAllItems();
                mRenderWindowPanel.GetRenderWindow().AddRenderer(ren);

                mVtkImageViewer.SetupInteractor(mRenderWindowPanel.GetRenderWindow().GetInteractor());
                mVtkImageViewer.GetRenderer().ResetCamera();
            }
        }
    }
}
