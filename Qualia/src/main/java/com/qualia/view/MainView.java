package com.qualia.view;

import com.qualia.controller.MainViewController;
import com.qualia.model.MetaTableTreeModel;
import com.qualia.model.Metadata;
import org.jdesktop.swingx.JXTreeTable;
import vtk.vtkImageViewer2;
import vtk.vtkInteractorStyleImage;
import vtk.vtkRenderWindowPanel;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainView extends JFrame {

	private static final long serialVersionUID = -1245702017236285965L;

    private MainViewController mViewController;

    private JXTreeTable mTreeTable;
    private JButton mBtnImport;
    private vtkRenderWindowPanel mRightPanel;

	public MainView(final MainViewController controller, final MetaTableTreeModel tableModel) {

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

        JButton btnQuery = new JButton("Query");
        toolBar.add(btnQuery);

        JButton btnSend = new JButton("Send");
        toolBar.add(btnSend);

		JButton toolbarBtnEtc = new JButton("Etc");
		toolBar.add(toolbarBtnEtc);

        mTreeTable = new JXTreeTable(tableModel);
        mTreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {


                TreePath treePath = mTreeTable.getTreeSelectionModel().getSelectionPath();
                Object target = treePath.getLastPathComponent();
                System.out.println(target.toString());

                if(target instanceof Metadata){
                    controller.onTableDataClicked((Metadata) target);
                }

                if((mouseEvent.getClickCount()==2)&&(target instanceof Metadata)){
                    System.out.println("double clicked");
                    controller.onTableDataDoubleClicked((Metadata) target);
                }

            }
        });
        mTreeTable.setRootVisible(false);

		getContentPane().add(new JScrollPane(mTreeTable), BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.SOUTH);

        JPanel panel_1 = new JPanel();
        splitPane.setLeftComponent(panel_1);

        mRightPanel = new vtkRenderWindowPanel();
		splitPane.setRightComponent(mRightPanel);
        mRightPanel.setInteractorStyle(new vtkInteractorStyleImage());
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

    public void updateRightPanel(vtkImageViewer2 imageViewer){
        imageViewer.SetRenderWindow(mRightPanel.GetRenderWindow());
        imageViewer.SetupInteractor(mRightPanel.GetRenderWindow().GetInteractor());

        imageViewer.SetColorLevel(-500);
        imageViewer.SetColorWindow(3000);

        int sliceMin, sliceMax, sliceMiddle;

        sliceMin = imageViewer.GetSliceMin();
        sliceMax = imageViewer.GetSliceMax();
        sliceMiddle = (sliceMax-sliceMin)/2;

        imageViewer.SetSlice(sliceMiddle);
        imageViewer.SetSliceOrientationToXY();

        mRightPanel.Render();
    }
}
