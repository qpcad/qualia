package com.qualia.controller;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.qualia.helper.DicomLoader;
import com.qualia.helper.DicomParser;
import com.qualia.helper.ItkImageArchive;
import com.qualia.helper.VtkImageArchive;
import com.qualia.model.MetaTableTreeModel;
import com.qualia.model.Metadata;
import com.qualia.view.MainView;
import org.itk.itkcommon.itkImageSS3;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.TwilightSkin;
import vtk.vtkImageData;
import vtk.vtkNativeLibrary;

import javax.swing.*;
import java.io.File;
import java.sql.SQLException;

public class MainViewController {
    static {
        if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
            for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
                if (!lib.IsLoaded()) {
                    System.out.println(lib.GetLibraryName() + " not loaded");
                }
            }
        }
        vtkNativeLibrary.DisableOutputWindow(null);
    }

    private final static String DATABASE_URL = "jdbc:sqlite:qualia.db:metadata";
    private MainView mMainView;
    private ConnectionSource connectionSource = null;
    private Dao<Metadata, Integer> metadataDao;

    private VtkViewController mVtkViwerController;

    public MainViewController() {
        MetaTableTreeModel metaTableTreeModel = this.init();

        mMainView = new MainView(this, metaTableTreeModel);
        mMainView.init();
    }

    public static void main(String[] args) throws Exception {
        SubstanceLookAndFeel.setSkin(new TwilightSkin());
        new MainViewController();
    }

    private MetaTableTreeModel init() {
        MetaTableTreeModel metaTableTreeModel = new MetaTableTreeModel();

        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            metadataDao = DaoManager.createDao(connectionSource, Metadata.class);

            if (!metadataDao.isTableExists()) {
                TableUtils.dropTable(connectionSource, Metadata.class, true);
                TableUtils.createTable(connectionSource, Metadata.class);
            } else {
                metadataDao.queryForAll();
                metaTableTreeModel.updatePatientList(metadataDao);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mMainView, "Database error");
        }

        return metaTableTreeModel;
    }

    public void onImportBtnClicked() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showSaveDialog(mMainView);

        File dir = fileChooser.getSelectedFile();

        if (dir != null) {
            System.out.println(dir.getPath());

            DicomParser parser = new DicomParser(this, dir.getPath());
            Thread th = new Thread(parser);
            th.start();

        } else {
            System.out.println("No selection");
            JOptionPane.showMessageDialog(mMainView, "No Selection");
        }
    }

    public synchronized void addDicomImages(Metadata data) {
        try {
            if (metadataDao.queryForEq("uid", data.uId).isEmpty())
                metadataDao.create(data);
            else
                System.out.println("duplicated data");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mMainView, "Database error");
        }
    }

    public synchronized void updateTable() {
        MetaTableTreeModel metaTableTreeModel = new MetaTableTreeModel();
        try {
            metadataDao.queryForAll();
            metaTableTreeModel.updatePatientList(metadataDao);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mMainView, "Database error");
        }
        mMainView.updateMetaTable(metaTableTreeModel);
    }

    public void onTableDataClicked(Metadata targetMetadata) {
        System.out.println("clicked");
        System.out.println(targetMetadata.toString());


        itkImageSS3 itkImage = ItkImageArchive.getInstance().getItkImage(targetMetadata.uId);

        if (itkImage == null) {
            DicomLoader dicomLoader = new DicomLoader(mMainView, targetMetadata);
            Thread th = new Thread(dicomLoader);
            th.start();
        } else {
            vtkImageData vtkImage = VtkImageArchive.getInstance().getVtkImage(targetMetadata.uId);
            mMainView.updateRightPanel(vtkImage);
        }
    }

    public void onTableDataDoubleClicked(Metadata targetMetadata) {
        itkImageSS3 itkImage = ItkImageArchive.getInstance().getItkImage(targetMetadata.uId);

        if (itkImage == null) {
            DicomLoader dicomLoader = new DicomLoader(mMainView, targetMetadata);
            dicomLoader.run();
        }
        mVtkViwerController = new VtkViewController(mMainView, targetMetadata);
    }

    public void onDeleteBtnClicked(Metadata targetMetadata) {
        try {
            metadataDao.delete(targetMetadata);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateTable();
    }
}
