package com.qualia.model;

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

    public String patientId;
    public String patientName;
    public String patientBirthday;
    public String patientSex;
    public String accessionNumber;
    public String modality;
    public String studyId;
    public String acquisionDate;
    public String contentDate;
    public String instituteName;
    public String referringName;

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
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
