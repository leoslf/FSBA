package FSBA;

import static FSBA.Macro.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

/**
 * Utilities for ResultSet manipulation
 * @author leosin
 *
 */
class ResultSetUtil {
	/** ResultSet instance */
	private ResultSet resultSet;
	/** ResultSet meta-data instance */
	private ResultSetMetaData metaData;
	
	/** column count */
	private int columnCount = -1;
	/** column names */
	private Vector<String> columnNames = new Vector<String>();
	/** data rows */
	private Vector<Vector<String>> rows = new Vector<Vector<String>>();
	
	/**
	 * Initialize the class with ResultSet and get data from it
	 * @param rs resultSet class
	 */
	public ResultSetUtil(ResultSet rs) {
		resultSet = rs;
		getHeader();
		getRows();
	}
	
	/**
	 * Obtain header of the resultSet
	 */
	private void getHeader() {
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
	 */
	private void getRows() {
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
	private void getNextRow() {
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
	
	/**
	 * accessor for columnNames
	 * @return Vector of String that contains column names
	 */
	public Vector<String> getVectorColumnNames() {
		return columnNames;
	}
	
	/**
	 * accessor for rows
	 * @return Vector of Vector of String that contains rows
	 */
	public Vector<Vector<String>> getVectorRows() {
		return rows;
	}
	
	/**
	 * accessor for rows
	 * @return 2D String array that contains rows
	 */
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
	
	/**
	 * accessor for columNames 
	 * @return one-dimension String array that contains column names
	 */
	public String[] getStrArrColumnNames() {
		return columnNames.toArray(new String[columnNames.size()]);
	}
}

