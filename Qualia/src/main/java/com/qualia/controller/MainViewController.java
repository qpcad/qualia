package com.qualia.controller;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.qualia.itk.helper.DicomParser;
import com.qualia.model.Metadata;
import com.qualia.view.MainView;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;

public class MainViewController {
    private MainView mMainView;

    private final static String DATABASE_URL = "jdbc:sqlite:qualia.db:metadata";

    private ConnectionSource connectionSource = null;
    private Dao<Metadata, Integer> metadataDao;

    public MainViewController(){
        this.init();

        mMainView = new MainView(this, metadataDao);
        mMainView.init();
    }

    private void init() {
        try{
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            metadataDao = DaoManager.createDao(connectionSource, Metadata.class);

            TableUtils.dropTable(connectionSource, Metadata.class, true);
            TableUtils.createTable(connectionSource, Metadata.class);

        }catch(Exception e){
            JOptionPane.showMessageDialog(mMainView, "Database error");
        }

    }

    public void onImportBtnClicked(MouseEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showSaveDialog(mMainView);

        File dir = fileChooser.getSelectedFile();

        if (dir != null) {
            System.out.println(dir.getPath());

            DicomParser parser = new DicomParser(dir.getPath());

            String[] uIds = parser.getUidList();

            for(int i=0;i<uIds.length;i++){
                Metadata data = parser.getMetadataByUid(uIds[i]);

                try{
                    metadataDao.create(data);
                }catch (Exception e){
                    JOptionPane.showMessageDialog(mMainView, "Database error");
                }
            }

            mMainView.updateMetaTable();

        } else {
            System.out.println("No selection");
            JOptionPane.showMessageDialog(mMainView, "No Selection");
        }


    }


    public static void main(String[] args) throws Exception {
        new MainViewController();
    }


}
