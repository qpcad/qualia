import ITKTest.DicomImages;
import org.junit.Test;

/**
 * Created by sapsaldog on 2014. 5. 19..
 */
public class ItkParseTest {
    @Test
    public void testParseID(){
        DicomImages dicomImages = new DicomImages();

        String currentDir = System.getProperty("user.dir");
        System.out.println(currentDir);
        String[] uIds = dicomImages.scanDicomDirectory(currentDir + "/src/test/resources/13614193285030022"); //입력된 path스캔해서 시리즈 리스트 생성

        for (String uId: uIds)
            System.out.println(uId); // 각각의 시리즈 ID 출력

        assert(uIds[0].contentEquals("1.3.6.1.4.1.9328.50.3.69.22.500000512512"));

    }
}
