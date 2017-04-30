package FSBA;
import static FSBA.Macro.*;
import FSBA.DBC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.*;
import java.util.*;

enum Accesses {
	order,
	lineItems,
	notes,
	orderStatus,
	printStatus,
	auditTrail,
	apptTimeID,
	location,
	datetime,
	BIdump,
}

enum ClosingMode {
	daily,
	monthly,
	annual
}

public class SalesOrderAPI {
	/** Database connection instance */
	private DBC dbc;
	
	/**
	 * Default constructor
	 */
	public SalesOrderAPI() {
		dbc = new DBC();
	}
	
	/**
	 * initialize with database information
	 * @param argv
	 */
	public SalesOrderAPI(String ... argv) {
		//TODO: handle argv
		dbc = new DBC();
	}
	
	/**
	 * initialize with database connection setup
	 * @param dbc
	 */
	public SalesOrderAPI(DBC dbc) {
		this.dbc = dbc;
	}
	
	
	static final String[][][] notes = 
		{
				{
					{ "standardCode_id", "Standardized Code", },
					{ "remarks","Remarks", },
				},
				{
					{ "notes", "n", },
				},
				{
					
				}
		};
	
	static final String[][][] lineItemSets = 
		{
			{
				{ "sequenceNumber", "Sequence No.", },
				{ "product_id ", "Product Code",},
				{ "qty", "Qty", },
				{ "product", "Product", },
				{ "price","Price($)", }
			},
			{
				{ "lineItemSets", "l",}
			},
			{

			}
		};
	
	static final String[][][] dateTime = 
		{
			{
				{ "q.date", "Date" },
				{ "t.timeslot", "Time slot" },
				{ "q.qty", "Quota" },
			},
			{
				{ "quota", "q" },
			},
			{
				{ "timeslots", "t",
					"t.id", "q.id" },
			}
		};
	
	static final String[][][] orderList = 
		{
			{				
				{ "s.id", "Order No.", },
				{ "o.status", "Status",},
				{ "s.customer", "Customer", },
				{ "s.phone", "Phone", },
				{ "d.location", "Location", },
				{ "s.date", "Date", },
				{ "t.timeslot", "Timeslot", },
				{ "s.date_created","Created time", },
				{ "s.last_updated","Last update", },
			},
			{
				{ "sales", "s",}
			},
			{
				{"orderStatuses","o",
				 "o.id","s.status_id "},
				{ "deliveryLocations","d",
				  "d.id", "s.deliveryLocation_id" },
				{ "timeslots","t",
				  "t.id","s.timeslot_id" },
			}
		};
	
	private String[] compose(String[][][] arr) {
		String column = "";
		String table = "";
		String conditionPresets = "";
		for(int i = 0; i < arr[0].length; ++i) {
			column += arr[0][i][0] + " AS \"" + arr[0][i][1] + "\"";
			column += (i == arr[0].length - 1)? " " : ", ";
		}
		
		table = arr[1][0][0] + " AS \"" + arr[1][0][1] + "\" ";
		
		for(String[] subArr: arr[2]) {
			conditionPresets += "LEFT JOIN " + subArr[0] + " AS " + subArr[1] + " ";
			conditionPresets += "ON " + subArr[2] + " = " + subArr[3] + " ";
		}
		String[] retVal = {column, table, conditionPresets};
		/*for(String elem: retVal) {
			logInfo(elem);
		}*/
		return retVal;
	}
	
	/** 
	 * Parameterized Query/Search
	 * @param conditions to be searched
	 * @return the result set of query
	 */
	public ResultSet query(Accesses mode, String ...conditions) {
		String conditionformatted =  (conditions != null && conditions.length > 0 && conditions[0].length() > 0? " WHERE "+conditions[0] : "");
		String[] components = null;
		switch(mode) {
			case order:
				components = compose(orderList);
				break;
			case lineItems:
				components = compose(lineItemSets);
				break;
			case notes:
				components = compose(notes);
				break;
			case orderStatus:
				return dbc.select("status", "orderStatuses", conditionformatted);
			case printStatus:
				return dbc.select("status",	"printStatuses" + conditionformatted);
			case auditTrail:
				return dbc.select(""
						+ "a.timestamp AS \"Timestamp\", "
					   + "(s.firstName || \" \" || s.lastName) AS \"Staff\", "
						+ "a.oldValue AS \"Old value\", "
						+ "a.newValue AS \"New value\" ", 
					"auditTrail AS a",
					"LEFT JOIN staff AS s " //CONDITIONS
						+ "ON s.id = a.staff_id"
					+ conditionformatted
							);
			case apptTimeID:
				return dbc.select("id",	"timeslots", conditionformatted);
			case location:
				return dbc.select("location", "deliveryLocations", conditionformatted);
			case datetime:
				components = compose(dateTime);
				break;
			case BIdump:
				return dbc.select(""
						+ "l.id,"
						+ "l.sequenceNumber,"
						+ "l.product_id,"
						+ "l.qty,"
						+ "l.product,"
						+ "l.price,"
						+ "l.sales_id,"
						+ "s.printStatus_id,"
						+ "s.status_id,"
						+ "s.customer,"
						+ "s.phone,"
						+ "s.deliveryLocation_id,"
						+ "s.date,"
						+ "s.date_created,"
						+ "s.last_updated,"
						+ "s.timeslot_id,"
						+ "s.price",
						"lineItemSets AS l", 
						"LEFT JOIN sales AS s ON l.sales_id = s.id");
			default:
				logErr("Invalid Enumeration: Accesses");
				return null;
		}
		return dbc.select(components[0], components[1], components[2] + conditionformatted);
	}
	
