package com.qualia.model;

import com.j256.ormlite.dao.Dao;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import java.util.*;

public class MetaTableTreeModel extends AbstractTreeTableModel{
    private  final static String[] COLUMN_NAMES = {
            Metadata.KEY_PATIENT_NAME,
            Metadata.KEY_PATIENT_ID,
            Metadata.KEY_PATIENT_BIRTHDAY,
            Metadata.KEY_PATIENT_SEX,
            Metadata.KEY_ACCESSION_NUMBER,
            Metadata.KEY_MODALITY,
            Metadata.KEY_STUDY_ID,
            Metadata.KEY_ACQUISION_DATE,
            Metadata.KEY_CONTENT_DATE,
            Metadata.KEY_INSTITUTE_NAME,
            Metadata.KEY_REFERRING_NAME
    };

    Vector<Patient> mPatientList = new Vector<Patient>();

    public MetaTableTreeModel(){
        super(new Object());
    }

    public void updatePatientList(Dao<Metadata, Integer> metaDao){
        HashMap<String, Vector<Metadata>> patientMap =
                new HashMap<String, Vector<Metadata>>();

        for (Metadata metadata : metaDao) {
            Vector<Metadata> metaList = patientMap.get(metadata.patientId);

            if(metaList==null){
                metaList = new Vector<Metadata>();
                patientMap.put(metadata.patientId, metaList);
            }

            metaList.add(metadata);
        }

        Vector<Patient> patientVector = new Vector<Patient>();

        patientMap.keySet();

        for(String key : patientMap.keySet()){
            Patient patient = new Patient();
            patient.setMetaDataList(patientMap.get(key));
            patientVector.add(patient);
        }

        mPatientList = patientVector;

    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof Metadata;
    }

    @Override
    public int getChildCount(Object parent) {
        if(parent instanceof Patient){
            Patient patient = (Patient) parent;
            return patient.getMetaDataList().size();
        }

        return mPatientList.size();
    }

    @Override
    public Object getChild(Object parent, int index) {
        if(parent instanceof Patient){
            Patient patient = (Patient) parent;
            return patient.getMetaDataList().get(index);
        }

        return mPatientList.get(index);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        Patient patient = (Patient) parent;
        Metadata metadata = (Metadata) child;
        return patient.getMetaDataList().indexOf(metadata);
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (node instanceof Patient) {
            Patient patient = (Patient) node;
            switch (column){
                case 0:
                    return patient.getPatientName();
                case 1:
                    return patient.getPatientId();
                case 2:
                    return patient.getPatientAge();
                case 3:
                    return patient.getPatientSex();
            }
        } else if (node instanceof Metadata){
            Metadata metadata = (Metadata) node;
            switch (column){
                case 0:
                case 1:
                case 2:
                case 3:
                    return "";
                case 4:
                    return metadata.accessionNumber;
                case 5:
                    return metadata.modality;
                case 6:
                    return metadata.studyId;
                case 7:
                    return metadata.acquisionDate;
                case 8:
                    return metadata.contentDate;
                case 9:
                    return metadata.instituteName;
                case 10:
                    return metadata.referringName;

            }
        }

        return null;
    }
}
