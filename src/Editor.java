import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

public class Editor implements ActionListener {
	private JFrame frame;
	private JTextArea text;
	
	private final JFileChooser chooser = new JFileChooser();
	private static final String renderer = "bin/ReiTrei"; 
	private String filename, path;

	private void addMenuItem(JMenu menu, String name) {
		JMenuItem item = new JMenuItem(name);
		item.addActionListener(this);
		menu.add(item);
	}
	
	private void addMenuItem(JMenu menu, String name, ActionListener listener) {
		JMenuItem item = new JMenuItem(name);
		item.addActionListener(listener);
		menu.add(item);
	}
	
	private void resetTitle() {
		if (filename == null) frame.setTitle("Editor");
		else frame.setTitle("Editor: " + filename);
	}
	
	private void save(File file) {
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(text.getText());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void load(File file) {
		try {
			String allText = "";
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext())
				allText = allText + scanner.nextLine() + "\n";
			scanner.close();
			text.setText(allText);
		} catch (FileNotFoundException e) {
			return;
		}
	}
	
	
	public void newFile() {
		filename = path = null;
		resetTitle();
		text.setText(null);
	}
	
	public void openFile() {
		int returnVal = chooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            filename = file.getName();
            path = file.getAbsolutePath();
            resetTitle();
            load(file);
        } else {
        	System.out.println("Open command cancelled by user.");
        }
	}
	
	public void saveFile() {
		if (path == null) {
			saveAsFile();
			return;
		}
		
		File file = new File(path);
		save(file);
	}
	
	public void saveAsFile() {
		int returnVal = chooser.showSaveDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file.exists()) return; //uf confirm existing files
            filename = file.getName();
            path = file.getAbsolutePath();
            resetTitle();
            save(file);
        } 
		else {
        	System.out.println("Save as command cancelled by user.");
        }
	}
	
	public void closeFile() {
		//uf yes/no dialog
		newFile();
	}
	
	public void render(String options) {
		saveFile();
		try {
			Runtime rt = Runtime.getRuntime();
			rt.exec(renderer + " " + path + " " + options) ;
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public void previewFile() {
		render("--no-output --size 256 256");
	}
	
	public void renderFile() {
		render("--size 800 600");
	}
	
	public Editor() {
		frame = new JFrame("Editor");// The string is the title of the frame
		resetTitle();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// Otherwise it will only hide

		chooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				
				String ext = f.getName();
				int i = ext.lastIndexOf('.');				
				if (i > 0 &&  i < ext.length() - 1) ext = ext.substring(i+1).toLowerCase();
				else return false;
				
				return ext.equals("ray");
			}

			public String getDescription() {return null;}			
		});
		
		JMenuBar bar = new JMenuBar();

		JMenu menu = new JMenu("File");
		addMenuItem(menu, "New", new ActionListener() {
			public void actionPerformed(ActionEvent e) {newFile();}			
		});
		addMenuItem(menu, "Open...", new ActionListener() {
			public void actionPerformed(ActionEvent e) {openFile();}
		});
		addMenuItem(menu, "Save", new ActionListener() {
			public void actionPerformed(ActionEvent e) {saveFile();}
		});		
		addMenuItem(menu, "Save As...", new ActionListener() {
			public void actionPerformed(ActionEvent e) {saveAsFile();}
		});
		addMenuItem(menu, "Revert", new ActionListener() {
			public void actionPerformed(ActionEvent e) {if (filename != null) load(new File(path));}//uf
		});
		addMenuItem(menu, "Close", new ActionListener() {
			public void actionPerformed(ActionEvent e) {closeFile();}//uf
		});
		addMenuItem(menu, "Exit");
		bar.add(menu);

		menu = new JMenu("Edit");
		addMenuItem(menu, "Undo");
		addMenuItem(menu, "Redo");
		addMenuItem(menu, "Cut");
		addMenuItem(menu, "Copy");
		addMenuItem(menu, "Paste");
		addMenuItem(menu, "Preferences");
		bar.add(menu);

		menu = new JMenu("Render");
		addMenuItem(menu, "Preview", new ActionListener() {
			public void actionPerformed(ActionEvent e) {previewFile();}
		});
		addMenuItem(menu, "Render", new ActionListener() {
			public void actionPerformed(ActionEvent e) {renderFile();}
		});
		addMenuItem(menu, "Render Settings");
		bar.add(menu);

		menu = new JMenu("Help");
		addMenuItem(menu, "About");
		addMenuItem(menu, "Online Documentation");
		bar.add(menu);

		frame.setJMenuBar(bar);

		text = new JTextArea();
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		JScrollPane areaScrollPane = new JScrollPane(text);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(640, 480));
		
		frame.getContentPane().add(areaScrollPane);
		
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		System.out.println(event.getActionCommand());
	}
	
	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Editor();
            }
        });
    }
};