package FSBA;

import static FSBA.Macro.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Text writer to file
 * @author leosin
 *
 */
class dataWriter {
	/** instance of last file */
	private File lastFile = null;
	/** Index for previous file */
	private int lastFileIndex = 0;
	/** flag for appending */
	private boolean append;
	/** flag for by date */
	private boolean byDate;
	/** output directory name */
	private String dir;
	/** output filename prefix */
	private String filenamePrefix;
	/** output file extension */
	private String ext;
	/** PrintStream instance */
	private PrintStream ps = null;
	
	/**
	 * Constructor for dataWrter
	 * @param filenameConfig String array of format : { directory name, filename prefix, extension }
	 * @param argv variable argument list for flags: {[append,[by date]]} 
	 */
	dataWriter(String[] filenameConfig, boolean ... argv) {
		if(argv != null && argv.length > 0) {
			append = argv[0];
			if(argv.length > 1) {
				byDate = argv[1];
			}
			
		} else {
			append = false;
		}
		if(filenameConfig != null && check_argv(filenameConfig, 3)) {
			dir = filenameConfig[0];
			filenamePrefix = filenameConfig[1];
			ext = filenameConfig[2];
		}
	}
	
	/**
	 * Log String to log file
	 * @param str String to be logged to file
	 */
	public void writeToFile(String str){
		
		try {
			if(ps == null) {
				// create instance of print stream if ps is null
				ps = new PrintStream(new FileOutputStream(getLastFile(),append));
			}
			
			if(ps != null) {
				ps.println(str);
			} else {
				// log warning since ps still null
				logWarn("ps == null, cannot write to file");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (NullPointerException e ) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * get the last file to be used
	 * @return File the last file 
	 */
	private File getLastFile() {
		File fp = lastFile;
		int i = lastFileIndex;
		
		File logDir = new File(dir);
		if(!logDir.exists()) {
			try {
				// create directory if directory does not exist
				logDir.mkdir();
				// set permissions of directory
				logDir.setWritable(true, false);
			} catch (SecurityException e) {
				e.printStackTrace(System.err);
			}
		}
		
		while(i < 256) {
			if(fp != null) {
				if(fp.exists()) {
					if(fp.length() < 100L *(1 << 10)) {
						break;
					} else {
						++i;
						continue;
					}
				} else {
					try {
						fp.createNewFile();
						fp.setWritable(true, false);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
			fp = new File(dir + "/" + filenamePrefix + (i > 0 ? "_" + i : "") + (byDate ? timestamp(false) : "")+"."+ext);
			lastFileIndex = i;
			lastFile = fp;
			++i;
		}
		return fp;
	}
}
