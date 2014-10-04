import com.qualia.controller.VtkViewController;
import com.qualia.helper.DicomParser;
import com.qualia.model.Metadata;
import org.junit.Before;
import org.junit.Test;
import vtk.vtkRenderWindowPanel;

import javax.swing.*;
import java.awt.*;

public class ItkParseTest{
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
    public void testPopup() throws InterruptedException {
        String[] uIds = mParser.getUidList();

        JFrame frame = new JFrame();
        new VtkViewController(frame, mParser.getMetadataByUid(uIds[0]));
        frame.setPreferredSize(new Dimension(1024, 768));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        Thread.sleep(100000);

    }

}
