package com.qualia.model;

import java.util.Vector;

public class Patient {

    private Vector<Metadata> mMetaDataList;

    public Vector<Metadata> getMetaDataList() {
        return mMetaDataList;
    }

    public void setMetaDataList(Vector<Metadata> metaDataList) {
        mMetaDataList = metaDataList;
    }

    public String getPatientId() {
        return mMetaDataList.get(0).patientId;
    }

    public String getPatientName() {
        return mMetaDataList.get(0).patientName;
    }

    public String getPatientAge() {
        return mMetaDataList.get(0).patientBirthday;
    }

    public String getPatientSex() {
        return mMetaDataList.get(0).patientSex;
    }


}
