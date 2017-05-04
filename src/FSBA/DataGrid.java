package FSBA;

import java.awt.Color;
import java.sql.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import static FSBA.Macro.*;

@SuppressWarnings("serial")
/**
 * Data Grid / Table Object customized to be constructed with java.sql.ResultSet
 * @author leosin
 *
 */
class DataGrid extends JTable {
	/** ResultSet instance obtained from Constructor argument */
	private ResultSet resultSet = null;
	/** Meta data of ResultSet passed in */
	private ResultSetMetaData metaData = null;
	/** Number of Columns in the ResultSet */
	private int columnCount = -1;
	/** List of Column names */
	private Vector<String> columnNames;
	/** 2-Dimension List of Rows */
	private Vector<Vector<Object>> rows;
	
	/**
	 * Create table by set model from argument ResultSet _resultSet
	 * @param _resultSet ResultSet instance
	 * @throws SQLException handled in sub routine
	 */
	public DataGrid(ResultSet _resultSet, String tableName) throws SQLException {
		this.setName(tableName);
		update(_resultSet);
	}
	
	/**
	 * Update the table model of the data grid
	 * @param _resultSet new resultSet
	 */
	public void update(ResultSet _resultSet) {
		resultSet = _resultSet;
		columnNames = new Vector<String>();
		rows = new Vector<Vector<Object>>();
		try {
			// set the table model
			setModel(createTableModel());
		} catch (SQLException e) {
			logException(e);
		}
		// set grid border color as grey: rgb(211,211,211)
		setGridColor(new Color(211, 211, 211));
	}
	
	/**
	 * Helper function of to create table model
	 * @return Table model from resultSet
	 * @throws SQLException handled in sub routine
	 */
	private DefaultTableModel createTableModel() throws SQLException {
		if(resultSet == null) {
			return new DefaultTableModel();
		}
		getHeader();
		getRows();
		
		return new DefaultTableModel(rows, columnNames);
	}
	/**
	 * Obtain header of the resultSet
	 */
	private void getHeader() {
		try {
			// get metadata from ResultSet
			metaData = resultSet.getMetaData();
			// get column count from ResultSetMetaData from ResultSet
			columnCount = metaData.getColumnCount();
			
			for(int i = 1; i <= columnCount; ++i) {
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
				row.add(resultSet.getObject(i));
			}
			rows.add(row);
		} catch (SQLException e) {
			logException(e);
		}
	}
}
