import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileAdapter {
	private File file;
	private boolean is_dirty;
	
	public FileAdapter() {file = null; is_dirty = false;}
	
	public void release() {file = null; is_dirty = false;}
	
	public void bind(File file) {this.file = file; is_dirty = true;}
	
	public boolean bound() {return (file != null);}
	
	public String getName() {return file.getName();}
	
	public String getPath() {return file.getPath();}
	
	public String load() throws FileNotFoundException {
		String text = "";
		Scanner scanner = new Scanner(file);
		while (scanner.hasNext())
			text = text + scanner.nextLine() + "\n";
		scanner.close();
		is_dirty = false;
		return text;
	}
	
	public void save(String text) throws IOException {
		FileWriter fw = new FileWriter(file);
		fw.write(text);
		fw.close();
		is_dirty = false;
	}
	
	public void markDirty() {is_dirty = true;}
	
	public void clearDirty() {is_dirty = false;}
	
	public boolean dirty() {return is_dirty;}
}
