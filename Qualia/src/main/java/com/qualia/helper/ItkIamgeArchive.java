package com.qualia.helper;

import org.itk.itkcommon.itkImageSS3;
import vtk.vtkImageData;

import java.util.HashMap;

public class ItkIamgeArchive {
    HashMap<String, itkImageSS3> mMap;

    private static ItkIamgeArchive mInstance = null;

    private ItkIamgeArchive(){
        mMap = new HashMap<String, itkImageSS3>();
    }

    public static VtkImageArchive getInstance(){
        if(mInstance==null){
            mInstance = new ItkIamgeArchive();
        }

        return mInstance;
    }

    public void setVtkImage(String uId, itkImageSS3 imageData){
        mMap.put(uId, imageData);
    }

    public itkImageSS3 getVtkImage(String uId){
        return mMap.get(uId);
    }
}
