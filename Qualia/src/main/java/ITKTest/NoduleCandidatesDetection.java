package ITKTest;

import org.itk.itkbinarymathematicalmorphology.itkBinaryMorphologicalOpeningImageFilterIUC2IUC2SE2;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkcommon.itkImageUC3;
import org.itk.itkcommon.itkSize2;
import org.itk.itkcommon.itkVectorD3;
import org.itk.itkimagegrid.itkSliceBySliceImageFilterIUC3IUC3;
import org.itk.itkimageintensity.itkMaskImageFilterISS3IUC3ISS3;
import org.itk.itklabelmap.*;
import org.itk.itkmathematicalmorphology.itkFlatStructuringElement2;

/**
 * <pre>
 * kr.qualia
 * NoduleCandidatesDetection.java
 * FIXME 클래스 설명
 * </pre>
 *
 * @author taznux
 * @date 2014. 4. 17.
 */
public class NoduleCandidatesDetection implements Runnable {
    private itkImageSS3 lungImage_;
    private itkImageUC3 lungMask_;
    private itkImageUC3 noduleCandidatesMask_;
    private itkLabelMap3 noduleCandidates_;
    private itkImageUC3 vesselMask_;
    private itkLabelMap3 vesselMap_;

    public NoduleCandidatesDetection() {
        lungImage_ = null;
        lungMask_ = null;
        noduleCandidatesMask_ = null;
        noduleCandidates_ = null;
        vesselMask_ = null;
        vesselMap_ = null;
    }

    public void setLungImage(itkImageSS3 lungImage) {
        lungImage_ = lungImage;
    }

    public void setLungMask(itkImageUC3 lungMask) {
        lungMask_ = lungMask;
    }

    public itkImageUC3 getNoduleCandidatesMask() {
        return noduleCandidatesMask_;
    }

    public itkLabelMap3 getNoduleCandidates() {
        return noduleCandidates_;
    }

    public itkImageUC3 getVesselMask() {
        return vesselMask_;
    }

    public itkLabelMap3 getVesselMap() {
        return vesselMap_;
    }


