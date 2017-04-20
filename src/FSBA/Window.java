package FSBA;

import static FSBA.Macro.*;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

public class Window extends JFrame implements ListSelectionListener {
	/** width of the main window */
	final static int win_w = 1280;
	/** height of the main window */
	final static int win_h = 720;
	
	
	/** database connection instance from Main class */
	private DBC dbc;
	/** user's display name from Main class */
	private String userDisplayName;
	
	private DataGrid salesOrderTable;
	private JPanel salesOrderInfo;
	// 0, 0
	private JTextField orderNo;
	// 2, 0
	private JTextField apptDate;
	// 2, 1
	private JTextField apptTime;
	// 3, 0
	private JTextField contactName;
	// 3, 1
	private JTextField contactPhone;
	private DataGrid lineItems;
	private DataGrid notes;
	private DataGrid auditTrail;
	
	/**
	 * Create Main Window of FSBA
	 * Obtain instances of Database Connection, class DBC, and user's display name as private instances.
	 * @param dbc
	 * @param userDisplayName
	 * @throws SQLException
	 */
	public Window(DBC dbc, String userDisplayName) throws SQLException {
		if(userDisplayName == null  || userDisplayName.isEmpty()){
			logErr("Login Failed");
			return;
		} else {
			this.userDisplayName = userDisplayName;
			logInfo("Login as: " + userDisplayName);
		}
		this.dbc = dbc;
		
		
		
		setComponents();
		
	}
	/**
	 * Centers the window to the middle of the screen
	 */
	static void setLocationCenter(Object obj) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - win_w) / 2);
        int y = (int) ((dimension.getHeight() - win_h) / 2);
        ((java.awt.Window) obj).setLocation(x, y);
	}
	/**
	 * Set GUI Components of the main window of FSBA
	 * @throws SQLException
	 */
	@SuppressWarnings("serial")
	private void setComponents() throws SQLException {
		// initialize frame with APPNAME on title bar
		setTitle(Macro.APPNAME);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(win_w, win_h));
        setResizable(false);
        setLocationCenter(this);
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(new JLabel());

        // setup listener for confirmation box closing
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
            	int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit the program?", "Exit Confirmation",JOptionPane.YES_NO_OPTION);
            	
            	if(confirmation == JOptionPane.YES_OPTION) {
            		dispose();
            	}
            }
        });
       
        // Main contentPane setup: vertical split pane
        JSplitPane splitPane = new JSplitPane();
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		splitPane.setContinuousLayout(true);
		splitPane.setDividerSize(0);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		
		GradientPanel gradientPanel = new GradientPanel();
		gradientPanel.setBorder(null);
		gradientPanel.setMinimumSize(new Dimension(0,48));
		splitPane.setLeftComponent(gradientPanel);
		gradientPanel.setLayout(null);
		
		JLabel appName = new JLabel(APPNAME);
		appName.setVerticalAlignment(SwingConstants.BOTTOM);
		appName.setBounds(35, 6, 173, 31);
		appName.setFont(new Font("Lucida Grande", Font.PLAIN, 25));
		appName.setForeground(Color.WHITE);
		gradientPanel.add(appName);
		
		JLabel userDisplayNameLabel = new JLabel(this.userDisplayName);
		userDisplayNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		userDisplayNameLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		userDisplayNameLabel.setForeground(Color.WHITE);
		userDisplayNameLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		userDisplayNameLabel.setBounds(946, 11, 307, 23);
		gradientPanel.add(userDisplayNameLabel);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		splitPane.setRightComponent(tabbedPane);
		
		JScrollPane tabHome = new JScrollPane();
		tabbedPane.addTab("Home", null, tabHome, null);
		
		JSplitPane tabSalesOrder = new JSplitPane();
		tabSalesOrder.setOrientation(JSplitPane.VERTICAL_SPLIT);
		tabSalesOrder.setDividerLocation(100);
		tabbedPane.addTab("Sales Order", null, tabSalesOrder, null);
		
		salesOrderTable = new DataGrid(dbc.select(""
				+ "" // COLUMNS
					+ "s.id AS \"Order No.\", "
					+ "o.status AS \"Status\", "
					+ "s.customer AS \"Customer\", " 
					+ "s.phone AS \"Phone\", "
					+ "d.location AS \"Location\", "
					+ "s.date AS \"Date\", "
					+ "t.timeslot AS \"Timeslot\"",
				"sales AS s ", // TABLE
				"LEFT JOIN orderStatuses AS o " //CONDITIONS
					+ "ON o.id = s.status_id "
				+ "LEFT JOIN deliveryLocations AS d "
					+ "ON d.id = s.deliveryLocation_id "
				+ "LEFT JOIN timeslots AS t "
					+ "ON t.id = s.timeslot_id"
				)) {
			// Set all cells in salesOrderTable not editable
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JScrollPane salesListView = new JScrollPane(salesOrderTable);
		
		// setup listeners
		setupTableListener(salesOrderTable);
		setupCellListener(salesOrderTable);

		JScrollPane salesOrderInfoScrollPane = new  JScrollPane();

		// setup components of tabSalesOrder
		tabSalesOrder.setLeftComponent(salesListView);		
		tabSalesOrder.setRightComponent(salesOrderInfoScrollPane);
		
		// initialize salesOrderInfo JPanel
		salesOrderInfo = new JPanel();
		salesOrderInfoScrollPane.setViewportView(salesOrderInfo);
		salesOrderInfo.setLayout(null);
		setupSalesOrderInfoPanel();
		
		// TAB Financial Reports
		JScrollPane tabFinancialReports = new JScrollPane();
		tabbedPane.addTab("Financial Reports", null, tabFinancialReports, null);
		
		// TAB Business Intelligence
		JScrollPane tabBusinessIntelligence = new JScrollPane();
		tabbedPane.addTab("Business Intelligence", null, tabBusinessIntelligence, null);
		
		// Tab Configuration
		JSplitPane tabConfiguration = new JSplitPane();
		tabbedPane.addTab("Configuration", null, tabConfiguration, null);
		
		JTree tree = new JTree();
		tabConfiguration.setLeftComponent(tree);
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		tabConfiguration.setRightComponent(panel);
		
        
		pack();
		setVisible(true);
	}
	
	/** Helper function for setting position of components */
	private int coor(int mode, int col, int row, int dim) {
		int xOffset = mode == 1 ? -5 : 0;
		int yOffset = mode == 1 ? 18 : 0;
		return (dim == 0 ? 30 + xOffset + 1280/4 * col : 48 + yOffset + 54 * row );
	}
	
	private void setupSalesOrderInfoPanel() throws SQLException {
		final int w = 1280*3/16;
		final int h = 26;
		
		JButton btnSave = new JButton("Save");
		btnSave.setBounds(155, 6, 61, 29);
		salesOrderInfo.add(btnSave);
		
		JButton btnConfirmNewOrder = new JButton("Confirm New Order");
		btnConfirmNewOrder.setBounds(213, 6, 155, 29);
		salesOrderInfo.add(btnConfirmNewOrder);
		
		// Col 0 Row 0
		JLabel lblOrderNo = new JLabel("Sales Order No.");
		lblOrderNo.setBounds(coor(0,0,0,0), coor(0,0,0,1), w, h);
		salesOrderInfo.add(lblOrderNo);
		
		orderNo = new JTextField();
		orderNo.setBounds(coor(1,0,0,0), coor(1,0,0,1), w, h);
		salesOrderInfo.add(orderNo);
		orderNo.setColumns(10);
		
		// Col 0 Row 1
		
		JLabel lblOrderStatus = new JLabel("Order Status");
		lblOrderStatus.setBounds(coor(0,0,1,0), coor(0,0,1,1), w, h);
		salesOrderInfo.add(lblOrderStatus);
		
		DropdownList list = new DropdownList(dbc.select("status", "orderStatuses"));
		list.setBounds(coor(1,0,1,0), coor(1,0,1,1), w, h);
		//list.setSelectedIndex(3);
		salesOrderInfo.add(list);
		
		// Col 0 Row 2
		JLabel lblLineItems = new JLabel("Line Items");
		lblLineItems.setBounds(coor(0,0,2,0), coor(0,0,2,1)-4, w, h);
		salesOrderInfo.add(lblLineItems);
		
		lineItems = new DataGrid(dbc.select(""
					+ "l.id, "
					+ "l.sequenceNumber AS \"Sequence No.\", "
					+ "l.product_id AS \"Product Code\", "
					+ "p.name AS \"Product\", "
					+ "l.qty AS \"Qty\", "
					+ "l.price AS \"Price($)\"",
				"lineItemSets AS l ",
				"LEFT JOIN products AS p "
					+ "ON p.id = l.product_id "
				));
		lineItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane lineItemList = new JScrollPane(lineItems);
		lineItemList.setSize(coor(1,3,2,0) + w - coor(1,0,2,0), 125);
		lineItemList.setLocation(coor(1,0,2,0), coor(1,0,2,1));
		salesOrderInfo.add(lineItemList);
		
		// Col 0 Row 5
		JLabel lblNotesPrintOnOrder = new JLabel("Notes Print on Order");
		lblNotesPrintOnOrder.setBounds(coor(0,0,5,0), coor(0,0,5,1)-4, w, h);
		salesOrderInfo.add(lblNotesPrintOnOrder);
		
		notes = new DataGrid(dbc.select(""
				+ "n.standardCode_id AS \"Standardized Code\", "
				+ "n.remarks AS \"Remarks\" ",
				"notes AS n"
				));
		
		notes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane notesList = new JScrollPane(notes);
		notesList.setSize((coor(1,3,5,0) + w - coor(1,0,5,0))/2 - coor(1,0,5,0), 100);
		notesList.setLocation(coor(1,0,5,0), coor(1,0,5,1));
		salesOrderInfo.add(notesList);
		
		// Col 2 Row 5
		JLabel lblAuditTrail = new JLabel("Audit Trail");
		lblAuditTrail.setBounds(coor(0,2,5,0), coor(0,2,5,1)-4, w, h);
		salesOrderInfo.add(lblAuditTrail);
		
		auditTrail = new DataGrid(dbc.select("*","notes"));
		JScrollPane auditTrailList = new JScrollPane(auditTrail);
		auditTrailList.setSize((coor(1,3,5,0) + w - coor(1,2,5,0)), 100);
		auditTrailList.setLocation(coor(1,2,5,0), coor(1,2,5,1));
		salesOrderInfo.add(auditTrailList);
		
		
		// Col 2 Row 0
		
		JLabel lblApptDate = new JLabel("Appointment Date");
		lblApptDate.setBounds(coor(0,2,0,0), coor(0,2,0,1), w, h);
		salesOrderInfo.add(lblApptDate);
		
		apptDate = new JTextField();
		apptDate.setColumns(10);
		apptDate.setBounds(coor(1,2,0,0), coor(1,2,0,1), w, h);
		salesOrderInfo.add(apptDate);
		
		// Col 2 Row 1

		JLabel lblApptTime = new JLabel("Appointment Time");
		lblApptTime.setBounds(coor(0,2,1,0), coor(0,2,1,1), w, h);
		salesOrderInfo.add(lblApptTime);
		
		apptTime = new JTextField();
		apptTime.setColumns(10);
		apptTime.setBounds(coor(1,2,1,0), coor(1,2,1,1), w, h);
		salesOrderInfo.add(apptTime);
		

		// Col 3 Row 0
		
		JLabel lblContactPerson = new JLabel("Contact Person");
		lblContactPerson.setBounds(coor(0,3,0,0), coor(0,3,0,1), w, h);
		salesOrderInfo.add(lblContactPerson);
		
		contactName = new JTextField();
		contactName.setColumns(10);
		contactName.setBounds(coor(1,3,0,0), coor(1,3,0,1), w, h);
		salesOrderInfo.add(contactName);
		
		// Col 3 Row 1
		
		JLabel lblContactPhoneNo = new JLabel("Contact Phone No.");
		lblContactPhoneNo.setBounds(coor(0,3,1,0), coor(0,3,1,1), w, h);
		salesOrderInfo.add(lblContactPhoneNo);
		
		contactPhone = new JTextField();
		contactPhone.setColumns(10);
		contactPhone.setBounds(coor(1,3,1,0), coor(1,3,1,1), w, h);
		salesOrderInfo.add(contactPhone);
	}
	public void valueChanged(ListSelectionEvent e) {
		
        
	}
	
	/** Cell listener setup */
	private void setupCellListener(DataGrid table) {
		ListSelectionModel rowSelectionModel = table.getSelectionModel();
	    rowSelectionModel.addListSelectionListener(this);
	}
	
	/** Table listener setup */
	private void setupTableListener(DataGrid table) {
		TableModel tableModel = table.getModel();
		tableModel.addTableModelListener(new TableModelListener() {
			 public void tableChanged(TableModelEvent tme) {
			        if (tme.getType() == TableModelEvent.UPDATE) {
			          System.out.println("Cell " + tme.getFirstRow() + ", " + tme.getColumn() + " changed."
			              + " The new value: " + tableModel.getValueAt(tme.getFirstRow(), tme.getColumn()));
			        }
			 }
		});
	}
}
