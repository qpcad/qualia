package ITKTest;

import org.itk.itkcommon.*;
import org.itk.itkimagecompose.itkJoinSeriesImageFilterIUC2IUC3;
import org.itk.itkimagegrid.itkSliceBySliceImageFilterIUC3IUC3;
import org.itk.itklabelmap.*;
import org.itk.itkmathematicalmorphology.itkClosingByReconstructionImageFilterIUC2IUC2SE2;
import org.itk.itkmathematicalmorphology.itkFlatStructuringElement2;

/**
 * <pre>
 * kr.qualia
 * LungSegmentation.java
 * FIXME 클래스 설명
 * </pre>
 *
 * @author taznux
 * @date 2014. 4. 17.
 */
public class LungSegmentation implements Runnable {
    private itkImageSS3 lungImage_;
    private itkImageUC3 lungMask_;
    private itkImageRGBUC3 labelImage_;

    public LungSegmentation() {
        lungImage_ = null;
        lungMask_ = null;
        labelImage_ = null;
    }

    public void setLungImage(itkImageSS3 lungImage) {
        lungImage_ = lungImage;
    }

    public itkImageUC3 getLungMask() {
        return lungMask_;
    }

    public itkImageRGBUC3 getLabelImage() {
        return labelImage_;
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param inputImage
     * @return
     * @method removeRim
     */
    private itkImageUC3 removeRim(itkImageUC3 inputImage) {
        itkExtractImageFilterIUC3IUC2 sliceImageFilter = new itkExtractImageFilterIUC3IUC2();
        itkJoinSeriesImageFilterIUC2IUC3 joinSliceFilter = new itkJoinSeriesImageFilterIUC2IUC3();

        itkImageUC3 thresholdImage = inputImage;

        sliceImageFilter.SetInput(thresholdImage);
        sliceImageFilter.SetDirectionCollapseToSubmatrix();

        joinSliceFilter.SetOrigin(lungImage_.GetOrigin().GetElement(2));
        joinSliceFilter.SetSpacing(lungImage_.GetSpacing().GetElement(2));

        itkImageRegion3 inputRegion = thresholdImage.GetLargestPossibleRegion();
        for (int i = 0; i < inputRegion.GetSize(2); i++) {
            itkImageRegion3 desireRegion = new itkImageRegion3();
            itkSize3 size = new itkSize3();
            itkIndex3 index = new itkIndex3();

            size.SetElement(0, inputRegion.GetSize(0));
            size.SetElement(1, inputRegion.GetSize(1));
            size.SetElement(2, 0);
            index.Fill(0);
            index.SetElement(2, i);

            desireRegion.SetIndex(index);
            desireRegion.SetSize(size);

            sliceImageFilter.SetExtractionRegion(desireRegion);

            //----------------------

            itkBinaryImageToShapeLabelMapFilterIUC2LM2 labelMapFilter = new itkBinaryImageToShapeLabelMapFilterIUC2LM2();

            labelMapFilter.SetInput(sliceImageFilter.GetOutput());
            labelMapFilter.Update();

            long labels = labelMapFilter.GetOutput().GetNumberOfLabelObjects();
            for (long l = 1; l <= labels; l++) {
                itkShapeLabelObjectUL2 labelObject = labelMapFilter.GetOutput().GetLabelObject(l);
                itkImageRegion2 bbox = labelObject.GetBoundingBox();

                // ignore outside label
                for (short j = 0; j < 2; j++) {
                    //System.out.printf("%3d %3d \n", bbox.GetIndex(j), bbox.GetSize(j) + bbox.GetIndex(j));
                    if (labelObject.GetNumberOfPixels() < 50
                            || (bbox.GetIndex(j) == 0)
                            || (bbox.GetSize(j) + bbox.GetIndex(j) == 512)) {
                        labelMapFilter.GetOutput().RemoveLabel(l);
                        //System.out.println("Boundary " + labelMapFilter.GetOutput().GetNumberOfLabelObjects());
                        break;
                    }
                }
            }

            itkLabelMapToBinaryImageFilterLM2IUC2 labelMapToMask = new itkLabelMapToBinaryImageFilterLM2IUC2();
            labelMapToMask.SetInput(labelMapFilter.GetOutput());
            labelMapToMask.Update();

            //----------------------

            joinSliceFilter.PushBackInput(labelMapToMask.GetOutput());

            labelMapToMask.GetOutput().DisconnectPipeline();
        }
        joinSliceFilter.Update();

        return joinSliceFilter.GetOutput();
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param labelMap
     * @return
     * @method extractLung
     */
    private itkLabelMap3 extractLung(itkLabelMap3 labelMap) {
        long maxVolume = 0;
        long maxVolume1 = 0;
        long maxIndex = 0;
        long maxIndex1 = 0;

        itkLabelMap3 lungMap = new itkLabelMap3();
        lungMap.CopyInformation(labelMap);

        for (long i = 1; i <= labelMap.GetNumberOfLabelObjects(); i++) {
            itkShapeLabelObjectUL3 labelObject = labelMap.GetLabelObject(i);

            long volume = labelObject.GetNumberOfPixels();
            if (volume > maxVolume1) { // second largest
                maxVolume1 = volume;
                maxIndex1 = i;
            }
            if (volume > maxVolume) { // largest
                maxVolume1 = maxVolume;
                maxIndex1 = maxIndex;
                maxVolume = volume;
                maxIndex = i;
            }
        }

        // extract lung
        lungMap.AddLabelObject(labelMap.GetLabelObject(maxIndex));
        if (maxVolume < maxVolume1 * 2)
            lungMap.AddLabelObject(labelMap.GetLabelObject(maxIndex1));

        System.out.printf("%d, %d, %d, %d\n", maxIndex, maxVolume, maxIndex1,
                maxVolume1);

        return lungMap;
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param labelMap
     * @return
     * @method refineLungMask
     */
    private itkImageUC3 refineLungMask(itkLabelMap3 labelMap) {
        itkLabelMapToBinaryImageFilterLM3IUC3 labelToBinary = new itkLabelMapToBinaryImageFilterLM3IUC3();
        itkBinaryFillholeImageFilterIUC3 holeFillFilter = new itkBinaryFillholeImageFilterIUC3();
        itkSliceBySliceImageFilterIUC3IUC3 slicebysliceFitler = new itkSliceBySliceImageFilterIUC3IUC3();

        itkClosingByReconstructionImageFilterIUC2IUC2SE2 closingFilter = new itkClosingByReconstructionImageFilterIUC2IUC2SE2();

        labelToBinary.SetInput(labelMap);
        slicebysliceFitler.SetInput(labelToBinary.GetOutput());
        holeFillFilter.SetInput(slicebysliceFitler.GetOutput());

        itkSize2 radius = new itkSize2();
        radius.SetElement(0, 5);
        radius.SetElement(1, 5);
        itkFlatStructuringElement2 ball = itkFlatStructuringElement2.Ball(radius);

        closingFilter.SetKernel(ball);

        slicebysliceFitler.SetFilter(closingFilter);
        holeFillFilter.SetForegroundValue((short) 255);

        holeFillFilter.Update();

        return holeFillFilter.GetOutput();
    }

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @return
     * @method getLungLabelMap
     */
    private itkLabelMap3 getLungLabelMap() {
        itkImageUC3 lungThresholdImage = ImageProcessingUtils.thresholdImage(lungImage_, (short) -500);
        itkImageUC3 initialLungMask = removeRim(lungThresholdImage);

        itkBinaryImageToShapeLabelMapFilterIUC3LM3 labelMapFilter = new itkBinaryImageToShapeLabelMapFilterIUC3LM3();
        labelMapFilter.SetInput(initialLungMask);
        labelMapFilter.SetFullyConnected(true);
        labelMapFilter.Update();

        initialLungMask.DisconnectPipeline();

        itkLabelMap3 labelMap = labelMapFilter.GetOutput();
        return labelMap;
    }

    public void run() {
        ImageProcessingUtils.tic();

        itkLabelMap3 lungLabelMap = getLungLabelMap();
        labelImage_ = ImageProcessingUtils.labelMapToRGB(lungLabelMap);

        System.out.println(lungLabelMap.GetNumberOfLabelObjects());

        lungLabelMap = extractLung(lungLabelMap);

        System.out.println(lungLabelMap.GetNumberOfLabelObjects());

        lungMask_ = refineLungMask(lungLabelMap);

        ImageProcessingUtils.toc();
    }
}