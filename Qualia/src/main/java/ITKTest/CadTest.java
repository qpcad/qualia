package ITKTest;

import org.itk.itkcommon.*;
import org.itk.itkimagefunction.itkLinearInterpolateImageFunctionISS3D;
import org.itk.itkimagegrid.itkResampleImageFilterISS3ISS3;
import org.itk.itkimageintensity.itkMaskImageFilterISS3IUC3ISS3;
import org.itk.itkiogdcm.itkGDCMImageIO;
import org.itk.itktransform.itkIdentityTransformD3;

import java.io.File;
import java.io.FilenameFilter;

/**
 * <pre>
 * kr.qualia
 * CadTest.java
 * FIXME 클래스 설명
 * </pre>
 *
 * @author taznux
 * @date 2014. 4. 17.
 */
public class CadTest {
    /* Input file path */
    private static String currentDir = System.getProperty("user.dir");
    private static final String INPUT_DICOM_PATH = currentDir + "/src/test/resources/13614193285030022";
    //private static final String INPUT_DICOM_PATH = "/home/taznux/NCIA_DB/LIDC_old/13614193285030024/";
    private static final String OUTPUT_LUNG_IMAGE1 = "/Users/taznux/Desktop/lung1.mha";
    private static final String OUTPUT_LUNG_IMAGE2 = "/Users/taznux/Desktop/lung2.mha";
    private static final String OUTPUT_NODULE_IMAGE1 = "/Users/taznux/Desktop/nodule1.mha";

    /**
     * <pre>
     * 1.개요 : FIXME
     * 2.처리내용 : FIXME
     * </pre>
     *
     * @param argv
     * @method main
     */
    public static void main(String argv[]) {
        boolean isIsotropic = false;

        System.out.println("Lung DICOM Images load");

		/* DICOM Images Load */
        DicomImages dicomImages = new DicomImages();

        String[] uIds = dicomImages.scanDicomDirectory(INPUT_DICOM_PATH);
        for (String uId : uIds)
            System.out.println(uId);

        String[] names = dicomImages.getDicomNames(uIds[0]);

        itkImageSS3 originalLungImage = dicomImages.loadDicomImages(names);
        itkImageSS3 lungImage = originalLungImage;

        // get path
        String[] pathList = names[0].split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pathList.length - 1; i++) { // remove last term
            if (pathList[i].length() > 1) {
                sb.append("/");
                sb.append(pathList[i]);
                System.out.println(pathList[i]);
            }
        }
        String path = sb.toString();
        System.out.println(path);

        // find annotation xml file
        GenericExtFilter filter = new GenericExtFilter(".xml");
        File dirFile = new File(path);
        String[] filenameList = dirFile.list(filter);

        if (filenameList.length > 0) {
            String f = filenameList[0];
            String xmlPath = path + "/" + f;
            System.out.println(f);

            try {
                LidcXmlParser parser = new LidcXmlParser(xmlPath, lungImage);
                parser.parseXML();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        // print meta data
        String[] keys = originalLungImage.GetMetaDataDictionary().GetKeys();
        for (String t : keys) {
            String[] labelId = new String[1];
            String value;

            itkGDCMImageIO.GetLabelFromTag(t, labelId);

            // down casting MetaDataObjectBase to MetaDataObjectS
            long pointer = itkMetaDataObjectBase.getCPtr(originalLungImage.GetMetaDataDictionary().Get(t));
            itkMetaDataObjectS metaObject = new itkMetaDataObjectS(pointer, false);

            value = metaObject.GetMetaDataObjectValue();

            System.out.println(t + ", \"" + labelId[0] + "\", {" + value + "}");
        }


        System.out.println("Lung Image Interpolation");
        /* Interpolation from anisotropic to isotropic voxel*/
        if (isIsotropic == true) {
            ImageProcessingUtils.tic();
            itkResampleImageFilterISS3ISS3 resampleImage = new itkResampleImageFilterISS3ISS3();
            itkVectorD3 inputSpacing = originalLungImage.GetSpacing();
            itkVectorD3 outputSpacing = new itkVectorD3(1);
            itkIdentityTransformD3 identityTransform = new itkIdentityTransformD3();
            itkLinearInterpolateImageFunctionISS3D linearInterpolate = new itkLinearInterpolateImageFunctionISS3D();

            identityTransform.SetIdentity();

            itkSize3 inputSize = originalLungImage.GetLargestPossibleRegion().GetSize();
            itkSize3 outputSize = new itkSize3();

            for (int i = 0; i < 3; i++)
                outputSize.SetElement(i, (long) (inputSize.GetElement(i) * inputSpacing.GetElement(i) / outputSpacing.GetElement(i) + 0.5));

            resampleImage.SetInput(originalLungImage);
            resampleImage.SetTransform(identityTransform);
            resampleImage.SetInterpolator(linearInterpolate);
            resampleImage.SetOutputSpacing(outputSpacing);
            resampleImage.SetOutputOrigin(originalLungImage.GetOrigin());
            resampleImage.SetOutputDirection(originalLungImage.GetDirection());
            resampleImage.SetSize(outputSize);

            resampleImage.Update();

            lungImage = resampleImage.GetOutput();
            ImageProcessingUtils.toc();
        }

        System.out.println("Lung Segmentation");
        /* Lung Segmentation */
        LungSegmentation lungSegFilter = new LungSegmentation();
        lungSegFilter.setLungImage(lungImage);
        lungSegFilter.run();


		/* Lung Masking */
        itkMaskImageFilterISS3IUC3ISS3 maskFilter = new itkMaskImageFilterISS3IUC3ISS3();

        maskFilter.SetInput1(lungImage);
        maskFilter.SetInput2(lungSegFilter.getLungMask());
        maskFilter.SetOutsideValue((short) -2000);
        maskFilter.Update();

        itkImageSS3 lungSegImage;
        lungSegImage = maskFilter.GetOutput();

		/* Output Lung Segmentation */
        ImageProcessingUtils.writeImage(lungSegImage, OUTPUT_LUNG_IMAGE1);
        ImageProcessingUtils.writeImage(lungSegFilter.getLabelImage(), OUTPUT_LUNG_IMAGE2);


        System.out.println("Nodule Candidates Detection");
		/* Nodule Candidates Detection */
        NoduleCandidatesDetection noduleCandidateDetection = new NoduleCandidatesDetection();
        noduleCandidateDetection.setLungImage(lungImage);
        noduleCandidateDetection.setLungMask(lungSegFilter.getLungMask());
        noduleCandidateDetection.run();

        ImageProcessingUtils.writeLabelMapOverlay(noduleCandidateDetection.getNoduleCandidates(), lungSegImage, OUTPUT_NODULE_IMAGE1);
    }

    /**
     * <pre>
     * kr.qualia
     * CadTest.java
     * FIXME 클래스 설명
     * </pre>
     *
     * @author taznux
     * @date 2014. 4. 17.
     */
    public static class GenericExtFilter implements FilenameFilter {

        private String ext;

        public GenericExtFilter(String ext) {
            this.ext = ext;
        }

        public boolean accept(File dir, String name) {
            return (name.endsWith(ext));
        }
    }
}