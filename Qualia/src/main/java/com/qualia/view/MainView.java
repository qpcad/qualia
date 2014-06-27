package com.qualia.view;

import com.qualia.controller.MainViewController;
import com.qualia.model.MetaTableTreeModel;
import com.qualia.model.Metadata;
import org.jdesktop.swingx.JXTreeTable;
import vtk.vtkImageData;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class MainView extends JFrame {

    private static final long serialVersionUID = -1245702017236285965L;
    private final JButton mBtnDelete;
    private MainViewController mViewController;
    private JXTreeTable mTreeTable;
    private JButton mBtnImport;
    private VtkSliceRenderPanel mRightPanel;
    private int[] columnWidth = {
        220,
        180,
        80,
        40,
        40,
        55,
        55,
        75,
        75,
        75,
        155
    };

    public MainView(final MainViewController controller, final MetaTableTreeModel tableModel) {

        mViewController = controller;

        getContentPane().setLayout(new BorderLayout(0, 0));

        JToolBar toolBar = new JToolBar();
        getContentPane().add(toolBar, BorderLayout.NORTH);

        mBtnImport = getImageButton("Import", "icon_Import.png", 32, 32);

        mBtnImport.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                mViewController.onImportBtnClicked();
            }
        });
        toolBar.add(mBtnImport);


        JButton toolbarBtnExport = getImageButton("Export", "icon_Export.png", 32, 32);
        toolBar.add(toolbarBtnExport);

        JButton toolbarBtnMetadata = getImageButton("Metadata", "icon_MetaData.png", 32, 32);
        toolBar.add(toolbarBtnMetadata);

        mBtnDelete = getImageButton("Delete", "icon_Delete.png", 32, 32);
        mBtnDelete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                TreePath treePath = mTreeTable.getTreeSelectionModel().getSelectionPath();
                Object target = treePath.getLastPathComponent();
                System.out.println(target.toString());

                mViewController.onDeleteBtnClicked((Metadata) target);
            }
        });
        toolBar.add(mBtnDelete);

        JButton toolbarBtnSearch = getImageButton("Search","icon_Search.png", 32, 32);
        toolBar.add(toolbarBtnSearch);

//        JButton btnQuery = getImageButton("icon_.png", 32, 32);
//        toolBar.add(btnQuery);
//
//        JButton btnSend = getImageButton("src/main/resources/icon_Import.png", 32, 32);
//        toolBar.add(btnSend);

        JButton toolbarBtnEtc = getImageButton("Etc", "icon_Etc.png", 32, 32);
        toolBar.add(toolbarBtnEtc);

        mTreeTable = new JXTreeTable(tableModel);
        mTreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {


                TreePath treePath = mTreeTable.getTreeSelectionModel().getSelectionPath();
                Object target = treePath.getLastPathComponent();
                System.out.println(target.toString());

                if (target instanceof Metadata) {
                    controller.onTableDataClicked((Metadata) target);
                }

                if ((mouseEvent.getClickCount() == 2) && (target instanceof Metadata)) {
                    System.out.println("double clicked");
                    controller.onTableDataDoubleClicked((Metadata) target);
                }

            }
        });
        mTreeTable.setRootVisible(false);

        changeTableColumnWidth(mTreeTable, columnWidth);



        getContentPane().add(new JScrollPane(mTreeTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane();
        getContentPane().add(splitPane, BorderLayout.SOUTH);

        JPanel panel_1 = new JPanel();
        splitPane.setLeftComponent(panel_1);

        mRightPanel = new VtkSliceRenderPanel();
        splitPane.setRightComponent(mRightPanel);
    }

    private static void changeTableColumnWidth(JTable table, int[] widthList) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        TableColumn column;

        for (int i = 0; i < widthList.length; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setCellRenderer(centerRenderer);
            column.setPreferredWidth(widthList[i]);
        }
    }

    public void init() {
        this.setPreferredSize(new Dimension(1024, 768));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    public void updateMetaTable(MetaTableTreeModel tableModel) {
        mTreeTable.setTreeTableModel(tableModel);
        changeTableColumnWidth(mTreeTable, columnWidth);
        mTreeTable.updateUI();
    }

    public void updateRightPanel(vtkImageData inputImage) {
        mRightPanel.render(inputImage, VtkSliceRenderPanel.ORIENTATION_XY);
    }

    private JButton getImageButton(String name, String resources, int width, int height){
        URL resourceUrl = getClass().getClassLoader().getResource(resources);
        ImageIcon iconImport = new ImageIcon(resourceUrl);

        Image imageImport = iconImport.getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return  new JButton(new ImageIcon(imageImport));
    }
}