	public int queryID(String mode, String timeslot) {
		ResultSet rs = query(Accesses.apptTimeID, mode + "=\"" + timeslot+"\"");
		int retVal = -1;
		try {
			while(rs.next()) {
				retVal = rs.getInt(1);
			}
		} catch (SQLException e) {
			logException(e);
		}
		return retVal;
	}
	
	class ResultSetProcessing {
		private ResultSet resultSet;
		private ResultSetMetaData metaData;
		private int columnCount = -1;
		private Vector<String> columnNames = new Vector<String>();
		private Vector<Vector<String>> rows = new Vector<Vector<String>>();
		public ResultSetProcessing(ResultSet rs) {
			resultSet = rs;
			getHeader();
			getRows();
		}
		/**
		 * Obtain header of the resultSet
		 */
		void getHeader() {
			try {
				metaData = resultSet.getMetaData();
				columnCount = metaData.getColumnCount();
				for(int i = 1; i <= columnCount; ++i) {
					// debug(metaData.getColumnName(i));
					columnNames.add(metaData.getColumnName(i));
				}
			} catch (SQLException e) {
				logException(e);
			}
		}
		
		/**
		 * Obtain Rows from resultSet
		 * @throws SQLException handled in sub routine
		 */
		void getRows() {
			try {
			while(resultSet.next()) {
					getNextRow();
			}
			} catch (SQLException e) {
				logException(e);
			}
		}
		
		/**
		 * Helper function to obtain nextRows from resultSet
		 */
		void getNextRow() {
			Vector<String> row = new Vector<String>();
			try {
				for(int i = 1; i <= columnCount; ++i) {
					//debug(""+resultSet.getObject(i));
					String str = resultSet.getString(i);
					row.add(str);
				}
				rows.add(row);
			} catch (SQLException e) {
				logException(e);
			}
		}
		
		public String[][] getStrArrRows() {
			String[][] ret = new String[rows.size()][columnNames.size()];
			for(int i = 0; i < rows.size(); ++i) {
				Vector<String> cols = rows.get(i);
				for(int j = 0; j < cols.size(); ++j) {
					ret[i][j] = cols.get(j);
				}
			}
			return ret;
		}
		
		public String[] getStrArrColumnNames() {
			return columnNames.toArray(new String[columnNames.size()]);
		}
	}
	
	public void BIDataDump() {
		ResultSetProcessing rsp = new ResultSetProcessing(query(Accesses.BIdump));
		String[] colNames = rsp.getStrArrColumnNames();
		String[][] rows = rsp.getStrArrRows();
		dataWriter dw = new dataWriter();
		
		String str = String.join(",", colNames) + "\r\n";
		for(int i = 0; i < rows.length; ++i) {
			str += String.join(",", rows[i]) + "\r\n";
		}
		dw.writeToFile(str);
		
	}
	class dataWriter {
		private File lastFile = null;
		/** Index for previous file */
		private int lastFileIndex = 0;
		private PrintStream ps = null;
		dataWriter() {
			
		}
		
		/**
		 * Log String to log file
		 * @param str String to be logged to file
		 */
		public void writeToFile(String str){
			
			try {
				if(ps == null) {
					ps = new PrintStream(new FileOutputStream(getLastFile(),false));
				}
				if(ps != null) {
					ps.println(str);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace(System.err);
			} catch (NullPointerException e ) {
				e.printStackTrace(System.err);
			}
		}
		
		private File getLastFile() {
			File fp = lastFile;
			int i = lastFileIndex;
			
			File logDir = new File("BI");
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
				fp = new File("BI/data" + (i > 0 ? "_" + i : "") +".csv");
				lastFileIndex = i;
				lastFile = fp;
				++i;
			}
			return fp;
		}
	}
	
	
	/**
	 * Insert new order into database with order fields
	 * @param order Order details
	 * @param lineItems Array of OrderComponent: line items rows
	 * @param notes Array of OrderComponent: notes 
	 * @return The 2D array of primary keys for the three order components
	 */
	public int[][] insertOrder(OrderComponent order, OrderComponent lineItems, OrderComponent notes) {
		int orderNumber = dbc.insert("sales", order.getColumns(), order.getValues());
		int[][] ids = {new int[1], new int[lineItems.getValuesArrLength()],  new int[notes.getValuesArrLength()]};
		OrderComponent[] components = {lineItems, notes};
		String[] tableName = { "lineItemSets", "notes" };
		for(int comp = 0; comp < components.length; ++comp) {
			logInfo("ids[1+comp]:"+ids[1+comp].length);
			for(int i = 1; i <= ids[1+comp].length; ++i) {
				ids[1+comp][i-1] = dbc.insert(tableName[comp], "sales_id, " + components[comp].getColumns(), orderNumber + ", " + components[comp].getValuesArr(i-1));
			}
		}
		return ids;
	}
	
	/**
	 * Update records in database
	 * @param id Row id in table
	 * @param column displayed column
	 * @param newValue new value of the element
	 * @return
	 */
	public boolean update(int id, int column, Object newValue) {
		//TODO: lookup which table is column from
		return dbc.update();
	}
	
	/**
	 * Perform time-period specified retail closing
	 * Output closing document file to filesystem
	 * @param mode enumeration of time-period as defined
	 */
	public void closing(ClosingMode mode) {
		// TODO: create closing document for specified period from mode

		switch(mode) {
		case daily: 
			break;
		case monthly: 
			break;
		case annual: 
			break;
		}
		
	}
	
}
