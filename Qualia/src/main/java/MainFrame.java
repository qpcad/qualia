import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -1245702017236285965L;

	private JTable table;

	public MainFrame() {
		getContentPane().setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);

		JButton toolbarBtnImport = new JButton("Import");
		toolbarBtnImport.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.showSaveDialog(MainFrame.this);
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					System.out.println("getCurrentDirectory(): "
							+ fileChooser.getCurrentDirectory());
					System.out.println("getSelectedFile() : "
							+ fileChooser.getSelectedFile());
				} else {
					System.out.println("No Selection ");
				}

			}
		});
		toolBar.add(toolbarBtnImport);

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

		table = new JTable();
		table.setModel(new DefaultTableModel(new Object[][] { { null, null,
				null, null, null, null, null, null, null, null, null }, },
				new String[] { "Patient's Name", "Patient ID ",
						"Patient's Birth Date", "Patient's Sex",
						"Accession Number", "Modality", "Study ID",
						"Acquisition Date", "Content Date", "Institution Name",
						"Referring Physician's Name" }));
		table.getColumnModel().getColumn(0).setPreferredWidth(86);
		table.getColumnModel().getColumn(1).setPreferredWidth(62);
		table.getColumnModel().getColumn(2).setPreferredWidth(110);
		table.getColumnModel().getColumn(4).setPreferredWidth(111);
		getContentPane().add(table, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.SOUTH);

		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);

		JPanel panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
	}

	public static void main(String[] args) throws Exception {
		JFrame frame = new MainFrame();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

}
