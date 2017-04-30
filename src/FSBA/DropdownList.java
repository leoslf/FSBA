package FSBA;

import java.*;
import java.sql.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import static FSBA.Macro.*;

@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
class DropdownList extends JComboBox {
	/** ResultSet instance obtained from Constructor argument */
	private ResultSet resultSet = null;
	/** 2-Dimension List of Rows */
	private Vector<String> rows = new Vector<String>();
	
	DropdownList(ResultSet _resultSet) {
		resultSet = _resultSet;

		setModel(createListModel());
	}
	
	private DefaultComboBoxModel createListModel() {
		try {
			getRows();
		} catch (SQLException e) {
			logException(e);
		}
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		for(String val : rows) {
			comboBoxModel.addElement(val);
		}
		return comboBoxModel;
	}
	
	/**
	 * Obtain Rows from resultSet
	 * @throws SQLException handled in subroutine 
	 */
	void getRows() throws SQLException {
		rows.add("");
		while(resultSet.next()) {
				getNextRow();
		}
		
	}
	
	/**
	 * Helper function to obtain nextRows from resultSet
	 */
	void getNextRow() {
		try {
			String row = resultSet.getString(1);
			rows.add(row);
		} catch (SQLException e) {
			logException(e);
		}
	}
}
