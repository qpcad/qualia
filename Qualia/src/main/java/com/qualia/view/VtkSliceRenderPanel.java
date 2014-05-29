package com.qualia.view;

import com.qualia.model.Metadata;
import vtk.vtkImageData;
import vtk.vtkImageViewer2;
import vtk.vtkInteractorStyleImage;
import vtk.vtkRenderWindowPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;


public class VtkSliceRenderPanel extends JPanel implements ChangeListener, MouseWheelListener {

    public final static int ORIENTATION_XY = 0;
    public final static int ORIENTATION_YZ = 1;
    public final static int ORIENTATION_XZ = 2;

    private Metadata mModel;
    private vtkImageViewer2 mVtkImageViewer;
    private vtkRenderWindowPanel mRenderWindowPanel;
    private JSlider mSlider;
    private int mColorLevel = -500;
    private int mColorWindow = 3000;

    public VtkSliceRenderPanel(Metadata target) {
        mModel = target;

        mVtkImageViewer = new vtkImageViewer2();
        mRenderWindowPanel = new vtkRenderWindowPanel();
        mSlider = new JSlider();
        mRenderWindowPanel.setInteractorStyle(new vtkInteractorStyleImage());

        setLayout(new BorderLayout());
        add(mRenderWindowPanel, BorderLayout.CENTER);
        add(mSlider, BorderLayout.SOUTH);

        mSlider.addChangeListener(this);
        addMouseWheelListener(this);
    }

    public void render(vtkImageData image, int orientation) {
        mRenderWindowPanel.Render();

        mVtkImageViewer.SetInputData(image);
        setOrientation(orientation);

        mVtkImageViewer.SetRenderWindow(mRenderWindowPanel.GetRenderWindow());
        mVtkImageViewer.SetupInteractor(mRenderWindowPanel.GetRenderWindow().GetInteractor());

        mVtkImageViewer.GetRenderer().ResetCamera();

        mVtkImageViewer.SetColorLevel(mColorLevel);
        mVtkImageViewer.SetColorWindow(mColorWindow);


        int sliceMin, sliceMax;
        sliceMin = mVtkImageViewer.GetSliceMin();
        sliceMax = mVtkImageViewer.GetSliceMax();

        mSlider.setMaximum(sliceMax);
        mSlider.setMinimum(sliceMin);

        setSliceIndex((sliceMax - sliceMin) / 2);

        mRenderWindowPanel.Render();
    }

    public void setOrientation(int orientation) {
        switch (orientation) {
            case ORIENTATION_XY:
                mVtkImageViewer.SetSliceOrientationToXY();
                break;
            case ORIENTATION_YZ:
                mVtkImageViewer.SetSliceOrientationToYZ();
                break;
            case ORIENTATION_XZ:
                mVtkImageViewer.SetSliceOrientationToXZ();
                break;
        }
    }

    public void setSliceIndex(int index) {
        mSlider.setValue(index);
        mVtkImageViewer.Render();
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        JSlider source = (JSlider) changeEvent.getSource();
        if (!source.getValueIsAdjusting()) {
            int value = source.getValue();
            mVtkImageViewer.SetSlice(value);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        int notches = mouseWheelEvent.getWheelRotation();
        setSliceIndex(mSlider.getValue() + notches);
    }
}
