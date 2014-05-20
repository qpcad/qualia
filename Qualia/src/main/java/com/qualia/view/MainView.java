package com.qualia.view;

import com.qualia.controller.MainViewController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainView extends JFrame {

	private static final long serialVersionUID = -1245702017236285965L;

    private MainViewController mViewController;

	private JTable mTableMeta;
    private JButton mBtnImport;


	public MainView(MainViewController controller) {
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

        mTableMeta = new JTable();
        mTableMeta.setModel(new DefaultTableModel(new Object[][] { { null, null,
				null, null, null, null, null, null, null, null, null }, },
				new String[] { "Patient's Name", "Patient ID ",
						"Patient's Birth Date", "Patient's Sex",
						"Accession Number", "Modality", "Study ID",
						"Acquisition Date", "Content Date", "Institution Name",
						"Referring Physician's Name" }));
        mTableMeta.getColumnModel().getColumn(0).setPreferredWidth(86);
        mTableMeta.getColumnModel().getColumn(1).setPreferredWidth(62);
        mTableMeta.getColumnModel().getColumn(2).setPreferredWidth(110);
        mTableMeta.getColumnModel().getColumn(4).setPreferredWidth(111);
		getContentPane().add(mTableMeta, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.SOUTH);

		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);

		JPanel panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
	}

    public void init(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

}
