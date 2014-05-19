import com.qualia.itk.helper.DicomParser;
import com.qualia.model.Metadata;
import org.junit.Test;

public class ItkParseTest {
    @Test
    public void testDicomParser(){
        String currentDir = System.getProperty("user.dir");
        String path = currentDir + "/src/test/resources/13614193285030022";

        DicomParser parser = new DicomParser(path);

        String[] uIds = parser.getUidList();
        Metadata data = parser.getMetadataByUid(uIds[0]);

        System.out.println(data.toString());

    }
}
