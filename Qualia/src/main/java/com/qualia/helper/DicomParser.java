package com.qualia.helper;

import com.qualia.model.Metadata;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkcommon.itkMetaDataObjectBase;
import org.itk.itkcommon.itkMetaDataObjectS;
import org.itk.itkiogdcm.itkGDCMImageIO;
import org.itk.itkiogdcm.itkGDCMSeriesFileNames;
import org.itk.itkioimagebase.itkImageSeriesReaderISS3;
import org.itk.itkvtkglue.itkImageToVTKImageFilterISS3;
import vtk.vtkRenderWindowPanel;

import java.util.HashMap;

public class DicomParser {
    private HashMap<String, Metadata> mMetaMap;

    private itkGDCMSeriesFileNames dicomNames_;
    private String[] mUidList;

    private String mRootPath;

    public DicomParser(String path){
        mMetaMap = new HashMap<String, Metadata>();

        mRootPath = path;
        dicomNames_ = new itkGDCMSeriesFileNames();

        dicomNames_.SetRecursive(true);
        dicomNames_.SetUseSeriesDetails(true);
        dicomNames_.SetDirectory(path);
        mUidList = dicomNames_.GetSeriesUIDs();

        for(int i=0;i<mUidList.length;i++){
            String uid = mUidList[i];
            System.out.println("try to parse : " + uid);
            String[] fullFilenameList = dicomNames_.GetFileNames(uid);

            itkImageSS3 itkImages = this.loadDicomImages(fullFilenameList);

            itkImageToVTKImageFilterISS3 itkVtkFilter =
                    new itkImageToVTKImageFilterISS3();

            //convert ITK to VTK
            itkVtkFilter.SetInput(itkImages);
            itkVtkFilter.Update();
            VtkImageArchive.getInstance().setVtkImage(uid, itkVtkFilter.GetOutput());

            Metadata data = new Metadata(mRootPath, uid);

            String[] keys = itkImages.GetMetaDataDictionary().GetKeys();
            for (String t : keys) {
                String[] labelId = new String[1];
                String value;

                // key를 이용하여 LabelID 가져오기
                itkGDCMImageIO.GetLabelFromTag(t, labelId);

                // down casting MetaDataObjectBase to MetaDataObjectS,
                // String기반의 데이터를 가져오기 위한 방법,
                // C pointer를 이용하여 Base형의 데이터를 String으로 변환

                // 포인터 가져오기
                long pointer = itkMetaDataObjectBase.getCPtr(itkImages.GetMetaDataDictionary().Get(t));

                // 포인터를 이용하여 생성
                itkMetaDataObjectS metaObject = new itkMetaDataObjectS(pointer, false);

                // String형의 데이터 가져오기
                value = metaObject.GetMetaDataObjectValue();


                if(labelId[0].contentEquals(Metadata.KEY_PATIENT_NAME))
                    data.patientName = value;

                if(labelId[0].contentEquals(Metadata.KEY_PATIENT_ID))
                    data.patientId = value;

                if(labelId[0].contentEquals(Metadata.KEY_PATIENT_BIRTHDAY))
                    data.patientBirthday = value;

                if(labelId[0].contentEquals(Metadata.KEY_PATIENT_SEX))
                    data.patientSex = value;

                if(labelId[0].contentEquals(Metadata.KEY_ACCESSION_NUMBER))
                    data.accessionNumber = value;

                if(labelId[0].contentEquals(Metadata.KEY_MODALITY))
                    data.modality = value;

                if(labelId[0].contentEquals(Metadata.KEY_STUDY_ID))
                    data.studyId = value;

                if(labelId[0].contentEquals(Metadata.KEY_ACQUISION_DATE))
                    data.acquisionDate = value;

                if(labelId[0].contentEquals(Metadata.KEY_CONTENT_DATE))
                    data.contentDate = value;

                if(labelId[0].contentEquals(Metadata.KEY_INSTITUTE_NAME))
                    data.instituteName = value;

                if(labelId[0].contentEquals(Metadata.KEY_REFERRING_NAME))
                    data.referringName = value;
            }

            mMetaMap.put(uid, data);
        }
    }

    public String[] getUidList(){
        return mUidList;
    }


    public Metadata getMetadataByUid(String uId){
        return mMetaMap.get(uId);
    }

    private itkImageSS3 loadDicomImages(String[] fullFilenameList) {
        // read
        itkImageSeriesReaderISS3 reader = new itkImageSeriesReaderISS3();
        itkGDCMImageIO dicomIO = new itkGDCMImageIO();

        reader.SetFileNames(fullFilenameList);
        reader.SetImageIO(dicomIO);
        reader.Update();

        itkImageSS3 lungImage = reader.GetOutput();
        lungImage.SetMetaDataDictionary(dicomIO.GetMetaDataDictionary());

        return lungImage;
    }

    static{
        new vtkRenderWindowPanel();
    }

}