    public void run() {
        ImageProcessingUtils.tic();

		/* Lung Masking */
        itkMaskImageFilterISS3IUC3ISS3 maskFilter = new itkMaskImageFilterISS3IUC3ISS3();
        maskFilter.SetInput1(lungImage_);
        maskFilter.SetInput2(lungMask_);
        maskFilter.SetOutsideValue((short) -2000);
        maskFilter.Update();

        itkImageSS3 lungSegImage;
        lungSegImage = maskFilter.GetOutput();

        multiThresholdDetection(lungSegImage);

        System.out.println("Vessel Objects " + vesselMap_.GetNumberOfLabelObjects());
        System.out.println("Objects " + noduleCandidates_.GetNumberOfLabelObjects());
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
        noduleCandidates_ = new itkLabelMap3();
        vesselMap_ = new itkLabelMap3();

        vesselMap_.CopyInformation(lungSegImage);
        noduleCandidates_.CopyInformation(lungSegImage);

        short thresholdList[] = {-800, -700, -600, -500, -400, -300, -200, -600, -500, -400, -300, -200, -600, -300};
        int openRadiusList[] = {1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3};
        long new_label = 0;
        long new_vlabel = 0;


        for (int i = thresholdList.length - 1; i >= 0; i--) {
            itkSliceBySliceImageFilterIUC3IUC3 slicebysliceFitler = new itkSliceBySliceImageFilterIUC3IUC3();
            itkBinaryMorphologicalOpeningImageFilterIUC2IUC2SE2 openingFilter = new itkBinaryMorphologicalOpeningImageFilterIUC2IUC2SE2();
            itkBinaryImageToShapeLabelMapFilterIUC3LM3 labelMapFilter = new itkBinaryImageToShapeLabelMapFilterIUC3LM3();

            itkImageUC3 noduleThresholdImage = ImageProcessingUtils.thresholdImageL(lungSegImage, thresholdList[i]);

            System.out.println("(" + thresholdList[i] + ", " + openRadiusList[i] + ")");

            slicebysliceFitler.SetInput(noduleThresholdImage);
            slicebysliceFitler.SetFilter(openingFilter);
            labelMapFilter.SetInput(slicebysliceFitler.GetOutput());

            itkSize2 radius = new itkSize2();
            radius.SetElement(0, openRadiusList[i]);
            radius.SetElement(1, openRadiusList[i]);
            itkFlatStructuringElement2 ball = itkFlatStructuringElement2.Ball(radius);

            openingFilter.SetKernel(ball);

            labelMapFilter.Update();
            long labels = labelMapFilter.GetOutput().GetNumberOfLabelObjects();
            for (long l = 1; l <= labels; l++) {
                itkStatisticsLabelObjectUL3 labelObject = labelMapFilter.GetOutput().GetLabelObject(l);

                double volume = labelObject.GetPhysicalSize();
                double pixels = labelObject.Size();
                itkVectorD3 pmoments = labelObject.GetPrincipalMoments();
                double e = labelObject.GetElongation();
                double r = labelObject.GetRoundness();
                double ee = Math.abs(pmoments.GetElement(2) / pmoments.GetElement(1));

                // TODO this filter is not accurate
                if (labelObject.GetEquivalentSphericalRadius() < 1.5 || volume < ((3 / 2) ^ 3) * Math.PI * 4 / 3) { // small objects
                    continue;
                }
                if (labelObject.GetEquivalentSphericalRadius() > 15 || volume > ((30 / 2) ^ 3) * Math.PI * 4 / 3) { // huge object
                    if (thresholdList[i] > -100 || ee > 4) { // vessel
                        labelObject.SetLabel(++new_vlabel);
                        vesselMap_.AddLabelObject(labelObject);
                    }
                    continue;
                }
                if (ee > 4) { // vessel - elongated object
                    labelObject.SetLabel(++new_vlabel);
                    vesselMap_.AddLabelObject(labelObject);

                    continue;
                }
                // vessel overlap check
                int overlap = 0;
                for (int p = 0; p < pixels; p++) {
                    if (vesselMap_.GetPixel(labelObject.GetIndex(p)) > 0)
                        overlap++;
                }
                double ratio = overlap / pixels;
                if (ratio > 0.3) {
                    System.out.println("Overlap:" + overlap + "/" + pixels + "=" + ratio);
                    labelObject.SetLabel(++new_vlabel);
                    vesselMap_.AddLabelObject(labelObject);
                    continue;
                }
                //-----------------------
                if (labelObject.GetRoundness() > 1.1) {
                    continue;
                }

                labelObject.SetLabel(++new_label);
                noduleCandidates_.AddLabelObject(labelObject);

                //System.out.println(labelObject);
                //System.out.println(e + " " + ee + " " + r);
            }
            System.out.println("Objects " + labels + " " + noduleCandidates_.GetNumberOfLabelObjects());
            System.out.println(new_label + ", " + new_vlabel);

            ImageProcessingUtils.toc();
        }

        {
            itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToBinaryImageFilter = new itkLabelMapToBinaryImageFilterLM3IUC3();
            itkBinaryImageToLabelMapFilterIUC3LM3 binaryImageToLabelMapFilter = new itkBinaryImageToLabelMapFilterIUC3LM3();

            labelMapToBinaryImageFilter.SetInput(noduleCandidates_);
            binaryImageToLabelMapFilter.SetInput(labelMapToBinaryImageFilter.GetOutput());
            binaryImageToLabelMapFilter.Update();
            noduleCandidatesMask_ = labelMapToBinaryImageFilter.GetOutput();
            noduleCandidates_ = binaryImageToLabelMapFilter.GetOutput();
        }

        {
            itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToBinaryImageFilter = new itkLabelMapToBinaryImageFilterLM3IUC3();
            itkBinaryImageToLabelMapFilterIUC3LM3 binaryImageToLabelMapFilter = new itkBinaryImageToLabelMapFilterIUC3LM3();

            labelMapToBinaryImageFilter.SetInput(vesselMap_);
            binaryImageToLabelMapFilter.SetInput(labelMapToBinaryImageFilter.GetOutput());
            binaryImageToLabelMapFilter.Update();
            vesselMask_ = labelMapToBinaryImageFilter.GetOutput();
            vesselMap_ = binaryImageToLabelMapFilter.GetOutput();
        }


        //ImageProcessingUtils.writeLabelMapOverlay(vesselMap, lungSegImage, "/Users/taznux/desktop/vessel.mha");
    }

}
