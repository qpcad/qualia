package com.qualia.view;

import com.qualia.model.Metadata;
import vtk.*;

public class VtkVolumeRenderPanel extends vtkRenderWindowPanel {
    private Metadata mModel;

    public VtkVolumeRenderPanel(Metadata model){
        setInteractorStyle(new vtkInteractorStyleTrackballCamera());
        this.mModel = model;
    }

    public void render(vtkImageData image) {


        vtkPiecewiseFunction opacityTransferFunction = new vtkPiecewiseFunction();
        opacityTransferFunction.AddPoint(0, 0.0);
        //opacityTransferFunction.AddPoint(8, 0.0);
        //opacityTransferFunction.AddPoint(16, 0.0);
        //opacityTransferFunction.AddPoint(24, 0.0);
        //opacityTransferFunction.AddPoint(32, 0.0);
        //opacityTransferFunction.AddPoint(48, 0.0);
        //opacityTransferFunction.AddPoint(64, 0.0);
        //opacityTransferFunction.AddPoint(80, 0.01);
        opacityTransferFunction.AddPoint(96, 0.005);
        //opacityTransferFunction.AddPoint(102, 0.05);
        //opacityTransferFunction.AddPoint(128, 0.1);
        //opacityTransferFunction.AddPoint(144, 0.4);
        opacityTransferFunction.AddPoint(160, 0.1);
        //opacityTransferFunction.AddPoint(176, 0.6);
        //opacityTransferFunction.AddPoint(192, 0.7);
        //opacityTransferFunction.AddPoint(208, 1.0);
        //opacityTransferFunction.AddPoint(224, 0.9);
        //opacityTransferFunction.AddPoint(240, 1.0);
        //opacityTransferFunction.AddPoint(254, 1.0);
        opacityTransferFunction.AddPoint(255, 1.0);


        // Create transfer mapping scalar value to color
        vtkColorTransferFunction colorTransferFunction = new vtkColorTransferFunction();
        colorTransferFunction.AddRGBPoint(0.0, 0.0, 0.0, 0.0);
        //colorTransferFunction.AddRGBPoint(8.0, 0.0, 0.0, 0.0);
        //colorTransferFunction.AddRGBPoint(16.0, 1.0, 0.0, 0.0);
        //colorTransferFunction.AddRGBPoint(24.0, 1.0, 0.5, 0.5);
        //colorTransferFunction.AddRGBPoint(32.0, 0.0, 0.0, 0.5);
        //colorTransferFunction.AddRGBPoint(48.0, 0.0, 0.0, 0.5);
        //colorTransferFunction.AddRGBPoint(64.0, 0.0, 0.0, 0.5);
        colorTransferFunction.AddRGBPoint(96.0, 0.0, 0.0, 1.5);
        //colorTransferFunction.AddRGBPoint(102.0, 0.0, 0.0, 1.0);
        colorTransferFunction.AddRGBPoint(128.0, 1.0, 0.0, 1.0);
        colorTransferFunction.AddRGBPoint(144.0, 1.5, 1.5, 0.0);
        //colorTransferFunction.AddRGBPoint(160.0, 1.5, 1.5, 0.0);
        //colorTransferFunction.AddRGBPoint(176.0, 1.0, 1.0, 1.0);
        colorTransferFunction.AddRGBPoint(192.0, 2.5, 2.5, 2.5);
        //colorTransferFunction.AddRGBPoint(208.0, 2.5, 2.5, 2.5);
        //colorTransferFunction.AddRGBPoint(224.0, 1.0, 1.0, 1.0);
        //colorTransferFunction.AddRGBPoint(240.0, 1.0, 1.0, 1.0);
        //colorTransferFunction.AddRGBPoint(255.0, 1.0, 1.0, 1.0);

        // The property describes how the data will look
        vtkVolumeProperty volumeProperty = new vtkVolumeProperty();
        volumeProperty.SetColor(colorTransferFunction);
        volumeProperty.SetScalarOpacity(opacityTransferFunction);
        volumeProperty.ShadeOn();
        volumeProperty.SetInterpolationTypeToLinear();

        // The mapper / ray cast function know how to render the data
        vtkVolumeRayCastCompositeFunction compositeFunction = new vtkVolumeRayCastCompositeFunction();
        vtkVolumeRayCastMapper volumeMapper = new vtkVolumeRayCastMapper();
        vtkImageCast cast = new vtkImageCast();

        volumeMapper.SetVolumeRayCastFunction(compositeFunction);

        cast.SetOutputScalarTypeToUnsignedChar();
        cast.SetInputData(image);
        cast.Update();

        volumeMapper.SetInputData(cast.GetOutput());

        // The volume holds the mapper and the property and
        // can be used to position/orient the volume
        vtkVolume volume = new vtkVolume();
        volume.SetMapper(volumeMapper);
        volume.SetProperty(volumeProperty);


        vtkRenderer ren = new vtkRenderer();

        this.GetRenderWindow().GetRenderers().RemoveAllItems();
        this.GetRenderWindow().AddRenderer(ren);


        if (ren.GetViewProps().GetNumberOfItems() > 0)
            ren.RemoveVolume(ren.GetViewProps().GetLastProp());
        ren.AddVolume(volume);
        ren.ResetCamera(volume.GetBounds());

        Render();
    }
}
