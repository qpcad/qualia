package com.qualia.view;

import com.qualia.helper.VtkImageArchive;
import com.qualia.model.Metadata;
import vtk.vtkImageViewer2;
import vtk.vtkInteractorStyleImage;
import vtk.vtkRenderWindowPanel;

public class VtkSliceRenderPanel extends vtkRenderWindowPanel {

    public final static int ORIENTATION_XY = 0;
    public final static int ORIENTATION_YZ = 1;
    public final static int ORIENTATION_XZ = 2;

    private Metadata mModel;
    private vtkImageViewer2 mVtkImageViewer;
    private int mColorLevel = -500;
    private int mColorWindow = 3000;

    public VtkSliceRenderPanel(Metadata model) {
        super();
        mVtkImageViewer = new vtkImageViewer2();
        setInteractorStyle(new vtkInteractorStyleImage());

        setModel(model);
    }

    public void setModel(Metadata model){
        mModel = model;

        mVtkImageViewer.SetInputData(VtkImageArchive.getInstance().getVtkImage(mModel.uId));

        mVtkImageViewer.GetRenderer().ResetCamera();

        mVtkImageViewer.SetRenderWindow(this.GetRenderWindow());
        mVtkImageViewer.SetupInteractor(this.GetRenderWindow().GetInteractor());

        mVtkImageViewer.SetColorLevel(mColorLevel);
        mVtkImageViewer.SetColorWindow(mColorWindow);

        int sliceMin, sliceMax;
        sliceMin = mVtkImageViewer.GetSliceMin();
        sliceMax = mVtkImageViewer.GetSliceMax();

        mVtkImageViewer.SetSlice((sliceMax-sliceMin)/2);

    }

    public void setOrientation(int orientation){
        switch (orientation){
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

    public void setSliceIndex(int index){
        mVtkImageViewer.SetSlice(index);
    }
}
