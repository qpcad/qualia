import com.qualia.helper.DicomParser;
import com.qualia.model.Metadata;
import org.junit.Before;
import org.junit.Test;
import vtk.vtkImageData;
import vtk.vtkRenderWindowPanel;

public class ItkParseTest {
    DicomParser mParser;

    @Before
    public void init(){
        new vtkRenderWindowPanel();
        String currentDir = System.getProperty("user.dir");
        String path = currentDir + "/src/test/resources/13614193285030022";

        mParser = new DicomParser(path);

    }

    @Test
    public void testMetadataLoad(){
        String[] uIds = mParser.getUidList();
        Metadata data = mParser.getMetadataByUid(uIds[0]);

        System.out.println(data.toString());

        assert(data!=null);
    }

    @Test
    public void test(){
        String[] uIds = mParser.getUidList();
        vtkImageData vtkData = mParser.getVtkImageByUid(uIds[0]);

        assert(vtkData!=null);

    }
}
