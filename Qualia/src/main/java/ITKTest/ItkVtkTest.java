package ITKTest;

import com.qualia.helper.DicomParser;
import com.qualia.model.Metadata;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkvtkglue.itkImageToVTKImageFilterISS3;
import vtk.*;

import javax.swing.*;
import java.awt.*;


public class ItkVtkTest extends JFrame {
    public class SliceViewer extends JPanel {
        private vtkRenderWindowPanel renWin_;
        private vtkImageViewer2 viewer_;

        public SliceViewer() {
            setLayout(new BorderLayout());

            renWin_ = new vtkRenderWindowPanel();
            viewer_ = new vtkImageViewer2();

            renWin_.setInteractorStyle(new vtkInteractorStyleImage());

            add(renWin_, BorderLayout.CENTER);
        }

        public void setSliceOrientationToXY() {
            viewer_.SetSliceOrientationToXY();
        }

        public void setSliceOrientationToXZ() {
            viewer_.SetSliceOrientationToXZ();
        }

        public void setSliceOrientationToYZ() {
            viewer_.SetSliceOrientationToYZ();
        }

        public void setSlice(int idx) {
            viewer_.SetSlice(idx);
        }


        public void setInput(vtkImageData image) {
            viewer_.SetInputData(image);

            viewer_.GetRenderer().ResetCamera();
            viewer_.SetRenderWindow(renWin_.GetRenderWindow());
            viewer_.SetupInteractor(renWin_.GetRenderWindow().GetInteractor());

            viewer_.SetColorLevel(-500);
            viewer_.SetColorWindow(3000);

            int sliceMin, sliceMax, sliceMiddle;

            sliceMin = viewer_.GetSliceMin();
            sliceMax = viewer_.GetSliceMax();
            sliceMiddle = (sliceMax-sliceMin)/2;

            viewer_.SetSlice(sliceMiddle);
        }
    }

    public class VolumeViewer extends JPanel {
        private vtkRenderWindowPanel renWin_;

        public VolumeViewer() {
            setLayout(new BorderLayout());

            renWin_ = new vtkRenderWindowPanel();
            renWin_.setInteractorStyle(new vtkInteractorStyleTrackballCamera());

            add(renWin_, BorderLayout.CENTER);
        }


        public void setInput(vtkImageData image) {
            // Create transfer mapping scalar value to opacity
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

            renWin_.GetRenderWindow().GetRenderers().RemoveAllItems();
            renWin_.GetRenderWindow().AddRenderer(ren);


            if (ren.GetViewProps().GetNumberOfItems() > 0)
                ren.RemoveVolume(ren.GetViewProps().GetLastProp());
            ren.AddVolume(volume);
            ren.ResetCamera(volume.GetBounds());


            renWin_.Render();
        }
    }


    private VolumeViewer volumeViewer_;
    private SliceViewer sliceViewer1_;
    private SliceViewer sliceViewer2_;
    private SliceViewer sliceViewer3_;


    public ItkVtkTest() {
        GridLayout testLayout = new GridLayout(2, 2);
        setLayout(testLayout);

        volumeViewer_ = new VolumeViewer();
        volumeViewer_.setPreferredSize(new Dimension(600, 600));

        // viewer initialize
        sliceViewer1_ = new SliceViewer();
        sliceViewer1_.setPreferredSize(new Dimension(600, 600));

        sliceViewer2_ = new SliceViewer();
        sliceViewer2_.setPreferredSize(new Dimension(600, 600));

        sliceViewer3_ = new SliceViewer();
        sliceViewer3_.setPreferredSize(new Dimension(600, 600));


        add(volumeViewer_);
        add(sliceViewer1_);
        add(sliceViewer2_);
        add(sliceViewer3_);
    }


    public void sliceViewer(vtkImageData image) {
        // set data
        sliceViewer1_.setInput(image);
        sliceViewer1_.setSliceOrientationToXY();
        sliceViewer1_.setSlice(50);

        sliceViewer2_.setInput(image);
        sliceViewer2_.setSliceOrientationToXZ();
        sliceViewer2_.setSlice(200);

        sliceViewer3_.setInput(image);
        sliceViewer3_.setSliceOrientationToYZ();
        sliceViewer3_.setSlice(100);
    }

    public void renderVolume(vtkImageData image) {
        volumeViewer_.setInput(image);
    }


    public vtkImageData loadDicomData() {
        itkImageSS3 inputImage;
        vtkImageData outputImage;

        itkImageToVTKImageFilterISS3 itkVtkFilter;

        itkVtkFilter = new itkImageToVTKImageFilterISS3();

        // dicom load
        String currentDir = System.getProperty("user.dir");
        String path = currentDir + "/src/test/resources/13614193285030022";

        DicomParser parser = new DicomParser(path);

        String[] uIds = parser.getUidList();
        Metadata data = parser.getMetadataByUid(uIds[0]);

        System.out.println(data.toString());

        outputImage = parser.getVtkImageByUid(uIds[0]);

        return outputImage;
    }

    public void init() {

        this.setPreferredSize(new Dimension(1024, 768));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    public static void main(String argv[]) {

        ItkVtkTest itkVtkTest = new ItkVtkTest();
        itkVtkTest.init();

        vtkImageData image = itkVtkTest.loadDicomData();

        itkVtkTest.sliceViewer(image);
        itkVtkTest.renderVolume(image);
    }
}
