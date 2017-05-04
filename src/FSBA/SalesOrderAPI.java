package FSBA;
import static FSBA.Macro.*;
import FSBA.DBC;
import FSBA.Macro.dateMode;

import java.sql.*;
import java.util.Vector;

/**
 * API for Sales Order Management
 * @author leosin
 *
 */
public class SalesOrderAPI {
	/** Database connection instance */
	private DBC dbc;
	/** mode enumerations for Query access */
	enum Accesses {
		order,
		lineItems,
		notes,
		orderStatus,
		auditTrail,
		apptTimeID,
		location,
		datetime,
		BIdump,
		closing
	}

	/** mode enumerations for closing */
	enum ClosingMode {
		daily,
		monthly,
		annual,
		others
	}
	
	
	/**
	 * Default constructor
	 */
	public SalesOrderAPI() {
		dbc = new DBC();
	}
	
	/**
	 * initialize with database information
	 * @param argv argument handler
 	 */
	public SalesOrderAPI(String ... argv) {
		dbc = new DBC();
	}
	
	/**
	 * initialize with database connection setup
	 * @param dbc database connection to be move" 
	 */
	public SalesOrderAPI(DBC dbc) {
		this.dbc = dbc;
	}
	
	static final String[][][] notes = 
		{
				{
					{ "sequenceNumber", "Sequence No.", },
					{ "standardCode_id", "Standardized Code", },
					{ "remarks","Remarks", },
				},
				{
					{ "notes", "n", },
				},
				{}
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
			{}
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
					"t.id", "q.timeslot_id" },
			}
		};
	
	static final String[][][] orderList = 
		{
			{				
				{ "substr('00000'|| s.id ,-5,5)", "Order No.", },
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
	static final String[][][] auditTrail = {
			{
				{ "a.timestamp ", "Timestamp", },
				{ "(s.firstName || \" \" || s.lastName) ", "Staff", },
				{ "a.oldValue ", "Old value", },
				{ "a.newValue ", "New value", },
			},
			{
				{ "auditTrail", "a", },
			},
			{
				{ "staff ", "s", 
					"s.id", "a.staff_id", },
			}
	};
	static final String[][][] BIdump = {
			{
				{"l.id",  },
				{"l.sequenceNumber",  },
				{"l.product_id",  },
				{"l.qty",  },
				{"l.product",  },
				{"l.price",  },
				{"l.sales_id",  },
				{"s.printStatus_id",  },
				{"s.status_id",  },
				{"s.customer",  },
				{"s.phone",  },
				{"s.deliveryLocation_id",  },
				{"s.date",  },
				{"s.date_created",  },
				{"s.last_updated",  },
				{"s.timeslot_id",  },
			},
			{
				{ "lineItemSets",  "l", },
			},
			{
				{ "sales ",  "s", 
					"l.sales_id",  "s.id",  },
			}
	};
	static final String[][][] closing = {
			{
				{"s.date", "Date" },
				{"l.sales_id", "Order No." },
				{"l.sequenceNumber", "Seq. No."  },
				{"l.product_id", "Product id" },
				{"l.product", "Product" },
				{"l.qty", "Qty" },
				{"l.price", "Price($)" },
				{"d.location", "Location" },
				{"t.timeslot", "Timeslot" },
			},
			{
				{ "lineItemSets",  "l", },
			},
			{
				{ "sales ",  "s", 
					"l.sales_id",  "s.id",  },
				{ "deliveryLocations", "d",
					"d.id", "s.deliveryLocation_id" },
				{ "timeslots", "t",
					"t.id", "s.timeslot_id" }
			}
	};
	
	/**
	 * Helper function for compose or formatting SQL statement from 3-dimensional String array
	 * <p>
	 * Return format: 	{
	 * 		"column1 [AS \"alias\"], ..., columnN [AS \"alias\"]",
	 * 		"table_name [AS \"alias\"]",
	 * 		"[LEFT JOIN table2 AS \"alias\"] ..."
	 * }
	 * </p>
	 * @param arr 3D String array
	 * @return one-dimensional array of String: { columns, table, conditions (e.g. JOIN ) }
	 */
	private String[] compose(String[][][] arr) {
		String column = "";
		String table = "";
		String conditionPresets = "";
		
		// compose columns
		for(int i = 0; i < arr[0].length; ++i) {
			column += arr[0][i][0] + (arr[0][i].length == 2 ? " AS \"" + arr[0][i][1] + "\"" : "");
			column += (i == arr[0].length - 1)? " " : ", ";
		}
		
		// compose FROM ...
		table = arr[1][0][0] + (arr[1][0].length == 2 ? " AS \"" + arr[1][0][1] + "\"" : "");
		
		// compose conditions
		for(String[] subArr: arr[2]) {
			conditionPresets += "LEFT JOIN " + subArr[0] + " AS " + subArr[1] + " ";
			conditionPresets += "ON " + subArr[2] + " = " + subArr[3] + " ";
		}
		String[] retVal = {column, table, conditionPresets};
		
		return retVal;
	}
	
	/**
	 * ** 
	 * Parameterized Query/Search
	 * @param mode see Accesses modes
	 * @param conditions to be searched
	 * @return the result set of query
	 */
	public ResultSet query(Accesses mode, String ...conditions) {
		String conditionformatted =  (conditions != null && conditions.length > 0 && conditions[0].length() > 0? " WHERE "+conditions[0] : "");
		String[] components = null;
		switch(mode) {
			case order:			components = compose(orderList);	break;
			case lineItems:		components = compose(lineItemSets);	break;
			case notes:			components = compose(notes);		break;
			case orderStatus:	
				return dbc.select("status", "orderStatuses", conditionformatted);
				
			case auditTrail:	components = compose(auditTrail);	break;
				
			case apptTimeID:
				return dbc.select("id",	"timeslots", conditionformatted);
				
			case location:
				return dbc.select("location", "deliveryLocations", conditionformatted);
				
			case datetime:		components = compose(dateTime);		break;
			case BIdump:		components = compose(BIdump);		break;
			case closing: 		components = compose(closing);		break;
				
			default:
				logErr("Invalid Enumeration: Accesses");
				return null;
		}
		return dbc.select(components[0], components[1], components[2] + conditionformatted);
	}
	
	/**
	 * Query key / ID of given value of given table
	 * @param mode mode to use
	 * @param timeslot timeslot value
	 * @return the required id 
	 */
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
	
	
	/**
	 * export data for business intelligence analysis 
	 */
	public void BIDataDump() {
		ResultSetUtil rsUtil = new ResultSetUtil(query(Accesses.BIdump));
		String[] colNames = rsUtil.getStrArrColumnNames();
		String[][] rows = rsUtil.getStrArrRows();
		dataWriter dw = new dataWriter(new String[] {"BI","data","csv"});
		
		String str = String.join(",", colNames) + "\r\n";
		for(int i = 0; i < rows.length; ++i) {
			str += String.join(",", rows[i]) + "\r\n";
		}
		dw.writeToFile(str);
		
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
		int[][] ids = {new int[]{orderNumber}, new int[lineItems.getValuesArrLength()],  new int[notes.getValuesArrLength()]};
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
	 * @param table
	 * @param orderNo
	 * @param row Row id in table
	 * @param column displayed column
	 * @param newValue new value of the element
	 */
	public void update(String table, int orderNo, int row, int column, Object newValue) {
		String[][][] schema = null;
		if(table=="lineItemSets") {
			schema = lineItemSets;
		} else if(table=="notes") {
			schema = notes;
		} else {
			logWarn("invalid table");
			return;
		}
		dbc.update(table, schema[0][column][0] + "="+newValue.toString(), "sales_id="+orderNo + " AND sequenceNumber="+(row+1));
	}
	
	/**
	 * Update records in database
	 * @param table table name
	 * @param orderNo order number 
	 * @param colnVal formatted columns and values to be set in update statement
	 * @param newValue new value of the element
	 */
	public void update(String table, int orderNo, String colnVal, Object newValue) {
		dbc.update(table, colnVal, "id="+orderNo);
	}
	
	/**
	 * Commit change
	 */
	public void commit() {
		dbc.commitUpdate();
	}
	
	/**
	 * Perform time-period specified retail closing
	 * Assume Financial year starts from 1st January
	 * Output closing document file to filesystem
	 * @param mode enumeration of time-period as defined
	 */
	public void closing(ClosingMode mode) {
		
		int modeIndex = mode.ordinal();
		dataWriter dw = new dataWriter(new String[] {"closing", ClosingMode.values()[modeIndex].toString(), "txt"}, false, false);
		String[][] closingConditions = {
				{ "'now'","'now'" },
				{ datePart(dateMode.d, -1), datePart(dateMode.d)},
				{ datePart(dateMode.m, -1), datePart(dateMode.m)},
				{ datePart(dateMode.y, -1), datePart(dateMode.y)}
		};
		String condition = "s.date BETWEEN \"" + closingConditions[modeIndex][0] + "\" AND \"" + closingConditions[modeIndex][1] + "\"";
		
		ResultSetUtil rsutil = new ResultSetUtil(query(Accesses.closing, condition));
		
		Vector<String> columnNames = rsutil.getVectorColumnNames();
		Vector<Vector<String>> rows = rsutil.getVectorRows();
		
		
		
		rows.add(0,columnNames);
		int[] maxW = TableBuilder.maxWidths(rows);
		rows.remove(0);
		
		Vector<String> hyphen = new Vector<String>();
		for(int i = 0; i < maxW.length; ++i) {
			hyphen.add(new String(new char[maxW[i]]).replace("\0", "-"));
		}
		String[] format = { TableBuilder.format(maxW, "+"), TableBuilder.format(maxW, "|")};
		format[0] = String.format(format[0], hyphen.toArray());
		String column = String.format(format[1], columnNames.toArray());
		
		final Vector<String> dataRows = new Vector<String>();
		rows.forEach((Vector<String> row) -> dataRows.add(String.format(format[1], row.toArray())));
		
		dw.writeToFile(format[0]);
		dw.writeToFile(column);
		dw.writeToFile(format[0]);
		dataRows.forEach((String row) -> dw.writeToFile(row));
		dw.writeToFile(format[0]);

	}
	
	static class TableBuilder {
		static String format(int[] len, String c) {
			final StringBuilder formatting = new StringBuilder(c);
			for(int i : len) {
				formatting.append(" %-" + i + "s "+ c);
			}
			
			return formatting.toString();
		}
		
		static int[][] lengthArr(Vector<Vector<String>> rows) {
			int[][] temp = new int[rows.size()][rows.get(0).size()];
			for(int i = 0; i < rows.size(); ++i) {
				for(int j = 0; j < rows.get(0).size(); ++j) {
					temp[i][j] = rows.get(i).get(j).length();
				}
			}
			return temp;
		}
		static int[][] transpose(Vector<Vector<String>> rows) {
			int[][] r = lengthArr(rows);
			int[][] temp = new int[r[0].length][r.length];
			for(int i = 0; i < r[0].length; ++i) {
				for(int j = 0; j < r.length; ++j) {
					temp[i][j] = r[j][i];
				}
			}
			return temp;
		}

		static int[] maxWidths(Vector<Vector<String>> rows) {
			int[][] r = transpose(rows);
			int[] retVal = new int[r.length];
			for(int i = 0; i < r.length; ++i) {
				int max = 0;
				for(int elem : r[i]) {
					if(max < elem) {
						max = elem;
					}
				}
				retVal[i] = max;
			}
			
			return retVal;
		}
	}
}
