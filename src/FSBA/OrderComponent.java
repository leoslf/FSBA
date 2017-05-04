package FSBA;

import java.util.Vector;

import javax.swing.*;
import javax.swing.table.*;

import static FSBA.Macro.*;

/**
 * To store order component in format
 * @author leosin
 *
 */
public class OrderComponent {
	/** Column names of the order component */
	private Vector<String> columns;
	/** The data row of the order component */
	private Vector<String> values;
	private Vector<Vector<String>> valuesArr;
	
	/**
	 * Constructor of OrderComponent
	 * Formats input to column and values
	 * @param arrs String of structured input with format [Column name 1]=[Value 1],...,[Column name n]=[Value n]
	 */
	public OrderComponent(String arrs) {
		columns = new Vector<String>();
		values = new Vector<String>();
		
		String[] arr = arrs.split(",");
		for(String elem : arr) {
			String[] temp = elem.split("=");
			if(temp.length > 1) {
				columns.add(temp[0]);
				values.add(temp[1]);
			}
		}
	}
	
	public OrderComponent(JTable table,String[][][] schema) {
		int[] dim = new int[2];
		String[][] data = getTableData(table,dim);
		columns = new Vector<String>();
		valuesArr = new Vector<Vector<String>>();
		for(int i = 0; i < dim[1]; ++i) {
			columns.add(schema[0][i][0]);
		}
		for(int i = 0; i < dim[0]; ++i) {
			Vector<String> row = new Vector<String>();
			for(int j = 0; j < dim[1]; ++j) {
				row.add(data[i][j]);
			}
			valuesArr.add(row);
		}
	}
	
	/**
	 * Accessor of table data
	 * @param table on database
	 * @param dim row, column
	 * @return 2-DIMENSIONAL STRING ARRAY OF TABLE DATA
	 */
	private String[][] getTableData (JTable table,int[] dim) {
	    DefaultTableModel model = (DefaultTableModel) table.getModel();
	    int rowNo = model.getRowCount(), colNo = model.getColumnCount();
	    dim[0] = rowNo;
	    dim[1] = colNo;
	    logInfo("rowNo:"+rowNo+" colNo:"+colNo);
	    String[][] tableData = new String[rowNo][colNo];
	    for (int i = 0 ; i < rowNo ; i++) {
	        for (int j = 0 ; j < colNo ; j++) {
	            tableData[i][j] = model.getValueAt(i, j) != null ? model.getValueAt(i,j).toString() : "\"\"";
	            logInfo("i:"+i+" j:" + j + " " + tableData[i][j]);
	        }
	    }
	    return tableData;
	}
	
	/**
	 * Accessor of column names in the format SQL accepts
	 * @return String of column names: column1, column2, ..., columnN
	 */
	public String getColumns() {
		String[] colStrArr = columns.toArray(new String[columns.size()]); 
		logInfo(String.join(", ", colStrArr));
		return ""+String.join(", ", colStrArr);
	}
	
	/**
	 * Accessor of row values in the format SQL accepts
	 * @return String of values: value1, value2, ..., valueN
	 */
	public String getValues() {
		String[] valStrArr = values.toArray(new String[values.size()]);
		logInfo(String.join(", ", valStrArr));
		return ""+String.join(", ", valStrArr);
	}
	
	/**
	 * *
	 * Accessor of row values in the format SQL accepts
	 * @param index index of vector
	 * @return String of values: value1, value2, ..., valueN
	 */
	public String getValuesArr(int index) {
		String[] valStrArr = valuesArr.get(index).toArray(new String[valuesArr.get(index).size()]);
		for(int i = 0; i < valStrArr.length; ++i) {
			valStrArr[i] = "\"" + valStrArr[i] + "\"";
		}
		logInfo("index:"+ index + " " + String.join(", ", valStrArr));
		return ""+String.join(", ", valStrArr);
	}
	
	/**
	 * accessor of length of valuesArr 
	 * @return length of valuesArr
	 */
	public int getValuesArrLength() {
		return valuesArr.size();
	}
}
