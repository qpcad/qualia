package ITKTest;

import org.itk.itkcommon.itkImageSS3;
import org.itk.itkcommon.itkImageUC3;
import org.itk.itkcommon.itkVectorD3;
import org.itk.itkimageintensity.itkAddImageFilterISS3ISS3ISS3;
import org.itk.itkimageintensity.itkMaskImageFilterISS3IUC3ISS3;
import org.itk.itklabelmap.*;

/**
 * Created by taznux on 2014. 6. 5..
 */
public class NoduleClassification implements Runnable {
    private itkImageSS3 lungSegImage_;
    private itkImageUC3 noduleCandidatesMask_;
    private itkLabelMap3 nodules_;
    private itkImageUC3 nodulesMask_;
    private itkImageUC3 vesselMask_;
    private itkImageSS3 nodulesLabel_;

    public void setLungSegImage(itkImageSS3 lungSegImage_) {
        this.lungSegImage_ = lungSegImage_;
    }

    public itkLabelMap3 getNodules() {
        return nodules_;
    }

    public itkImageUC3 getNodulesMask_() {
        return nodulesMask_;
    }

    @Override
    public void run() {
        long new_label = 0;
        itkBinaryImageToStatisticsLabelMapFilterIUC3ISS3LM3 labelMapFilter = new itkBinaryImageToStatisticsLabelMapFilterIUC3ISS3LM3();

        nodules_ = new itkLabelMap3();

        ImageProcessingUtils.tic();

        labelMapFilter.SetInput1(noduleCandidatesMask_);
        labelMapFilter.SetInput2(lungSegImage_);

        labelMapFilter.FullyConnectedOn();
        labelMapFilter.ComputeFeretDiameterOn();
        labelMapFilter.ComputePerimeterOn();

        labelMapFilter.Update();

        ImageProcessingUtils.toc();

        long labels = labelMapFilter.GetOutput().GetNumberOfLabelObjects();
        for (long l = 1; l <= labels; l++) {
            itkStatisticsLabelObjectUL3 labelObject = labelMapFilter.GetOutput().GetLabelObject(l);

            double volume = labelObject.GetPhysicalSize();
            double pixels = labelObject.Size();
            itkVectorD3 principalMoments = labelObject.GetPrincipalMoments();
            double roundness = labelObject.GetRoundness();
            //double elongation = labelObject.GetElongation();
            double elongation = Math.abs(principalMoments.GetElement(2) / principalMoments.GetElement(1));
            double feretDiameter = labelObject.GetFeretDiameter();

            if (feretDiameter < 3 || volume < Math.pow(1.5, 3) * Math.PI * 4 / 3) { // small object
                continue;
            }
            if (feretDiameter > 30 || volume > Math.pow(15, 3) * Math.PI * 4 / 3) { // huge object
                continue;
            }
            if (roundness < 0.8 || roundness > 1.2 || elongation > 4) {
                continue;
            }

            System.out.println("Nodule " + new_label);

            labelObject.SetLabel(new_label++);
            nodules_.AddLabelObject(labelObject);
        }

        ImageProcessingUtils.toc();

        {
            itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToBinaryImageFilter = new itkLabelMapToBinaryImageFilterLM3IUC3();
            itkBinaryImageToLabelMapFilterIUC3LM3 binaryImageToLabelMapFilter = new itkBinaryImageToLabelMapFilterIUC3LM3();

            labelMapToBinaryImageFilter.SetInput(nodules_);
            binaryImageToLabelMapFilter.SetInput(labelMapToBinaryImageFilter.GetOutput());
            binaryImageToLabelMapFilter.Update();
            nodulesMask_ = labelMapToBinaryImageFilter.GetOutput();
            nodules_ = binaryImageToLabelMapFilter.GetOutput();
        }

        itkMaskImageFilterISS3IUC3ISS3 maskImageFilter = new itkMaskImageFilterISS3IUC3ISS3();
        itkMaskImageFilterISS3IUC3ISS3 maskImageFilter1 = new itkMaskImageFilterISS3IUC3ISS3();
        itkAddImageFilterISS3ISS3ISS3 addImageFilter = new itkAddImageFilterISS3ISS3ISS3();
        itkAddImageFilterISS3ISS3ISS3 addImageFilter1 = new itkAddImageFilterISS3ISS3ISS3();

        maskImageFilter.SetConstant1((short) 1500);
        maskImageFilter.SetMaskImage(nodulesMask_);
        maskImageFilter1.SetConstant1((short) 500);
        maskImageFilter1.SetMaskImage(vesselMask_);

        addImageFilter.SetInput1(lungSegImage_);
        addImageFilter.SetInput2(maskImageFilter.GetOutput());
        addImageFilter1.SetInput1(addImageFilter.GetOutput());
        addImageFilter1.SetInput2(maskImageFilter1.GetOutput());

        maskImageFilter.SetOutsideValue((short) -500);
        maskImageFilter1.SetOutsideValue((short) -500);

        addImageFilter1.Update();

        ImageProcessingUtils.toc();

        nodulesLabel_ = addImageFilter1.GetOutput();
    }

    public void setVesselMask_(itkImageUC3 vesselMask_) {
        this.vesselMask_ = vesselMask_;
    }

    public void setNoduleCandidatesMask_(itkImageUC3 noduleCandidatesMask_) {
        this.noduleCandidatesMask_ = noduleCandidatesMask_;
    }

    public itkImageSS3 getNodulesLabel() {
        return nodulesLabel_;
    }
}
