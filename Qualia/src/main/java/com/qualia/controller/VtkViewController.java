package com.qualia.controller;

import com.qualia.model.Metadata;
import com.qualia.view.VtkView;

import javax.swing.*;

public class VtkViewController {
    public VtkViewController(JFrame frame, Metadata target){
        JDialog dialog = new VtkView(target);
        dialog.pack();
        dialog.setVisible(true);

    }
}
