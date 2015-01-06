package com.qualia.Module;

import ITKTest.ImageProcessingUtils;
import com.qualia.view.VtkView;
import org.itk.itkbinarymathematicalmorphology.itkBinaryMorphologicalOpeningImageFilterIUC2IUC2SE2;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkcommon.itkImageUC3;
import org.itk.itkcommon.itkSize2;
import org.itk.itkcommon.itkVectorD3;
import org.itk.itkimagegrid.itkSliceBySliceImageFilterIUC3IUC3;
import org.itk.itkimageintensity.itkAddImageFilterISS3ISS3ISS3;
import org.itk.itkimageintensity.itkMaskImageFilterISS3IUC3ISS3;
import org.itk.itklabelmap.*;
import org.itk.itkmathematicalmorphology.itkFlatStructuringElement2;


public class AnnNoduleClassification extends ModuleBase {
    VtkView mDialog = null;

    LungSegmentation mModuleSegmentation = null;
    NoduleDetection mModuleDetection = null;
    itkImageSS3 mOutput;
    private itkImageSS3 mLungSegImage = null;
    private itkImageUC3 mNoduleCandidatesMask = null;
    private itkLabelMap3 mNodules = null;
    private itkImageUC3 mNodulesMask = null;
    private itkImageUC3 mVesselMask = null;

    public AnnNoduleClassification(VtkView dialog, LungSegmentation moduleSegment, NoduleDetection moduleDetection) {
        name = "ANN Nodule Classification";

        mDialog = dialog;

        mModuleSegmentation = moduleSegment;
        mModuleDetection = moduleDetection;

        initializePanel();
    }

