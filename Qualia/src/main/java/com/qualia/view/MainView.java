package com.qualia.view;

import com.j256.ormlite.dao.Dao;
import com.qualia.controller.MainViewController;
import com.qualia.model.MetaTableTreeModel;
import com.qualia.model.Metadata;
import com.qualia.model.Patient;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Vector;

public class MainView extends JFrame {

	private static final long serialVersionUID = -1245702017236285965L;

    private MainViewController mViewController;
    private Dao<Metadata, Integer> mMetaModel;


    private JXTreeTable mTreeTable;
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

        mTreeTable = new JXTreeTable(new MetaTableTreeModel());
        mTreeTable.setRootVisible(false);

		getContentPane().add(new JScrollPane(mTreeTable), BorderLayout.CENTER);

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

    public void updateMetaTable(){
        try{
            mMetaModel.queryForAll();

            HashMap<String, Vector<Metadata>> patientMap =
                    new HashMap<String, Vector<Metadata>>();

            for (Metadata metadata : mMetaModel) {
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

            MetaTableTreeModel model = (MetaTableTreeModel) mTreeTable.getTreeTableModel();
            model.setPatientList(patientVector);

            mTreeTable.updateUI();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
