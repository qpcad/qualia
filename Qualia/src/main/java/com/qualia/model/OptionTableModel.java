package com.qualia.model;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class OptionTableModel extends AbstractTableModel{

    HashMap<String, String> mMap = new HashMap<String, String>();
    Vector<String> mKeyList = new Vector<String>();
    Vector<String> mValueList = new Vector<String>();

    String[] column = {
            "Key", "Value"
    };

    public OptionTableModel(){
        HashMap<String, String> optionMap = new HashMap<String, String>();
        this.setOptionMap(optionMap);
    }

    public void setOptionMap(HashMap<String, String> map){
        this.mMap = map;

        mKeyList.clear();
        for(String key : mMap.keySet()){
            mKeyList.add(key);
            System.out.println(key);
        }

        mValueList.clear();
        for(String value : mMap.values()){
            mValueList.add(value);
            System.out.println(value);
        }

    }

    @Override
    public int getColumnCount() {
        return column.length;
    }

    @Override
    public String getColumnName(int i) {
        return column[i];
    }

    @Override
    public int getRowCount() {
        return mKeyList.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        if(col==0){
            return mKeyList.get(row);
        }else{
            return mValueList.get(row);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col==1);
    }
}
