package ITKTest;

import org.itk.itkbinarymathematicalmorphology.itkBinaryMorphologicalOpeningImageFilterIUC2IUC2SE2;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkcommon.itkImageUC3;
import org.itk.itkcommon.itkSize2;
import org.itk.itkcommon.itkVectorD3;
import org.itk.itkimagegrid.itkSliceBySliceImageFilterIUC3IUC3;
import org.itk.itkimageintensity.itkMaskImageFilterISS3IUC3ISS3;
import org.itk.itkimageintensity.itkOrImageFilterIUC3IUC3IUC3;
import org.itk.itklabelmap.itkBinaryImageToShapeLabelMapFilterIUC3LM3;
import org.itk.itklabelmap.itkBinaryImageToStatisticsLabelMapFilterIUC3ISS3LM3;
import org.itk.itklabelmap.itkLabelMap3;
import org.itk.itklabelmap.itkLabelMapToBinaryImageFilterLM3IUC3;
import org.itk.itklabelmap.itkStatisticsLabelObjectUL3;
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
 * @version 
 */
public class NoduleCandidatesDetection implements Runnable{
	private itkImageSS3 lungImage_;
	private itkImageUC3 lungMask_;
	private itkImageUC3 noduleCandidatesMask_;
	private itkLabelMap3 noduleCandidates_;
	
	public NoduleCandidatesDetection() {
		lungImage_ = null;
		lungMask_ = null;
		noduleCandidatesMask_ = null;
		noduleCandidates_ = null;	
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
		
		noduleCandidatesMask_ = multiThresholdDetection(lungSegImage);
	
		itkBinaryImageToStatisticsLabelMapFilterIUC3ISS3LM3 labelMapFilter = new itkBinaryImageToStatisticsLabelMapFilterIUC3ISS3LM3();
		labelMapFilter.SetInput1(noduleCandidatesMask_);
		labelMapFilter.SetInput2(lungImage_);
		labelMapFilter.Update();
		
		noduleCandidates_ = labelMapFilter.GetOutput();
		System.out.println("Objects " + noduleCandidates_.GetNumberOfLabelObjects());
	}

	/**
	 * <pre>
	 * 1.개요 : FIXME
	 * 2.처리내용 : FIXME
	 * </pre>
	 *
	 * @method multiThresholdDetection
	 * @param lungSegImage
	 * @return
	 */
	private itkImageUC3 multiThresholdDetection(itkImageSS3 lungSegImage) {
		itkOrImageFilterIUC3IUC3IUC3 orFilter = new itkOrImageFilterIUC3IUC3IUC3();
		itkLabelMap3 vesselMap = new itkLabelMap3();
		
		vesselMap.CopyInformation(lungSegImage);
		
		short thresholdList[] = {-800, -700, -600, -500, -400, -300, -200, -600, -500, -400, -300, -200, -600, -300};
		int openRadiusList[] = {1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3};
		
		for(int i = thresholdList.length-1; i >= 0 ; i--) {
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
			for (long l = 1; l <= labels; l++)
			{
				itkStatisticsLabelObjectUL3 labelObject = labelMapFilter.GetOutput().GetLabelObject(l);
				
				// TODO this filter is not accurate
				if(labelObject.GetEquivalentSphericalRadius() < 1.5) { // small objects
					labelMapFilter.GetOutput().RemoveLabelObject(labelObject);
					continue;
				}
				if(labelObject.GetPhysicalSize() > ((20/2)^3)*Math.PI*4/3 || (labelObject.GetPhysicalSize() > ((2/2)^3)*Math.PI*4/3 && labelObject.GetElongation() > 4)) { // vessel
					if(thresholdList[i] > -600) {
						labelObject.SetLabel(i*1000+l);
						vesselMap.AddLabelObject(labelObject);	
					}
					labelMapFilter.GetOutput().RemoveLabelObject(labelObject);
					continue;
				}
				if(vesselMap.GetPixel(labelObject.GetIndex(0)) > 0) {
					System.out.println("Overlap");
					labelMapFilter.GetOutput().RemoveLabelObject(labelObject);
					continue;
				}
				if(labelObject.GetEquivalentSphericalRadius() > 15) { // big objects
					labelMapFilter.GetOutput().RemoveLabelObject(labelObject);
					continue;
				}
				

				double size = labelObject.GetPhysicalSize();
				double pixels = labelObject.GetNumberOfPixels();
				itkVectorD3 pmoments = labelObject.GetPrincipalMoments();
				double e = labelObject.GetElongation();
				System.out.println(pmoments.GetElement(0) +","+ pmoments.GetElement(1) +","+ pmoments.GetElement(2));
				System.out.println(e + " " + size+ " " + pixels);
			}
			System.out.println("Objects " + labels + "->" + labelMapFilter.GetOutput().GetNumberOfLabelObjects());
			
			itkLabelMapToBinaryImageFilterLM3IUC3 labelMapToMask = new itkLabelMapToBinaryImageFilterLM3IUC3();
			
			labelMapToMask.SetInput(labelMapFilter.GetOutput());
			orFilter.PushBackInput(labelMapToMask.GetOutput());
			
			System.out.println(orFilter.GetNumberOfInputs());
			if(orFilter.GetNumberOfInputs() > 1 && i > 0) {
					orFilter.Update();
					orFilter.PushBackInput(orFilter.GetOutput());
					orFilter.GetInput(0).DisconnectPipeline();
					orFilter.GetInput(1).DisconnectPipeline();
					orFilter.PopFrontInput();
					orFilter.PopFrontInput();
			}
			
			ImageProcessingUtils.toc();
			System.out.println(orFilter.GetNumberOfInputs());
		}
		orFilter.Update();
		
		ImageProcessingUtils.writeLabelMapOverlay(vesselMap, lungSegImage, "/Users/taznux/desktop/vessel.mha");
		
		return orFilter.GetOutput();
	}

}
