package FSBA;
import java.io.File;
import java.sql.*;
import java.util.Vector;

import org.sqlite.*;
import static FSBA.Macro.*;

class DBC {
	/** Java Database Connection (JDBC) instance */
	private static Connection conn = null;
	/** Database type */
	private String dbType = "sqlite";
	/** Database file: for SQLite */
	private String dbFile = "all_proj.db";

	/**
	 * Create Java Database Connection to specific file of SQLite
	 * Add exit hook to close connection
	 */
	public DBC(String...argv) {
		if(argv!=null) {
			if(argv.length > 1) {
				dbType = argv[0];
				dbFile = argv[1];
			}
		}
		if(conn == null) {
			Runtime.getRuntime().addShutdownHook(new Thread() { public void run() { DBC.this.close(); } });
			//debug("DB Begin");
			try {
				//File f = new File(dbFile);
				//boolean create = !f.exists() || f.length() == 0;
				conn = DriverManager.getConnection("jdbc:" + dbType + ":" + dbFile);
				check(conn!=null);
				logInfo("DB Connected");
				/*if(create) {
					createSchema();
				}*/
			} catch (Exception e) {
				logException(e);
			} 
		}
		//debug("DB End");
	}
	
	/**
	 * Query given SQL statement to database
	 * @param sql SQL statement 
	 * @return Queried results
	 */
	private ResultSet query(String sql) {
		//debug("query Begin");
		ResultSet rs = null;
		Statement statement;
		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(sql);
		} catch (SQLException e) {
			logException(e);
		} 
		//debug("query End");
		return rs;
	}
	
	/**
	 * Parameterized query: SELECT
	 * @param columns Columns to be queried
	 * @param table Table reference
	 * @param conditions String array of conditions
	 * @return Queried result
	 */
	public ResultSet select(String columns, String table, String ...conditions) {
		return query("SELECT " + columns + " FROM " + table + " " + (conditions != null && conditions.length > 0 ? conditions[0] : ""));
	}
	
	public boolean update() {
		return false;
	}
	
	/*
	protected void createSchema() {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeQuery(""
					+"PRAGMA foreign_keys=OFF;"
					+"BEGIN TRANSACTION;"
					+"CREATE TABLE `location_timeslot_junction` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,"
					+"  `deliveryLocation_id` int(11) NOT NULL,"
					+"  `timeslot_id` int(11) NOT NULL,"
					+"  CONSTRAINT `location_timeslot_junction_ibfk_1` FOREIGN KEY (`deliveryLocation_id`) REFERENCES `deliveryLocations` (`id`),"
					+"  CONSTRAINT `location_timeslot_junction_ibfk_2` FOREIGN KEY (`timeslot_id`) REFERENCES `timeslots` (`id`)"
					+");"
					+"CREATE TABLE `note_standardCode` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,"
					+"  `remarks` varchar(32) NOT NULL"
					+");"
					+"CREATE TABLE `staff` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,"
					+"  `username` varchar(32) NOT NULL,"
					+"  `password` varchar(32) NOT NULL,"
					+"  `emailAddr` varchar(64) NOT NULL,"
					+"  `firstName` varchar(16) NOT NULL,"
					+"  `lastName` varchar(16) NOT NULL,"
					+"  `role` int(11) NOT NULL,"
					+"  `userStatus` int(11) NOT NULL,"
					+"  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+"  CONSTRAINT `staff_ibfk_1` FOREIGN KEY (`role`) REFERENCES `staff_roles` (`id`),"
					+"  CONSTRAINT `staff_ibfk_2` FOREIGN KEY (`userStatus`) REFERENCES `staff_status` (`id`)"
					+");"
					+"INSERT INTO `staff` VALUES(1,'admin','admin','leo69219326@gmail.com','System Administrator','',1,1,'2017-04-04 18:50:05');"
					+"INSERT INTO `staff` VALUES(2,'leosin','leosin','leo69219326@gmail.com','Leo','Sin',1,1,'2017-04-04 18:50:05');"
					+"CREATE TABLE `staff_roles` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,"
					+"  `roles` varchar(32) NOT NULL"
					+");"
					+"INSERT INTO `staff_roles` VALUES(1,'Administrator');"
					+"INSERT INTO `staff_roles` VALUES(2,'Supervisor');"
					+"INSERT INTO `staff_roles` VALUES(3,'employee');"
					+"CREATE TABLE `staff_status` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,"
					+"  `status` varchar(32) NOT NULL"
					+");"
					+"INSERT INTO `staff_status` VALUES(1,'Active');"
					+"INSERT INTO `staff_status` VALUES(2,'Inactive');"
					+"CREATE TABLE `suppliers` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,"
					+"  `company` varchar(32) NOT NULL"
					+");"
					+"CREATE TABLE `timeslots` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,"
					+"  `timeslot` varchar(32) NOT NULL"
					+");"
					+"INSERT INTO `timeslots` VALUES(1,'9-12 AM');"
					+"INSERT INTO `timeslots` VALUES(2,'2-5 PM');"
					+"INSERT INTO `timeslots` VALUES(3,'6-8 PM');"
					+"CREATE TABLE payment ("
					+"`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+"`payment` VARCHAR(255) NOT NULL"
					+");"
					+"CREATE TABLE `orderStatuses` ("
					+"`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+"`status` VARCHAR(11) NOT NULL"
					+");"
					+"INSERT INTO `orderStatuses` VALUES(1,'Scheduled');"
					+"INSERT INTO `orderStatuses` VALUES(2,'Dispatched');"
					+"INSERT INTO `orderStatuses` VALUES(3,'Completed');"
					+"INSERT INTO `orderStatuses` VALUES(4,'Cancelled');"
					+"CREATE TABLE `deliveryLocations` ("
					+"`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+"`location` varchar(32) NOT NULL"
					+");"
					+"INSERT INTO `deliveryLocations` VALUES(1,'Central');"
					+"INSERT INTO `deliveryLocations` VALUES(2,'Admiralty');"
					+"INSERT INTO `deliveryLocations` VALUES(3,'Tsim Sha Tsui');"
					+"INSERT INTO `deliveryLocations` VALUES(4,'Jordan');"
					+"INSERT INTO `deliveryLocations` VALUES(5,'Yau Ma Tei');"
					+"INSERT INTO `deliveryLocations` VALUES(6,'Mong Kok');"
					+"INSERT INTO `deliveryLocations` VALUES(7,'Prince Edward');"
					+"INSERT INTO `deliveryLocations` VALUES(8,'Sham Shui Po');"
					+"INSERT INTO `deliveryLocations` VALUES(9,'Mei Foo');"
					+"INSERT INTO `deliveryLocations` VALUES(10,'Lai King');"
					+"INSERT INTO `deliveryLocations` VALUES(11,'Tseun Wan');"
					+"CREATE TABLE `std_notes` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+"  `description` VARCHAR(64) NOT NULL"
					+");"
					+"CREATE TABLE `notes` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,"
					+"  `sales_id` int(11) NOT NULL,"
					+"  `standardCode_id` int(11),"
					+"  `remarks` varchar(32),"
					+"  FOREIGN KEY (`sales_id`) REFERENCES `sales` (`id`),"
					+"  FOREIGN KEY (`standardCode_id`) REFERENCES `std_notes` (`id`)"
					+");"
					+"CREATE TABLE `printStatuses` ("
					+"`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+"`status` VARCHAR(11) NOT NULL,"
					+"PRIMARY KEY (`id`)"
					+");"
					+"INSERT INTO `printStatuses` VALUES(1,'Not Printed');"
					+"INSERT INTO `printStatuses` VALUES(2,'Printed');"
					+"CREATE TABLE `auditTrail` ("
					+"`id` int(11) PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+"`staff_id` int(11) NOT NULL,"
					+"`sales_id` int(11) NOT NULL,"
					+"`table` VARCHAR(32) NOT NULL,"
					+"`oldValue` VARCHAR(255) NOT NULL,"
					+"`newValue` VARCHAR(255) NOT NULL,"
					+"`timestamp` DEFAULT CURRENT_TIMESTAMP,"
					+"FOREIGN KEY (`staff_id`) REFERENCES `staff` (`id`),"
					+"FOREIGN KEY (`sales_id`) REFERENCES `sales` (`id`)"
					+");"
					+"CREATE TABLE `products` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,"
					+"  `name` varchar(32) NOT NULL,"
					+"  `qty` int(11) NOT NULL"
					+"  --`supplier_id` int(11) NOT NULL,"
					+"  --CONSTRAINT `products_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`)"
					+");"
					+"INSERT INTO `products` VALUES(1,'product1',10);"
					+"INSERT INTO `products` VALUES(2,'product2',20);"
					+"CREATE TABLE `quota` ("
					+"`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+"`date` TEXT NOT NULL,"
					+"`timeslot_id` int(11) NOT NULL,"
					+"`qty` int(11) DEFAULT 10 NOT NULL,"
					+"FOREIGN KEY (`timeslot_id`) REFERENCES `timeslots` (`id`)"
					+");"
					+"INSERT INTO `quota` VALUES(1,'2017-04-28',1,10);"
					+"CREATE TABLE `sales` ("
					+"`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+"`printStatus_id` int(11) DEFAULT 1 NOT NULL,"
					+"`status_id` int(11) DEFAULT 1 NOT NULL,"
					+"`customer` VARCHAR(255) DEFAULT `` NOT NULL,"
					+"`phone` VARCHAR(20) DEFAULT `` NOT NULL,"
					+"`deliveryLocation_id` int(11) NOT NULL,"
					+"`date` TEXT DEFAULT `YYYY-MM-DD HH:MM:SS.SSS` NOT NULL,"
					+"`date_created` datetime default current_timestamp NOT NULL,"
					+"`last_updated` datetime default current_timestamp NOT NULL,"
					+"`timeslot_id` int(11) NOT NULL,"
					+"`price` FLOAT(11) DEFAULT 0.0 NOT NULL,"
					+"`payment_id` int(11) DEFAULT 1 NOT NULL,"
					+"FOREIGN KEY (`printStatus_id`) REFERENCES `printStatuses` (`id`)"
					+"FOREIGN KEY (`status_id`) REFERENCES `orderStatuses` (`id`)"
					+"FOREIGN KEY (`deliveryLocation_id`) REFERENCES `deliveryLocations` (`id`),"
					+"FOREIGN KEY (`timeslot_id`) REFERENCES `timeslots` (`id`),"
					+"FOREIGN KEY (`payment_id`) REFERENCES `payment` (`id`)"
					+");"
					+"CREATE TABLE `lineItemSets` ("
					+"  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
					+"  `sales_id` int(11) NOT NULL,"
					+"  `product_id` int(11) NOT NULL,"
					+"  `qty` int(11) NOT NULL,"
					+"  `product` VARCHAR(32) DEFAULT `` NOT NULL,"
					+"  `sequenceNumber` int(11) NOT NULL,"
					+"  `price` float(11) NOT NULL,"
					+"  CONSTRAINT `line_items_ibfk_1` FOREIGN KEY (`sales_id`) REFERENCES `sales` (`id`),"
					+"  CONSTRAINT `line_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)"
					+");"
					+"DELETE FROM sqlite_sequence;"
					+"INSERT INTO `sqlite_sequence` VALUES('sales',8);"
					+"INSERT INTO `sqlite_sequence` VALUES('lineItemSets',1);"
					+"CREATE INDEX `staff_userStatus` ON `staff` (`userStatus`);"
					+"CREATE INDEX `staff_role` ON `staff` (`role`);"
					+"CREATE INDEX `staff_role_2` ON `staff` (`role`);"
					+"CREATE INDEX `location_timeslot_junction_deliveryLocation_id` ON `location_timeslot_junction` (`deliveryLocation_id`);"
					+"CREATE INDEX `location_timeslot_junction_timeslot_id` ON `location_timeslot_junction` (`timeslot_id`);"
					+"CREATE INDEX `line_items_sales_id` ON `lineItemSets` (`sales_id`);"
					+"CREATE INDEX `line_items_product_id` ON `lineItemSets` (`product_id`);"
					+"COMMIT;"
					);
		} catch (SQLException e) {
			logException(e);
		}
	}
	*/
	
	
	/**
	 * Execute prepared insert statement
	 * @param table
	 * @param columns
	 * @param values
	 * @return
	 */
	public int insert(String table, String columns, String values) {
		PreparedStatement pstmt;
		ResultSet rs;
		int retVal = -1;
		String statementStr =  "INSERT INTO "+ table + " (" + columns + ") VALUES ("+ values + ")";
		logInfo(statementStr);
		try {
			pstmt = conn.prepareStatement(
					 statementStr, 
				     Statement.RETURN_GENERATED_KEYS
				     );

			pstmt.executeUpdate();

			rs = pstmt.getGeneratedKeys(); // will return the ID in ID_COLUMN
			
			if (rs.next()){
			    retVal = rs.getInt(1);
			}
		} catch (SQLException e) {
			logException(e);
		} 


		return retVal;
	}
	
	/**
	 * Close Database connection
	 */
	public void close() {
		try {
            if (conn != null) {
                conn.close();
    			logInfo("DB closed successful");
            }
        } catch (Exception e) {
            logException(e);
        }
	}
}
