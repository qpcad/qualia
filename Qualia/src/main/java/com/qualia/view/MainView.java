package com.qualia.view;

import com.j256.ormlite.dao.Dao;
import com.qualia.controller.MainViewController;
import com.qualia.model.Metadata;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainView extends JFrame {

	private static final long serialVersionUID = -1245702017236285965L;

    private MainViewController mViewController;
    private Dao<Metadata, Integer> mMetaModel;

	private JTable mTableMeta;
    private JButton mBtnImport;

	public MainView(MainViewController controller, Dao<Metadata, Integer> metadataDao) {
        mMetaModel = metadataDao;

        mViewController = controller;

		getContentPane().setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);

        mBtnImport = new JButton("Import");
        mBtnImport.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
                mViewController.onImportBtnClicked(event);
			}
		});
		toolBar.add(mBtnImport);

		JButton toolbarBtnExport = new JButton("Export");
		toolBar.add(toolbarBtnExport);

		JButton toolbarBtnMetadata = new JButton("Meta-data");
		toolBar.add(toolbarBtnMetadata);

		JButton toolbarBtnDelete = new JButton("Delete");
		toolBar.add(toolbarBtnDelete);

		JButton toolbarBtnSearch = new JButton("Search");
		toolBar.add(toolbarBtnSearch);

		JButton toolbarBtnEtc = new JButton("Etc");
		toolBar.add(toolbarBtnEtc);

        DefaultTableModel metaModel = new DefaultTableModel(
                new String[] {
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
                        Metadata.KEY_REFERRING_NAME,
                },
                0
        );

        mTableMeta = new JTable(metaModel);

		getContentPane().add(new JScrollPane(mTableMeta), BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.SOUTH);

		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);

		JPanel panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
	}

    public void init(){

        this.setPreferredSize(new Dimension(1024, 768));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void addTableColumn(Metadata data){
        DefaultTableModel model = (DefaultTableModel) mTableMeta.getModel();
        model.addRow(new String[] {
                data.patientName,
                data.patientId,
                data.patientBirthday,
                data.patientSex,
                data.accessionNumber,
                data.modality,
                data.studyId,
                data.acquisionDate,
                data.contentDate,
                data.instituteName,
                data.referringName
        });

    }

    public void updateMetaTable(){
        try{
            mMetaModel.queryForAll();

            for (Metadata metadata : mMetaModel) {
                this.addTableColumn(metadata);
            }
        }catch(Exception e){

        }
    }

}
