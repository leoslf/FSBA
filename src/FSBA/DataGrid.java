package FSBA;

import java.*;
import java.awt.Color;
import java.sql.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import static FSBA.Macro.*;

@SuppressWarnings("serial")
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
	public DataGrid(ResultSet _resultSet) throws SQLException {
		update(_resultSet);
	}
	
	/**
	 * Update the table model of the data grid
	 * @param _resultSet
	 * @throws SQLException
	 */
	public void update(ResultSet _resultSet) throws SQLException  {
		resultSet = _resultSet;
		columnNames = new Vector<String>();
		rows = new Vector<Vector<Object>>();
		setModel(createTableModel());
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
