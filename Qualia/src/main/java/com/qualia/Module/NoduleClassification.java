package com.qualia.Module;

import org.itk.itkcommon.itkImageSS3;

import java.util.HashMap;

public class NoduleClassification implements ModuleInterface {

    LungSegmentation mModuleSegmentation;
    NoduleDetection mModuleDetection;

    itkImageSS3 mOutput;

    public NoduleClassification(LungSegmentation moduleSegment, NoduleDetection moduleDetection){
        mModuleSegmentation = moduleSegment;
        mModuleDetection = moduleDetection;
    }

    @Override
    public void applyModule() {
        System.out.println("Nodule Classification");

        ITKTest.NoduleClassification noduleClassification = new ITKTest.NoduleClassification();
        noduleClassification.setLungSegImage(mModuleSegmentation.getOutput());
        noduleClassification.setNoduleCandidatesMask_(mModuleDetection.getMaskCandidates());
        noduleClassification.setVesselMask_(mModuleDetection.getMaskVessel());

        noduleClassification.run();

        mOutput = noduleClassification.getNodulesLabel();
    }

    @Override
    public itkImageSS3 getOutput() {
        return mOutput;
    }

    @Override
    public HashMap<String, String> getOptionMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Level", "2000");
        map.put("Balance", "0.7");

        return map;
    }

}
