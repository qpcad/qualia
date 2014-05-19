import ITKTest.DicomImages;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkcommon.itkMetaDataObjectBase;
import org.itk.itkcommon.itkMetaDataObjectS;
import org.itk.itkiogdcm.itkGDCMImageIO;
import org.junit.Test;

/**
 * Created by sapsaldog on 2014. 5. 19..
 */
public class ItkParseTest {
    @Test
    public void testParseMetadata(){
        DicomImages dicomImages = new DicomImages();

        String currentDir = System.getProperty("user.dir");
        System.out.println(currentDir);
        String[] uIds = dicomImages.scanDicomDirectory(currentDir + "/src/test/resources/13614193285030022"); //입력된 path스캔해서 시리즈 리스트 생성

        for (String uId: uIds)
            System.out.println(uId); // 각각의 시리즈 ID 출력

        assert(uIds[0].contentEquals("1.3.6.1.4.1.9328.50.3.69.22.500000512512"));

        String[] names = dicomImages.getDicomNames(uIds[0]); // 첫번째 시리즈(uIds[0]) 선택

        itkImageSS3 originalLungImage = dicomImages.loadDicomImages(names); // dcm파일로 부터 itkImage로 영상 로드
        itkImageSS3 lungImage = originalLungImage;

        String[] keys = originalLungImage.GetMetaDataDictionary().GetKeys(); // meta data는 dictionary 구조로 저장되어 있음, 현재 로드된 영상의 meta data에 사용된 key list 가져오기
        for (String t : keys) {
            String[] labelId = new String[1];
            String value;

            itkGDCMImageIO.GetLabelFromTag(t, labelId); // key를 이용하여 LabelID 가져오기

            // down casting MetaDataObjectBase to MetaDataObjectS, String기반의 데이터를 가져오기 위한 방법, C pointer를 이용하여 Base형의 데이터를 String으로 변환
            long pointer = itkMetaDataObjectBase.getCPtr(originalLungImage.GetMetaDataDictionary().Get(t)); // 포인터 가져오기

            itkMetaDataObjectS metaObject = new itkMetaDataObjectS(pointer, false); // 포인터를 이용하여 생성

            value = metaObject.GetMetaDataObjectValue(); // String형의 데이터 가져오기

            System.out.println(t + ", \"" + labelId[0] + "\", {" + value + "}"); // t는 key 8개의 숫자로 구성된 key, labelID[0]는 key의 설명, value는 각각의 key에대한 값
        }
    }
}
