package com.qualia.controller;

import com.qualia.model.Metadata;
import com.qualia.model.OptionTableModel;
import com.qualia.view.VtkView;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import java.util.*;

public class VtkViewController {
    public VtkViewController(JFrame frame, Metadata target){
        VtkView dialog = new VtkView(target, this);
        dialog.pack();
        dialog.setVisible(true);

        dialog.setModel(target);
    }

    public void onModule1BtnClicked(JXTable optionTable){
        OptionTableModel model = (OptionTableModel) optionTable.getModel();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Specular", "1");
        map.put("Diffuse", "0.6");
        map.put("Ambiant", "0.5");

        model.setOptionMap(map);

        model.fireTableDataChanged();
    }

    public void onModule2BtnClicked(JXTable optionTable){
        OptionTableModel model = (OptionTableModel) optionTable.getModel();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Level", "2000");
        map.put("Balance", "0.7");

        model.setOptionMap(map);

        model.fireTableDataChanged();
    }


}