    public synchronized void classification() {
        long new_label = 1;
        itkBinaryImageToStatisticsLabelMapFilterIUC3ISS3LM3 labelMapFilter = new itkBinaryImageToStatisticsLabelMapFilterIUC3ISS3LM3();

        mLungSegImage = mModuleSegmentation.getOutput();
        mNoduleCandidatesMask = mModuleDetection.getMaskCandidates();
        mVesselMask = mModuleDetection.getMaskVessel();

        mNodules = new itkLabelMap3();
        mNodules.CopyInformation(mLungSegImage);

        setProgress(1);

        ImageProcessingUtils.getInstance().tic();

        itkSliceBySliceImageFilterIUC3IUC3 sliceBySliceImageFilter = new itkSliceBySliceImageFilterIUC3IUC3();
        itkBinaryMorphologicalOpeningImageFilterIUC2IUC2SE2 openingImageFilter = new itkBinaryMorphologicalOpeningImageFilterIUC2IUC2SE2();
        itkSize2 radius = new itkSize2();
        radius.SetElement(0, 1);
        radius.SetElement(1, 1);
        itkFlatStructuringElement2 ball = itkFlatStructuringElement2.Box(radius);

        sliceBySliceImageFilter.SetInput(mNoduleCandidatesMask);
        sliceBySliceImageFilter.SetFilter(openingImageFilter);

        labelMapFilter.SetInput1(sliceBySliceImageFilter.GetOutput());
        labelMapFilter.SetInput2(mLungSegImage);

        openingImageFilter.SetKernel(ball);

        labelMapFilter.FullyConnectedOn();
        labelMapFilter.ComputeFeretDiameterOn();
        labelMapFilter.ComputePerimeterOn();

        labelMapFilter.Update();

        ImageProcessingUtils.getInstance().toc();
        setProgress(10);

        long labels = labelMapFilter.GetOutput().GetNumberOfLabelObjects();
        for (long l = 1; l <= labels; l++) {
            itkStatisticsLabelObjectUL3 labelObject = labelMapFilter.GetOutput().GetLabelObject(l);

            double volume = labelObject.GetPhysicalSize();
            double pixels = labelObject.Size();
            itkVectorD3 principalMoments = labelObject.GetWeightedPrincipalMoments();
            double roundness = labelObject.GetRoundness();
            //double elongation = labelObject.GetElongation();
            double elongation = Math.abs(principalMoments.GetElement(2) / principalMoments.GetElement(1));
            double feretDiameter = labelObject.GetEquivalentSphericalRadius() * 2;
            double mean = labelObject.GetMean();

            System.out.println(l + " " + volume + " " + roundness + " " + elongation + " " + feretDiameter + " " + mean + " " + principalMoments.GetElement(0) + " " + principalMoments.GetElement(1) + " " + principalMoments.GetElement(2));

            if (feretDiameter < 3 || volume < Math.pow(1.5, 3) * Math.PI * 4 / 3) { // small object
                continue;
            }
            if (feretDiameter > 30 || volume > Math.pow(15, 3) * Math.PI * 4 / 3) { // huge object
                continue;
            }
            if (roundness < 0.8 || roundness > 1.2 || elongation > 4 || mean > 100) {
                continue;
            }
            // vessel overlap check
            int overlap = 0;
            for (int p = 0; p < pixels; p++) {
                if (mVesselMask.GetPixel(labelObject.GetIndex(p)) > 0)
                    overlap++;
            }
            double ratio = overlap / pixels;
            if (ratio > 0.3) {
                System.out.println("Overlap:" + overlap + "/" + pixels + "=" + ratio);
                continue;
            }


            //System.out.println("Nodule " + new_label);
            //System.out.println(labelObject);

            labelObject.SetLabel(new_label++);
            mNodules.AddLabelObject(labelObject);
        }

        System.out.println("Objects " + labels + " " + mNodules.GetNumberOfLabelObjects());
        mNodules.Update();

        setProgress(20);

        ImageProcessingUtils.getInstance().toc();

        itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToBinaryImageFilter = new itkLabelMapToBinaryImageFilterLM3IUC3();
        itkBinaryImageToLabelMapFilterIUC3LM3 binaryImageToLabelMapFilter = new itkBinaryImageToLabelMapFilterIUC3LM3();

        labelMapToBinaryImageFilter.SetInput(mNodules);
        binaryImageToLabelMapFilter.SetInput(labelMapToBinaryImageFilter.GetOutput());
        binaryImageToLabelMapFilter.Update();
        mNodulesMask = labelMapToBinaryImageFilter.GetOutput();
        mNodules = binaryImageToLabelMapFilter.GetOutput();

        ImageProcessingUtils.getInstance().toc();

        setProgress(50);

        itkMaskImageFilterISS3IUC3ISS3 maskImageFilter = new itkMaskImageFilterISS3IUC3ISS3();
        itkMaskImageFilterISS3IUC3ISS3 maskImageFilter1 = new itkMaskImageFilterISS3IUC3ISS3();
        itkAddImageFilterISS3ISS3ISS3 addImageFilter = new itkAddImageFilterISS3ISS3ISS3();
        itkAddImageFilterISS3ISS3ISS3 addImageFilter1 = new itkAddImageFilterISS3ISS3ISS3();

        maskImageFilter.SetConstant1((short) 1500);
        maskImageFilter.SetMaskImage(mNodulesMask);
        maskImageFilter1.SetConstant1((short) 200);
        maskImageFilter1.SetMaskImage(mVesselMask);

        addImageFilter.SetInput1(mLungSegImage);
        addImageFilter.SetInput2(maskImageFilter.GetOutput());
        addImageFilter1.SetInput1(addImageFilter.GetOutput());
        addImageFilter1.SetInput2(maskImageFilter1.GetOutput());

        maskImageFilter.SetOutsideValue((short) -300);
        maskImageFilter1.SetOutsideValue((short) -300);

        addImageFilter1.Update();

        ImageProcessingUtils.getInstance().toc();

        mOutput = addImageFilter1.GetOutput();

        mDialog.renderItkImage(mOutput);

        setProgress(100);
    }

    @Override
    public void run() {
        if (mModuleDetection.getOutput() == null) {
            mModuleDetection.setVisible(true);
            mModuleDetection.run();
        }

        classification();
    }
}
