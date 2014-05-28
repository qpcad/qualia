package com.qualia.helper;

import org.itk.itkcommon.itkImageSS3;

import java.util.HashMap;

public class ItkImageArchive {
    private static ItkImageArchive mInstance = null;
    HashMap<String, itkImageSS3> mMap;

    private ItkImageArchive() {
        mMap = new HashMap<String, itkImageSS3>();
    }

    public static ItkImageArchive getInstance() {
        if(mInstance==null){
            mInstance = new ItkImageArchive();
        }

        return mInstance;
    }

    public void setItkImage(String uId, itkImageSS3 imageData) {
        mMap.put(uId, imageData);
    }

    public itkImageSS3 getItkImage(String uId) {
        return mMap.get(uId);
    }
}
