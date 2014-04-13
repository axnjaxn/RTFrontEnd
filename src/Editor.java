import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

public class Editor implements ActionListener {
	private JFrame frame;
	private JTextArea options, text;
	
	private final JFileChooser chooser = new JFileChooser();
	private static final String renderer = "bin/ReiTrei"; 
	private FileAdapter scenefile;

	private void addMenuItem(JMenu menu, String name) {
		addMenuItem(menu, name, this);
	}
	
	private void addMenuItem(JMenu menu, String name, ActionListener listener) {
		addMenuItem(menu, name, listener, null);
	}
	
	private void addMenuItem(JMenu menu, String name, ActionListener listener, String accel) {
		JMenuItem item = new JMenuItem(name);
		if (listener != null) item.addActionListener(listener);
		else item.addActionListener(this);
		if (accel != null) item.setAccelerator(KeyStroke.getKeyStroke(accel));
		menu.add(item);
	}
	
	private void resetTitle() {
		String title = "Editor";
		if (scenefile.bound()) {
			title = title + ": " + scenefile.getName();
			if (scenefile.dirty()) title = title + "*";
		}			
		frame.setTitle(title);
	}
	
	private void markDirty() {
		if (!scenefile.dirty()) {
			scenefile.markDirty();
			resetTitle();
		}
	}
	
	private void save() {
		try {
			scenefile.save(text.getText());
			scenefile.clearDirty();
			resetTitle();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void load() {
		try {
			text.setText(scenefile.load());
			scenefile.clearDirty();
			resetTitle();
		} catch (FileNotFoundException e) {
			return;
		}
	}
	
	
	public void newFile() {
		scenefile.release();
		resetTitle();
		text.setText(null);
	}
	
	public void openFile() {
		int returnVal = chooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            scenefile.bind(chooser.getSelectedFile());
            load();
        } else {
        	System.out.println("Open command cancelled by user.");
        }
	}
	
	public void saveFile() {
		if (scenefile.bound()) save();
		else saveAsFile();
	}
	
	public void saveAsFile() {
		int returnVal = chooser.showSaveDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            scenefile.bind(chooser.getSelectedFile());
            //if (scenefile.exists()) return; //uf confirm existing files
            save();
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
			rt.exec(renderer + " " + scenefile.getPath() + " " + options);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public void previewFile() {
		render("--no-output --size 256 256");
	}
	
	public void renderFile() {
		render(options.getText());
	}
	
	public Editor() {
		frame = new JFrame("Editor");// The string is the title of the frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// Otherwise it will only hide
		scenefile = new FileAdapter();
		resetTitle();

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
		JMenu menu;

		menu = new JMenu("File");
		addMenuItem(menu, "New", new ActionListener() {
			public void actionPerformed(ActionEvent e) {newFile();}			
		}, "control N");
		addMenuItem(menu, "Open...", new ActionListener() {
			public void actionPerformed(ActionEvent e) {openFile();}
		}, "control O");
		addMenuItem(menu, "Save", new ActionListener() {
			public void actionPerformed(ActionEvent e) {saveFile();}
		}, "control S");
		addMenuItem(menu, "Save As...", new ActionListener() {
			public void actionPerformed(ActionEvent e) {saveAsFile();}
		});
		addMenuItem(menu, "Revert", new ActionListener() {
			public void actionPerformed(ActionEvent e) {if (scenefile.bound()) load();}//uf
		});
		addMenuItem(menu, "Close", new ActionListener() {
			public void actionPerformed(ActionEvent e) {closeFile();}//uf
		}, "control W");
		addMenuItem(menu, "Exit", new ActionListener() {
			public void actionPerformed(ActionEvent e) {frame.dispose();}
		});
		bar.add(menu);

		menu = new JMenu("Edit");
		addMenuItem(menu, "Undo", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Undo not implemented.");
			}
		}, "control Z");
		addMenuItem(menu, "Redo", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Redo not implemented.");
			}
		}, "control Y");
		addMenuItem(menu, "Cut", new ActionListener() {
			public void actionPerformed(ActionEvent e) {text.cut();}			
		}, "control X");
		addMenuItem(menu, "Copy", new ActionListener() {
			public void actionPerformed(ActionEvent e) {text.copy();}			
		}, "control C");
		addMenuItem(menu, "Paste", new ActionListener() {
			public void actionPerformed(ActionEvent e) {text.paste();}
		}, "control V");
		addMenuItem(menu, "Preferences");
		bar.add(menu);

		menu = new JMenu("Render");
		addMenuItem(menu, "Preview", new ActionListener() {
			public void actionPerformed(ActionEvent e) {previewFile();}
		}, "F5");
		addMenuItem(menu, "Render", new ActionListener() {
			public void actionPerformed(ActionEvent e) {renderFile();}
		}, "F6");
		addMenuItem(menu, "Render Settings");
		bar.add(menu);

		menu = new JMenu("Help");
		addMenuItem(menu, "About");
		addMenuItem(menu, "Online Documentation", this, "F1");
		bar.add(menu);

		frame.setJMenuBar(bar);

		text = new JTextArea();
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		text.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) {markDirty();}
			public void insertUpdate(DocumentEvent arg0) {markDirty();}
			public void removeUpdate(DocumentEvent arg0) {markDirty();}
		});

		options = new JTextArea(1, 64);
		options.setText("--size 800 600");
			
		JScrollPane areaScrollPane = new JScrollPane(text);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(640, 480));
		
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		
		listPane.add(options);
		listPane.add(areaScrollPane);
		listPane.add(Box.createRigidArea(new Dimension(0,5)));
		
		frame.getContentPane().add(listPane);
		
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