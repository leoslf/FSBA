package FSBA;

import static FSBA.Macro.*;
import java.io.*;

/**
 * Utilities for "resources"
 * @author leosin
 *
 */
class ResourceUtil {
	/**
	 * Clone resources file to filesystem
	 * @param filename in resources
	 * @param outfilename destination
	 */
	ResourceUtil(String filename, String outfilename) {	
		String str = new File(".").getAbsolutePath()+"/"+outfilename;
		logInfo(str);
		File file = new File(str);
		InputStream inputStream = this.getClass().getResourceAsStream("/resources/"+filename);
		OutputStream outputStream = null;
		
		try {
			// write the inputStream to a FileOutputStream
			outputStream = new FileOutputStream(file);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			logInfo("\""+filename + "\" cloned as \""+ outfilename +"\" successfully");
		} catch (IOException e) {
			logException(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logException(e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					logException(e);
				}
			}
		}

	}
}
