package com.qualia.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "metadata")
public class Metadata {
    public final static String KEY_PATIENT_NAME = "Patient's Name";
    public final static String KEY_PATIENT_ID = "Patient ID";
    public final static String KEY_PATIENT_BIRTHDAY = "Patient's Birth Date";
    public final static String KEY_PATIENT_SEX = "Patient's Sex";
    public final static String KEY_ACCESSION_NUMBER = "Accession Number";
    public final static String KEY_MODALITY = "Modality";
    public final static String KEY_STUDY_ID = "Study ID";
    public final static String KEY_ACQUISION_DATE = "Acquisition Date";
    public final static String KEY_CONTENT_DATE = "Content Date";
    public final static String KEY_INSTITUTE_NAME = "Institution Name";
    public final static String KEY_REFERRING_NAME = "Referring Physician's Name";



    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "uid")
    public String uId;
    @DatabaseField(columnName = "path")
    public String path;

    @DatabaseField(columnName = "patientId")
    public String patientId;
    @DatabaseField(columnName = "name")
    public String patientName;
    @DatabaseField(columnName = "birthday")
    public String patientBirthday;
    @DatabaseField(columnName = "sex")
    public String patientSex;
    @DatabaseField(columnName = "acc")
    public String accessionNumber;
    @DatabaseField(columnName = "modality")
    public String modality;
    @DatabaseField(columnName = "studyId")
    public String studyId;
    @DatabaseField(columnName = "acqDate")
    public String acquisionDate;
    @DatabaseField(columnName = "contentDate")
    public String contentDate;
    @DatabaseField(columnName = "institute")
    public String instituteName;
    @DatabaseField(columnName = "referName")
    public String referringName;

    Metadata(){

    }

    public Metadata(String path, String uId){
        this.path = path;
        this.uId = uId;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("uId").append(":").append(uId).append("\n");
        sb.append(KEY_PATIENT_NAME).append(":").append(patientName).append("\n");
        sb.append(KEY_PATIENT_ID).append(":").append(patientId).append("\n");
        sb.append(KEY_PATIENT_BIRTHDAY).append(":").append(patientBirthday).append("\n");
        sb.append(KEY_PATIENT_SEX).append(":").append(patientSex).append("\n");
        sb.append(KEY_ACCESSION_NUMBER).append(":").append(accessionNumber).append("\n");
        sb.append(KEY_MODALITY).append(":").append(modality).append("\n");
        sb.append(KEY_STUDY_ID).append(":").append(studyId).append("\n");
        sb.append(KEY_ACQUISION_DATE).append(":").append(acquisionDate).append("\n");
        sb.append(KEY_CONTENT_DATE).append(":").append(contentDate).append("\n");
        sb.append(KEY_INSTITUTE_NAME).append(":").append(instituteName).append("\n");
        sb.append(KEY_REFERRING_NAME).append(":").append(referringName).append("\n");


        return sb.toString();
    }
}
