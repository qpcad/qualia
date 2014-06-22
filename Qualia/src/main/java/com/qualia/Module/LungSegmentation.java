package com.qualia.Module;

import ITKTest.ImageProcessingUtils;
import com.qualia.helper.ItkImageArchive;
import com.qualia.model.Metadata;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkcommon.itkImageUC3;
import org.itk.itkimageintensity.itkMaskImageFilterISS3IUC3ISS3;

import java.util.HashMap;

public class LungSegmentation implements ModuleInterface {
    Metadata mModel = null;
    itkImageSS3 mOutput = null;
    itkImageSS3 mImageLung = null;
    itkImageUC3 mImageMask = null;

    public LungSegmentation(Metadata model){
        mModel = model;
        mOutput = null;
        mImageLung = null;
        mImageMask = null;
    }

    public itkImageSS3 getImageLung(){
        return mImageLung;
    }

    public itkImageUC3 getImageMask(){
        return mImageMask;
    }


    @Override
    public void applyModule() {
        mImageLung = ItkImageArchive.getInstance().getItkImage(mModel.uId);

        System.out.println("Image interpolation");
        ImageProcessingUtils.getInstance().tic();
        itkImageSS3 isoLungImage = ImageProcessingUtils.getInstance().getInstance().imageInterpolation(mImageLung, 1.0);
        ImageProcessingUtils.getInstance().toc();

        mImageLung = isoLungImage;

        System.out.println("Lung Segmentation");
        /* Lung Segmentation */
        ITKTest.LungSegmentation lungSegmentation = new ITKTest.LungSegmentation();
        lungSegmentation.setLungImage(mImageLung);
        lungSegmentation.run();

        mImageMask = lungSegmentation.getLungMask();

        /* Lung Masking */
        itkMaskImageFilterISS3IUC3ISS3 maskFilter = new itkMaskImageFilterISS3IUC3ISS3();

        maskFilter.SetInput1(mImageLung);
        maskFilter.SetInput2(mImageMask);
        maskFilter.SetOutsideValue((short) -2000);
        maskFilter.Update();

        mOutput = maskFilter.GetOutput();
    }

    @Override
    public itkImageSS3 getOutput() {
        return mOutput;
    }

    @Override
    public HashMap<String, String> getOptionMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Segmentation", "-500");

        return map;
    }
}

