package ITKTest;

import org.itk.itkanisotropicsmoothing.itkGradientAnisotropicDiffusionImageFilterIF3IF3;
import org.itk.itkcommon.*;
import org.itk.itkconnectedcomponents.itkConnectedComponentImageFilterIUC3ISS3;
import org.itk.itkimagefilterbase.itkCastImageFilterIF3ISS3;
import org.itk.itkimagefilterbase.itkCastImageFilterISS3IF3;
import org.itk.itkimagefunction.itkLinearInterpolateImageFunctionISS3D;
import org.itk.itkimagefusion.itkLabelMapToRGBImageFilterLM3IRGBUC3;
import org.itk.itkimagefusion.itkLabelOverlayImageFilterIUC3ISS3IRGBUC3;
import org.itk.itkimagegrid.itkResampleImageFilterISS3ISS3;
import org.itk.itkimageintensity.itkRescaleIntensityImageFilterISS3IUC3;
import org.itk.itkioimagebase.itkImageFileWriterIRGBUC3;
import org.itk.itkioimagebase.itkImageFileWriterISS3;
import org.itk.itklabelmap.itkLabelMap3;
import org.itk.itklabelmap.itkLabelMapToBinaryImageFilterLM3IUC3;
import org.itk.itkthresholding.itkBinaryThresholdImageFilterISS3IUC3;
import org.itk.itktransform.itkIdentityTransformD3;

/**
 * <pre>
 * kr.qualia
 * ImageProcessingUtils.java
 * FIXME 클래스 설명
 * </pre>
 *
 * @author taznux
 * @date 2014. 4. 17.
 */
