package com.qualia.Module;

import ITKTest.NoduleCandidatesDetection;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkcommon.itkImageUC3;

import java.util.HashMap;

public class NoduleDetection implements ModuleInterface {

    LungSegmentation mModuleSegmentation;

    itkImageUC3 mMaskVessel = null;
    itkImageUC3 mMaskCandidates = null;

    itkImageSS3 mOutput = null;

    public NoduleDetection(LungSegmentation module){
        mModuleSegmentation = module;
    }

    public itkImageUC3 getMaskVessel(){
        return mMaskVessel;
    }

    public itkImageUC3 getMaskCandidates(){
        return mMaskCandidates;
    }

    @Override
    public void applyModule() {

        System.out.println("Nodule Candidates Detection");
        /* Nodule Candidates Detection */
        NoduleCandidatesDetection noduleCandidateDetection = new NoduleCandidatesDetection();
        noduleCandidateDetection.setLungImage(mModuleSegmentation.getImageLung());
        noduleCandidateDetection.setLungMask(mModuleSegmentation.getImageMask());
        noduleCandidateDetection.run();

        mMaskCandidates = noduleCandidateDetection.getNoduleCandidatesMask();
        mMaskVessel = noduleCandidateDetection.getVesselMask();

        mOutput = noduleCandidateDetection.getNoduleCandidatesLabel();
    }

    @Override
    public itkImageSS3 getOutput() {
        return mOutput;
    }

    @Override
    public HashMap<String, String> getOptionMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Min Threshold", "-700");
        map.put("Max Threshold", "-100");
        map.put("Step", "8");

        return map;
    }
}
