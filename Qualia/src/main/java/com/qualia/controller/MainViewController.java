package com.qualia.controller;

import com.qualia.view.MainView;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class MainViewController {
    MainView mMainView;


    public MainViewController(){
        mMainView = new MainView(this);
        mMainView.init();
    }

    public void onImportBtnClicked(MouseEvent event){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.showSaveDialog(mMainView);
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): "
                    + fileChooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : "
                    + fileChooser.getSelectedFile());
        } else {
            System.out.println("No Selection ");
        }
    }

    public static void main(String[] args) throws Exception {
        new MainViewController();
    }


}
