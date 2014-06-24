package com.qualia.view;

import com.qualia.model.Metadata;
import vtk.*;

public class VtkVolumeRenderPanel extends vtkRenderWindowPanel {
    private Metadata mModel;

    public VtkVolumeRenderPanel(Metadata model) {
        setInteractorStyle(new vtkInteractorStyleTrackballCamera());
        this.mModel = model;
    }

    public void render(vtkUnstructuredGrid grid) {
        vtkDataSetMapper mapper = new vtkDataSetMapper();
        mapper.SetInputData(grid);
        mapper.Update();

        vtkActor actor = new vtkActor();
        actor.SetMapper(mapper);

        vtkRenderer ren = new vtkRenderer();

        this.GetRenderWindow().GetRenderers().RemoveAllItems();
        this.GetRenderWindow().AddRenderer(ren);

        ren.AddActor(actor);

        Render();
    }

    public void render(vtkImageData image) {
        vtkImageCast cast = new vtkImageCast();
        cast.SetOutputScalarTypeToUnsignedShort();
        cast.SetInputData(image);
        cast.Update();

        vtkVolumeProperty volumeProperty = getVtkVolumeProperty();

        // The mapper / ray cast function know how to render the data
        vtkVolumeRayCastCompositeFunction compositeFunction = new vtkVolumeRayCastCompositeFunction();
        vtkVolumeRayCastMapper volumeMapper = new vtkVolumeRayCastMapper();


        volumeMapper.SetVolumeRayCastFunction(compositeFunction);
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

    public vtkVolumeProperty getVtkVolumeProperty() { // TODO it needs GUI for adjusting the function parameters
        vtkPiecewiseFunction opacityTransferFunction = new vtkPiecewiseFunction();
        opacityTransferFunction.AddPoint(0, 0.0);
        //opacityTransferFunction.AddPoint(8*12, 0.0);
        //opacityTransferFunction.AddPoint(16*12, 0.0);
        //opacityTransferFunction.AddPoint(24*12, 0.0);
        //opacityTransferFunction.AddPoint(32*12, 0.0);
        //opacityTransferFunction.AddPoint(48*12, 0.0);
        opacityTransferFunction.AddPoint(64 * 12, 0.0);
        opacityTransferFunction.AddPoint(80 * 12, 0.001);
        opacityTransferFunction.AddPoint(96 * 12, 0.005);
        //opacityTransferFunction.AddPoint(102*12, 0.05);
        opacityTransferFunction.AddPoint(128 * 12, 0.01);
        opacityTransferFunction.AddPoint(144 * 12, 0.1);
        opacityTransferFunction.AddPoint(160 * 12, 0.01);
        //opacityTransferFunction.AddPoint(176*12, 0.6);
        opacityTransferFunction.AddPoint(192 * 12, 0.4);
        //opacityTransferFunction.AddPoint(208*12, 1.0);
        opacityTransferFunction.AddPoint(224 * 12, 0.5);
        //opacityTransferFunction.AddPoint(240*12, 1.0);
        //opacityTransferFunction.AddPoint(254*12, 1.0);
        opacityTransferFunction.AddPoint(255 * 12, 1.0);


        // Create transfer mapping scalar value to color
        vtkColorTransferFunction colorTransferFunction = new vtkColorTransferFunction();
        colorTransferFunction.AddRGBPoint(0.0, 0.0, 0.0, 0.0);
        //colorTransferFunction.AddRGBPoint(8.0*12, 0.0, 0.0, 0.0);
        //colorTransferFunction.AddRGBPoint(16.0*12, 1.0, 0.0, 0.0);
        //colorTransferFunction.AddRGBPoint(24.0*12, 1.0, 0.5, 0.5);
        //colorTransferFunction.AddRGBPoint(32.0*12, 0.0, 0.0, 0.5);
        //colorTransferFunction.AddRGBPoint(48.0*12, 0.0, 0.0, 0.5);
        colorTransferFunction.AddRGBPoint(64.0 * 12, 0.0, 0.0, 0.5);
        colorTransferFunction.AddRGBPoint(80.0 * 12, 0.0, 0.0, 0.5);
        colorTransferFunction.AddRGBPoint(96.0 * 12, 0.0, 0.0, 1.5);
        //colorTransferFunction.AddRGBPoint(102.0*12, 0.0, 0.0, 1.0);
        colorTransferFunction.AddRGBPoint(128.0 * 12, 1.0, 0.0, 1.0);
        colorTransferFunction.AddRGBPoint(144.0 * 12, 1.5, 1.5, 0.0);
        colorTransferFunction.AddRGBPoint(160.0 * 12, 1.5, 0.0, 0.0);
        //colorTransferFunction.AddRGBPoint(176.0*12, 1.0, 1.0, 1.0);
        colorTransferFunction.AddRGBPoint(192.0 * 12, 2.5, 2.5, 2.5);
        //colorTransferFunction.AddRGBPoint(208*12.0, 2.5, 2.5, 2.5);
        colorTransferFunction.AddRGBPoint(224.0 * 12, 1.0, 1.0, 1.0);
        //colorTransferFunction.AddRGBPoint(240.0*12, 1.0, 1.0, 1.0);
        colorTransferFunction.AddRGBPoint(255.0 * 12, 1.0, 1.0, 1.0);

        // The property describes how the data will look
        vtkVolumeProperty volumeProperty = new vtkVolumeProperty();
        volumeProperty.SetColor(colorTransferFunction);
        volumeProperty.SetScalarOpacity(opacityTransferFunction);
        volumeProperty.ShadeOn();
        volumeProperty.SetInterpolationTypeToLinear();

        return volumeProperty;
    }

    public void clear() {
        this.GetRenderWindow().GetRenderers().RemoveAllItems();
        vtkRenderer ren = new vtkRenderer();

        this.GetRenderWindow().AddRenderer(ren);

        Render();
    }
}
