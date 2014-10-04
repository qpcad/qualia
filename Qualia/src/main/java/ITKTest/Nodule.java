package ITKTest;

import org.itk.itklabelmap.itkStatisticsLabelObjectUL3;

/**
 * <pre>
 * kr.qualia
 * Nodule.java
 * FIXME 클래스 설명
 * </pre>
 *
 * @author taznux
 * @date 2014. 4. 17.
 */
public class Nodule {
    int subtlety;
    int internalStructure;
    int calcification;
    int margin;
    int lobulation;
    int spiculation;
    int texture;
    int malignancy;
    private String noduleUID_;
    private long label_;
    private itkStatisticsLabelObjectUL3 noduleObj;

    public Nodule() {
        subtlety = 0;
        internalStructure = 0;
        calcification = 0;
        margin = 0;
        lobulation = 0;
        spiculation = 0;
        texture = 0;
        malignancy = 0;
    }

    public itkStatisticsLabelObjectUL3 getNoduleObj() {
        return noduleObj;
    }

    public void setNoduleObj(itkStatisticsLabelObjectUL3 noduleObj) {
        setLabel(noduleObj.GetLabel());
        this.noduleObj = noduleObj;
    }

    public String getNoduleUID() {
        return noduleUID_;
    }

    public void setNoduleUID(String noduleUID) {
        noduleUID_ = noduleUID;
    }

    public long getLabel() {
        return label_;
    }

    public void setLabel(long label) {
        label_ = label;
    }
}
