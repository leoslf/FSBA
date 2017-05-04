package FSBA;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Macros or pseudo-global variables and functions with logging 
 * @author leosin
 *
 */
class Macro {
	/** Application Name */
	static final String APPNAME = "FSBA";
	/** DEBUG Status: Activated or not */
	private static boolean DEBUG = true;
	/** File instance to store the previous using file to reduce procedures for logging */
	private static File lastLogFile = null;
	/** Index for previous log file */
	private static int lastLogFileIndex = 0;
	
	/**
	 * Log String to both streams: System.err and log file
	 * @param str String to be logged
	 */
	static void logStr(String str){
		str = timestamp() +" " + str;
		logToFile(str);
		System.err.println(str);
	}
	
	/**
	 * Log String to log file
	 * @param str String to be logged to file
	 */
	private static void logToFile(String str){
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream(getLastLogFile(), true));
			if(ps != null) {
				ps.println(str);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} catch (NullPointerException e ) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Return instance of log file to be used
	 * File instance would be created if file does not exist or file used is too large.
	 * Too large for log file is defined as greater than or equal to 100MB
	 * @return File instance of the most recent log file
	 */
	private static File getLastLogFile() {
		File fp = lastLogFile;
		int i = lastLogFileIndex;
		
		File logDir = new File("log");
		if(!logDir.exists()) {
			try {
				logDir.mkdir();
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
			fp = new File("log/FSBA" + (i > 0 ? "_" + i : "") +".log");
			lastLogFileIndex = i;
			lastLogFile = fp;
			++i;
		}
		return fp;
	}
	
	/**
	 * Return a String of debug information by stack-trace.
	 * Debug information is defined as class-name, line-number and method-name of macro caller outside class FSBA.Macro, i.e. this class.
	 * @return debug information of macro caller
	 */
	private static String info() {
		StackTraceElement[] obj = Thread.currentThread().getStackTrace();
		int index = 2;
		while(obj[index].getClassName()==obj[1].getClassName()) {
			++index;
		}
		return "("+obj[index].getClassName()+":"+obj[index].getLineNumber()+":"+obj[index].getMethodName()+"): ";
	}
	
	/**
	 * Return current timestamp formatted as ISO 8601 standard date
	 * @param argv va_list of boolean
	 * @return String of current timestamp
	 */
	static String timestamp(boolean ...argv) {
		boolean time = true;
		if (argv != null && argv.length > 0) {
			time = !argv[0];
		}
		return new SimpleDateFormat("yyyy-MM-dd" + (time ? " HH:mm:ss" : "")).format(new Date());
	}
	
	static class dateMode {
		public static final Object[] d =  { Calendar.DAY_OF_MONTH, "dd"};
		public static final Object[] m =  { Calendar.MONTH,"MM" };
		public static final Object[] y =  { Calendar.YEAR, "YYYY" };
	}
	
	static String datePart(Object[] mode, Integer ... argv) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		if(check_argv(argv,1)) {
			c.add((int)mode[0], argv[0]);
		}
		return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
	}
	
	/**
	 * Log debug string with "[Debug]" prefixed with timestamp
	 * @param str Debug String to be logged
	 */
	static void debug(String str) {
		if(DEBUG) {
			logStr("[Debug]"+ info() + str);
		}
	}
	
	/**
	 * Log error with "[ERROR]" prefixed with timestamp
	 * @param str Error to be logged
	 */
	static void logErr(String str) {
		logStr("[ERROR]"+ info() + str);
	}
	
	/**
	 * Log warning with "[Warn]" prefixed with timestamp
	 * @param str Warning to be logged 
	 */
	static void logWarn(String str) {
		logStr("[Warn]"+ info() + str);
	}
	
	/**
	 * Log information with "[Info]" prefixed with timestamp
	 * @param str Info to be logged
	 */
	static void logInfo(String str) {
		logStr("[Info]"+ info() + str);
	}
	
	/**
	 * Check the condition, if true do nothing, else log error 
	 * @param state condition to be checked
	 */
	static void check(boolean state) {
		if(!state) {
			logErr("state == NULL");
		}
	}
	
	/**
	 * Log exception 
	 * @param e Exception to be logged
	 */
	static void logException(Exception e) {
		String linefeed = (e.getMessage()!= null && e.getMessage().charAt(0) == '[' )? "\n" : "";
		logErr("Exception: "+ linefeed + e.getMessage());
		for (StackTraceElement st : e.getStackTrace()) {
			logStr("\t"+"Class: " + st.getClassName() + " Method : " 
					+  st.getMethodName() + " line : " + st.getLineNumber());
		}
	}
	
	/**
	 * checker for variable argument lists
	 * @param argv argument vector
	 * @param length expected length
	 * @return whether it is valid
	 */
	static boolean check_argv(Object[] argv, int length) {
		boolean retVal = true;
		retVal &= (argv.length > 0 && argv.length <= length);
		for(int i = 0; i < Math.min(length,argv.length); ++i) {
			retVal &= (argv[i] != null);
			if(argv[i] instanceof String) {
				retVal &= !((String)argv[i]).isEmpty();
			}
		}
		return retVal;
	}
}
