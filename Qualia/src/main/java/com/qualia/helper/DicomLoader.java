package com.qualia.helper;

import com.qualia.model.Metadata;
import com.qualia.view.MainView;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkiogdcm.itkGDCMImageIO;
import org.itk.itkioimagebase.itkImageSeriesReaderISS3;
import org.itk.itkvtkglue.itkImageToVTKImageFilterISS3;
import vtk.vtkImageData;

/**
 * Created by taznux on 2014. 6. 27..
 */
public class DicomLoader implements Runnable {
    private MainView mMainView;
    private Metadata mMetaData;

    public DicomLoader(MainView mainView, Metadata targetMetadata) {
        mMainView = mainView;
        mMetaData = targetMetadata;
    }

    @Override
    public void run() {
        // read
        itkImageSS3 itkImages = loadDicomImages(mMetaData.getFullFilenameList());
        ItkImageArchive.getInstance().setItkImage(mMetaData.uId, itkImages);

        itkImageToVTKImageFilterISS3 itkVtkFilter = new itkImageToVTKImageFilterISS3();

        //convert ITK to VTK
        itkVtkFilter.SetInput(itkImages);
        itkVtkFilter.Update();
        VtkImageArchive.getInstance().setVtkImage(mMetaData.uId, itkVtkFilter.GetOutput());

        vtkImageData vtkImage = VtkImageArchive.getInstance().getVtkImage(mMetaData.uId);
        mMainView.updateRightPanel(vtkImage);
    }

    private itkImageSS3 loadDicomImages(String[] fullFilenameList) {
        // read
        itkImageSeriesReaderISS3 reader = new itkImageSeriesReaderISS3();
        itkGDCMImageIO dicomIO = new itkGDCMImageIO();

        reader.SetFileNames(fullFilenameList);
        reader.SetImageIO(dicomIO);
        reader.Update();

        itkImageSS3 lungImage = reader.GetOutput();
        lungImage.SetMetaDataDictionary(dicomIO.GetMetaDataDictionary());

        return lungImage;
    }
}
