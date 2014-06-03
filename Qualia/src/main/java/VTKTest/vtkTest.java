package VTKTest;


import vtk.*;

import javax.swing.*;
import java.awt.*;

public class vtkTest extends JFrame {
    /* Load VTK shared librarires (.dll) on startup, print message if not found */
    static {
        if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
            for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
                if (!lib.IsLoaded())
                    System.out.println(lib.GetLibraryName() + " not loaded");
            }

            System.out.println("Make sure the search path is correct: ");
            System.out.println(System.getProperty("java.library.path"));
        }
        vtkNativeLibrary.DisableOutputWindow(null);
    }

    public vtkTest() {
        // Create a sphere source
        vtkSphereSource sphere = new vtkSphereSource();
        sphere.SetRadius(10.0);

        // Create a sphere mapper
        vtkPolyDataMapper sphereMapper = new vtkPolyDataMapper();
        sphereMapper.SetInputConnection(sphere.GetOutputPort());

        //create sphere actor
        vtkActor sphereActor = new vtkActor();
        sphereActor.SetMapper(sphereMapper);

        // Create a render window panel to display the sphere
        vtkRenderWindowPanel renderWindowPanel = new vtkRenderWindowPanel();
        renderWindowPanel.setPreferredSize(new Dimension(600, 600));
        renderWindowPanel.setInteractorStyle(new vtkInteractorStyleTrackballCamera());

        add(renderWindowPanel, BorderLayout.CENTER);

        renderWindowPanel.GetRenderer().AddActor(sphereActor);
    }

    public static void main(String[] args) {
        try {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
                    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

                    JFrame frame = new vtkTest();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}