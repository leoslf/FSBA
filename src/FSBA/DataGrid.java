package FSBA;

import java.*;
import java.sql.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import static FSBA.Macro.*;

class DataGrid extends JTable {
	/** ResultSet instance obtained from Constructor argument */
	private ResultSet resultSet = null;
	/** Meta data of ResultSet passed in */
	private ResultSetMetaData metaData = null;
	/** Number of Columns in the ResultSet */
	private int columnCount = -1;
	/** List of Column names */
	private Vector<String> columnNames = new Vector<String>();
	/** 2-Dimension List of Rows */
	private Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
	
	//TODO:
	public DataGrid() {
		
	}
	
	/**
	 * Create table by set model from argument ResultSet _resultSet
	 * @param _resultSet
	 * @throws SQLException
	 */
	public DataGrid(ResultSet _resultSet) throws SQLException {
		resultSet = _resultSet;
		setModel(createTableModel());
		//setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	}
	/**
	 * Helper function of to create table model
	 * @return Table model from resultSet
	 * @throws SQLException
	 */
	private DefaultTableModel createTableModel() throws SQLException {
		getHeader();
		getRows();
		
		return new DefaultTableModel(rows, columnNames);
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
	 * @throws SQLException
	 */
	private void getRows() throws SQLException {
		while(resultSet.next()) {
				getNextRow();
		}
		
	}
	
	/**
	 * Helper function to obtain nextRows from resultSet
	 */
	private void getNextRow() {
		Vector<Object> row = new Vector<Object>();
		try {
			for(int i = 1; i <= columnCount; ++i) {
				//debug(""+resultSet.getObject(i));
				row.add(resultSet.getObject(i));
			}
			rows.add(row);
		} catch (SQLException e) {
			logException(e);
		}
	}
}
