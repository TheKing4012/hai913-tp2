package parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Parser<T> {
	/* ATTRIBUTES */
	protected String projectPath;
	protected String jrePath;
	protected T parser;
	
	/* CONSTRUCTOR */
	public Parser(String projectPath) {
		setProjectPath(projectPath);
		setJREPath(System.getProperty("java.home"));
		configure();
	}
	
	/* METHODS */
	public String getProjectPath() {
		return projectPath;
	}
	
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	public String getJREPath() {
		return jrePath;
	}
	
	public void setJREPath(String jrePath) {
		this.jrePath = jrePath;
	}
	
	public T getParser() {
		return parser;
	}
	
	public List<File> listJavaFiles(String filePath){
		File folder = new File(filePath);
		List<File> javaFiles = new ArrayList<>();
		String fileName = "";
		
		for (File file: folder.listFiles()) {
			fileName = file.getName();
			
			if (file.isDirectory())
				javaFiles.addAll(listJavaFiles(file.getAbsolutePath()));
			else if (fileName.endsWith(".java"))
				javaFiles.add(file);
		}
		
		return javaFiles;
	}

	public ArrayList<File> getJavaFilesForFolder(File  folder) {
		ArrayList<File> javaFiles = new ArrayList<File>();
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				javaFiles.addAll(getJavaFilesForFolder(fileEntry));
			} else if (fileEntry.getName().contains(".java")) {
				javaFiles.add(fileEntry);
			}
		}

		return javaFiles;
	}
	
	public List<File> listJavaProjectFiles(){
		File file = new File(getProjectPath());
		return getJavaFilesForFolder(file);
	}
	
	public abstract void configure();
}
