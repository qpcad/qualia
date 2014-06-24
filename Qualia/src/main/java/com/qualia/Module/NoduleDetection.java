package com.qualia.Module;

import ITKTest.ImageProcessingUtils;
import com.qualia.view.QSlider;
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

public class NoduleDetection extends ModuleBase {
    VtkView mDialog;

    LungSegmentation mModuleSegmentation;

    itkImageUC3 mMaskVessel = null;
    itkImageUC3 mMaskCandidates = null;
    itkLabelMap3 mLabelMapCandidates = null;
    itkLabelMap3 mLabelMapVessel = null;

    itkImageSS3 mOutput = null;

    QSlider mSliderMinThreshold = null;
    QSlider mSliderMaxThreshold = null;
    QSlider mSliderStep = null;

    public NoduleDetection(VtkView dialog, LungSegmentation module) {
        name = "Nodule Candidates Detection";

        mDialog = dialog;
        mModuleSegmentation = module;

        initializePanel();

        mSliderMinThreshold = new QSlider(-800, -100, -700, "Min Threshold");
        addToConfigPanel(mSliderMinThreshold);

        mSliderMaxThreshold = new QSlider(-800, -100, -100, "Max Threshold");
        addToConfigPanel(mSliderMaxThreshold);

        mSliderStep = new QSlider(1, 16, 8, "Step");
        addToConfigPanel(mSliderStep);
    }

    public itkImageUC3 getMaskVessel(){
        return mMaskVessel;
    }

    public itkImageUC3 getMaskCandidates(){
        return mMaskCandidates;
    }

    public itkLabelMap3 getLabelMapCandidates() {
        return mLabelMapCandidates;
    }

    public itkLabelMap3 getLabelMapVessel() {
        return mLabelMapVessel;
    }


    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param lungSegImage
     * @return
     * @method multiThresholdDetection
     */
    private void multiThresholdDetection(itkImageSS3 lungSegImage) {
        long new_label = 1;
        long new_vlabel = 1;

        int step = mSliderStep.getValue();
        int maxThreshold = mSliderMaxThreshold.getValue();
        int minThreshold = mSliderMinThreshold.getValue();

        if (maxThreshold <= minThreshold) {
            maxThreshold = minThreshold + 100;
            mSliderMaxThreshold.setValue(maxThreshold);
        }

        mLabelMapCandidates = new itkLabelMap3();
        mLabelMapVessel = new itkLabelMap3();

        mLabelMapVessel.CopyInformation(lungSegImage);
        mLabelMapCandidates.CopyInformation(lungSegImage);

        for (int i = 0; i <= step; i++) {
            setProgress((int) Math.ceil(i * 80.0 / step));
            itkBinaryImageToShapeLabelMapFilterIUC3LM3 labelMapFilter = new itkBinaryImageToShapeLabelMapFilterIUC3LM3();

            short threshold = (short) (minThreshold + (maxThreshold - minThreshold) * (step - i) / step);
            int seRadius = (int) Math.abs(threshold / 100 / 3.0);
            seRadius = seRadius == 0 ? 1 : seRadius;

            for (int r = seRadius; r >= 0; r--) {
                if (threshold < -600 && r < 1) continue; // too many noises

                itkImageUC3 noduleThresholdImage = ImageProcessingUtils.getInstance().thresholdImageL(lungSegImage, threshold);

                System.out.println("T: " + threshold + " R: " + r);

                if (r > 0) {
                    itkSliceBySliceImageFilterIUC3IUC3 sliceBySliceImageFilter = new itkSliceBySliceImageFilterIUC3IUC3();
                    itkBinaryMorphologicalOpeningImageFilterIUC2IUC2SE2 openingImageFilter = new itkBinaryMorphologicalOpeningImageFilterIUC2IUC2SE2();
                    itkSize2 radius = new itkSize2();
                    radius.SetElement(0, r);
                    radius.SetElement(1, r);
                    itkFlatStructuringElement2 ball = itkFlatStructuringElement2.Box(radius);

                    sliceBySliceImageFilter.SetInput(noduleThresholdImage);
                    sliceBySliceImageFilter.SetFilter(openingImageFilter);
                    labelMapFilter.SetInput(sliceBySliceImageFilter.GetOutput());

                    openingImageFilter.SetKernel(ball);
                } else {
                    labelMapFilter.SetInput(noduleThresholdImage);
                }

                labelMapFilter.FullyConnectedOn();
                labelMapFilter.ComputeFeretDiameterOn();
                labelMapFilter.ComputePerimeterOn();


                labelMapFilter.Update();

                ImageProcessingUtils.getInstance().toc();

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

                    // TODO this filter is not accurate
                    //System.out.println(feretDiameter +" "+ roundness);
                    //System.out.println(labelObject);
                    if (feretDiameter < 3 || volume < Math.pow(1.5, 3) * Math.PI * 4 / 3) { // small object
                        continue;
                    }
                    if (feretDiameter > 30 || volume > Math.pow(15, 3) * Math.PI * 4 / 3) { // huge object
                        if (roundness < 0.8 || roundness > 1.2 || elongation > 4) { // vessel
                            System.out.println("R: " + roundness + ", E: " + elongation);
                            labelObject.SetLabel(new_vlabel++);
                            mLabelMapVessel.AddLabelObject(labelObject);
                        }
                        continue;
                    }
                    if (elongation > 4) { // vessel - elongated object
                        labelObject.SetLabel(new_vlabel++);
                        mLabelMapVessel.AddLabelObject(labelObject);
                        System.out.println("E: " + elongation);

                        continue;
                    }
                    if (roundness < 0.8 || roundness > 1.2) {
                        continue;
                    }
                    // vessel overlap check
                    if (new_vlabel > 0) {
                        int overlap = 0;
                        for (int p = 0; p < pixels; p++) {
                            if (mLabelMapVessel.GetPixel(labelObject.GetIndex(p)) > 0)
                                overlap++;
                        }
                        double ratio = overlap / pixels;
                        if (ratio > 0.3) {
                            System.out.println("Overlap:" + overlap + "/" + pixels + "=" + ratio);
                            labelObject.SetLabel(new_vlabel++);
                            mLabelMapVessel.AddLabelObject(labelObject);
                            continue;
                        }
                    }

                    labelObject.SetLabel(new_label++);
                    mLabelMapCandidates.AddLabelObject(labelObject);
                }

                System.out.println("Objects " + labels + " " + mLabelMapCandidates.GetNumberOfLabelObjects());
                System.out.println(new_label + ", " + new_vlabel);
            }
            ImageProcessingUtils.getInstance().toc();
        }

