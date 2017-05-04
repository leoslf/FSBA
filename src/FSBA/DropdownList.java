package FSBA;

import java.sql.*;
import java.util.*;

import javax.swing.*;

import static FSBA.Macro.*;

@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
/**
 * Dropdown List / combobox customized to be constructed with java.sql.ResultSet
 * @author leosin
 *
 */
class DropdownList extends JComboBox {
	/** ResultSet instance obtained from Constructor argument */
	private ResultSet resultSet = null;
	/** 2-Dimension List of Rows */
	private Vector<String> rows = new Vector<String>();
	
	/**
	 * Initialze Dropdown list /combobox
	 * @param _resultSet content in ResultSet
	 */
	DropdownList(ResultSet _resultSet) {
		resultSet = _resultSet;

		setModel(createListModel());
	}
	
	/**
	 * create list model 
	 * @return combobox model
	 */
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
