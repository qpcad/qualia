package com.qualia.helper;

import vtk.vtkImageData;

import java.util.HashMap;

public class VtkImageArchive {
    private static VtkImageArchive mInstance = null;
    HashMap<String, vtkImageData> mMap;

    private VtkImageArchive() {
        mMap = new HashMap<String, vtkImageData>();
    }

    public static VtkImageArchive getInstance() {
        if (mInstance == null) {
            mInstance = new VtkImageArchive();
        }

        return mInstance;
    }

    public void setVtkImage(String uId, vtkImageData imageData) {
        mMap.put(uId, imageData);
    }

    public vtkImageData getVtkImage(String uId) {
        return mMap.get(uId);
    }
}
