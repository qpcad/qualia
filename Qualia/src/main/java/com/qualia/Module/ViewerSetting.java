package com.qualia.Module;

import com.qualia.view.VtkView;

/**
 * Created by taznux on 2014. 6. 24..
 */
public class ViewerSetting extends ModuleBase {
    VtkView mDialog;

    public ViewerSetting(VtkView dialog) {
        name = "Viewer Setting";

        mDialog = dialog;

        initializePanel();
    }

    @Override
    public void run() {

    }
}
