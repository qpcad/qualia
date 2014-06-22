package com.qualia.Module;

import org.itk.itkcommon.itkImageSS3;

import java.util.HashMap;

public interface ModuleInterface {
    public void applyModule();
    public itkImageSS3 getOutput();
    public HashMap<String,String> getOptionMap();
}
