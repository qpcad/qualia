package com.qualia.controller;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.qualia.helper.DicomParser;
import com.qualia.helper.ItkImageArchive;
import com.qualia.helper.VtkImageArchive;
import com.qualia.model.MetaTableTreeModel;
import com.qualia.model.Metadata;
import com.qualia.view.MainView;
import org.itk.itkcommon.itkImageSS3;
import org.itk.itkiogdcm.itkGDCMImageIO;
import org.itk.itkioimagebase.itkImageSeriesReaderISS3;
import org.itk.itkvtkglue.itkImageToVTKImageFilterISS3;
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

            DicomParser parser = new DicomParser(dir.getPath());

            String[] uIds = parser.getUidList();

            for (int i = 0; i < uIds.length; i++) {
                Metadata data = parser.getMetadataByUid(uIds[i]);

                try {
                    if (metadataDao.queryForEq("uid", uIds[i]).isEmpty())
                        metadataDao.create(data);
                    else
                        System.out.println("duplicated data");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mMainView, "Database error");
                }
            }

            updateTable();

        } else {
            System.out.println("No selection");
            JOptionPane.showMessageDialog(mMainView, "No Selection");
        }
    }

    public void updateTable() {
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
            itkImageSS3 itkImages = loadDicomImages(targetMetadata.getFullFilenameList());
            ItkImageArchive.getInstance().setItkImage(targetMetadata.uId, itkImages);

            itkImageToVTKImageFilterISS3 itkVtkFilter =
                    new itkImageToVTKImageFilterISS3();

            //convert ITK to VTK
            itkVtkFilter.SetInput(itkImages);
            itkVtkFilter.Update();
            VtkImageArchive.getInstance().setVtkImage(targetMetadata.uId, itkVtkFilter.GetOutput());
        }

        vtkImageData vtkImage = VtkImageArchive.getInstance().getVtkImage(targetMetadata.uId);

        mMainView.updateRightPanel(vtkImage);
    }

    public void onTableDataDoubleClicked(Metadata targetMetadata) {
        mVtkViwerController = new VtkViewController(mMainView, targetMetadata);
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

    public void onDeleteBtnClicked(Metadata targetMetadata) {
        try {
            metadataDao.delete(targetMetadata);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateTable();
    }
}