final public class ImageProcessingUtils {
    static private long startTime_;

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @method tic
     */
    static public void tic() {
        startTime_ = System.currentTimeMillis();
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @method toc
     */
    static public void toc() {
        System.out.println("Elapsed time " + (System.currentTimeMillis() - startTime_) / 1000.0);
    }

    static public itkImageSS3 imageInterpolation(itkImageSS3 input) {
        double targetSpacing = 1;

        itkResampleImageFilterISS3ISS3 resampleImage = new itkResampleImageFilterISS3ISS3();
        itkVectorD3 inputSpacing = input.GetSpacing();
        itkVectorD3 outputSpacing = new itkVectorD3(targetSpacing);
        itkIdentityTransformD3 identityTransform = new itkIdentityTransformD3();
        itkLinearInterpolateImageFunctionISS3D linearInterpolate = new itkLinearInterpolateImageFunctionISS3D();

        identityTransform.SetIdentity();

        itkSize3 inputSize = input.GetLargestPossibleRegion().GetSize();
        itkSize3 outputSize = new itkSize3();

        for (int i = 0; i < 3; i++)
            outputSize.SetElement(i, (long) (inputSize.GetElement(i) * inputSpacing.GetElement(i) / outputSpacing.GetElement(i) + 0.5));

        resampleImage.SetInput(input);
        resampleImage.SetTransform(identityTransform);
        resampleImage.SetInterpolator(linearInterpolate);
        resampleImage.SetOutputSpacing(outputSpacing);
        resampleImage.SetOutputOrigin(input.GetOrigin());
        resampleImage.SetOutputDirection(input.GetDirection());
        resampleImage.SetSize(outputSize);

        resampleImage.Update();

        return resampleImage.GetOutput();
    }


    static public itkImageSS3 imageEnhancement(itkImageSS3 input) {
        itkCastImageFilterISS3IF3 castImageFilterISS3IF3 = new itkCastImageFilterISS3IF3();
        itkGradientAnisotropicDiffusionImageFilterIF3IF3 gradientAnisotropicDiffusionImageFilter = new itkGradientAnisotropicDiffusionImageFilterIF3IF3();
        itkCastImageFilterIF3ISS3 castImageFilterIF3ISS3 = new itkCastImageFilterIF3ISS3();


        castImageFilterISS3IF3.SetInput(input);
        gradientAnisotropicDiffusionImageFilter.SetInput(castImageFilterISS3IF3.GetOutput());
        castImageFilterIF3ISS3.SetInput(gradientAnisotropicDiffusionImageFilter.GetOutput());

        gradientAnisotropicDiffusionImageFilter.SetNumberOfIterations(5);
        gradientAnisotropicDiffusionImageFilter.SetTimeStep(0.05);
        gradientAnisotropicDiffusionImageFilter.SetConductanceParameter(5.0);

        castImageFilterIF3ISS3.Update();
        return castImageFilterIF3ISS3.GetOutput();
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param image
     * @param threshold
     * @return
     * @method thresholdImage
     */
    static public itkImageUC3 thresholdImage(itkImageSS3 image, short threshold) {
        itkBinaryThresholdImageFilterISS3IUC3 thresholdFilter = new itkBinaryThresholdImageFilterISS3IUC3();

        thresholdFilter.SetInput(image);
        thresholdFilter.SetUpperThreshold(threshold);
        thresholdFilter.Update();

        return thresholdFilter.GetOutput();
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param image
     * @param threshold
     * @return
     * @method thresholdImageL
     */
    static public itkImageUC3 thresholdImageL(itkImageSS3 image, short threshold) {
        itkBinaryThresholdImageFilterISS3IUC3 thresholdFilter = new itkBinaryThresholdImageFilterISS3IUC3();

        thresholdFilter.SetInput(image);
        thresholdFilter.SetLowerThreshold(threshold);
        thresholdFilter.Update();

        return thresholdFilter.GetOutput();
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param labelMap
     * @return
     * @method labelMapToRGB
     */
    static public itkImageRGBUC3 labelMapToRGB(itkLabelMap3 labelMap) {
        itkLabelMapToRGBImageFilterLM3IRGBUC3 labelToRGB = new itkLabelMapToRGBImageFilterLM3IRGBUC3();
        labelToRGB.SetInput(labelMap);
        labelToRGB.Update();

        return labelToRGB.GetOutput();
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param labelMap
     * @param image
     * @param fileName
     * @method writeLabelMapOverlay
     */
    static public void writeLabelMapOverlay(itkLabelMap3 labelMap, itkImageSS3 image, String fileName) {
        itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToBin = new itkLabelMapToBinaryImageFilterLM3IUC3();
        itkConnectedComponentImageFilterIUC3ISS3 connectedComponentFilter = new itkConnectedComponentImageFilterIUC3ISS3();
        itkLabelOverlayImageFilterIUC3ISS3IRGBUC3 labelOverlay = new itkLabelOverlayImageFilterIUC3ISS3IRGBUC3();
        itkRescaleIntensityImageFilterISS3IUC3 shortToUChar = new itkRescaleIntensityImageFilterISS3IUC3();

        labelMapToBin.SetInput(labelMap);
        shortToUChar.SetInput(image);
        connectedComponentFilter.SetInput(labelMapToBin.GetOutput());
        labelOverlay.SetInput(shortToUChar.GetOutput());
        labelOverlay.SetLabelImage(connectedComponentFilter.GetOutput());
        labelOverlay.SetOpacity(0.8);

        labelOverlay.Update();

        writeImage(labelOverlay.GetOutput(), fileName);
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param image
     * @param fileName
     * @method writeImage
     */
    static public void writeImage(itkImageSS3 image, String fileName) {
        itkImageFileWriterISS3 writer = new itkImageFileWriterISS3();
        writer.SetInput(image);
        writer.SetFileName(fileName);
        writer.Update();
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param image
     * @param fileName
     * @method writeImage
     */
    static public void writeImage(itkImageRGBUC3 image, String fileName) {
        itkImageFileWriterIRGBUC3 writer = new itkImageFileWriterIRGBUC3();
        writer.SetInput(image);
        writer.SetFileName(fileName);
        writer.Update();
    }
}