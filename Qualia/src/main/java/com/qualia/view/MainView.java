package com.qualia.view;

import com.qualia.controller.MainViewController;
import com.qualia.model.MetaTableTreeModel;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainView extends JFrame {

	private static final long serialVersionUID = -1245702017236285965L;

    private MainViewController mViewController;

    private JXTreeTable mTreeTable;
    private JButton mBtnImport;

	public MainView(MainViewController controller, MetaTableTreeModel tableModel) {

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

        mTreeTable = new JXTreeTable(tableModel);
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
        mTreeTable.updateUI();
    }

}