        {
            itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToBinaryImageFilter = new itkLabelMapToBinaryImageFilterLM3IUC3();
            itkBinaryImageToLabelMapFilterIUC3LM3 binaryImageToLabelMapFilter = new itkBinaryImageToLabelMapFilterIUC3LM3();

            labelMapToBinaryImageFilter.SetInput(mLabelMapCandidates);
            binaryImageToLabelMapFilter.SetInput(labelMapToBinaryImageFilter.GetOutput());
            binaryImageToLabelMapFilter.Update();
            mMaskCandidates = labelMapToBinaryImageFilter.GetOutput();
            mLabelMapCandidates = binaryImageToLabelMapFilter.GetOutput();
        }

        {
            itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToBinaryImageFilter = new itkLabelMapToBinaryImageFilterLM3IUC3();
            itkBinaryImageToLabelMapFilterIUC3LM3 binaryImageToLabelMapFilter = new itkBinaryImageToLabelMapFilterIUC3LM3();

            labelMapToBinaryImageFilter.SetInput(mLabelMapVessel);
            binaryImageToLabelMapFilter.SetInput(labelMapToBinaryImageFilter.GetOutput());
            binaryImageToLabelMapFilter.Update();
            mMaskVessel = labelMapToBinaryImageFilter.GetOutput();
            mLabelMapVessel = binaryImageToLabelMapFilter.GetOutput();
        }
        ImageProcessingUtils.getInstance().toc();


        //ImageProcessingUtils.getInstance().writeLabelMapOverlay(vesselMap, lungSegImage, "/Users/taznux/desktop/vessel.mha");
    }

    public void detection() {
        ImageProcessingUtils.getInstance().tic();

        setProgress(1);

        itkImageSS3 lungSegImage = mModuleSegmentation.getOutput();

        multiThresholdDetection(lungSegImage);

        System.out.println("Vessel Objects " + mLabelMapVessel.GetNumberOfLabelObjects());
        System.out.println("Objects " + mLabelMapCandidates.GetNumberOfLabelObjects());

        setProgress(80);

        itkMaskImageFilterISS3IUC3ISS3 maskImageFilter = new itkMaskImageFilterISS3IUC3ISS3();
        itkMaskImageFilterISS3IUC3ISS3 maskImageFilter1 = new itkMaskImageFilterISS3IUC3ISS3();
        itkAddImageFilterISS3ISS3ISS3 addImageFilter = new itkAddImageFilterISS3ISS3ISS3();
        itkAddImageFilterISS3ISS3ISS3 addImageFilter1 = new itkAddImageFilterISS3ISS3ISS3();

        maskImageFilter.SetConstant1((short) 1500);
        maskImageFilter.SetMaskImage(mMaskCandidates);
        maskImageFilter1.SetConstant1((short) 200);
        maskImageFilter1.SetMaskImage(mMaskVessel);

        addImageFilter.SetInput1(lungSegImage);
        addImageFilter.SetInput2(maskImageFilter.GetOutput());
        addImageFilter1.SetInput1(addImageFilter.GetOutput());
        addImageFilter1.SetInput2(maskImageFilter1.GetOutput());

        maskImageFilter.SetOutsideValue((short) -300);
        maskImageFilter1.SetOutsideValue((short) -300);

        addImageFilter1.Update();

        setProgress(90);

        mOutput = addImageFilter1.GetOutput();

        mDialog.renderItkImage(mOutput);

        setProgress(100);
    }

    @Override
    public void run() {
        if (mModuleSegmentation.getOutput() == null) {
            mModuleSegmentation.setVisible(true);
            mModuleSegmentation.run();
        }
        detection();
    }
}
