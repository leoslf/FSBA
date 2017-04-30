package FSBA;

import static FSBA.Macro.*;
import static FSBA.SalesOrderUI.win_h;
import static FSBA.SalesOrderUI.win_w;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

/**
 * Main window of the system
 * @author leosin
 *
 */
public class SalesOrderUI extends JFrame implements ListSelectionListener, ActionListener {
	/* ------------------------------- *
	 *  instance variables definition  *
	 * ------------------------------- */
	
	/** width of the main window */
	final static int win_w = 1280;
	/** height of the main window */
	final static int win_h = 720;
	
	
	/** SalesOrderManagement Tools */
	private SalesOrderAPI salesMgmt;
	/** user's display name from Main class */
	private String[] user;
	
	
	/** Panel for the Sales Order tab */
	private JPanel salesOrderInfo;
	
	// 0, 0
	/** text field for order number */
	private JTextField orderNo;
	// 0, 1
	/** dropdown list / combobox of order status */
	private DropdownList statusList;
	
	// 1, 0
	/** dropdown list for location */
	private DropdownList location;
	
	// 2, 0
	/** text field for appointment date */
	private JTextField apptDate;
	// 2, 1
	/** text field for appointment time */
	private JTextField apptTime;
	
	// 3, 0
	/** text field for contact name of customer */
	private JTextField contactName;
	// 3, 1
	/** text field of contact phone number of customer */
	private JTextField contactPhone;
	
	
	/** Order instance */
	private Order order;
	
	/** Table model of sales order */
	private DataGrid salesOrderTable;
	/** table of lineitems */
	private DataGrid lineItems;
	/** table of notes print on order */
	private DataGrid notes;
	/** table of audit trail */
	private DataGrid auditTrail;

	/** New button of Order */
	private JButton btnNewOrder;
	/** Save button of Order */
	private JButton btnSave;
	/** Confirm New Order button of Order */
	private JButton btnConfirmNewOrder;
	/** date time picker button */
	private JButton btnDateTimePicker;
	
	/** New button of Line items */
	private JButton btnNewLineItem;
	/** New button of Line items */
	private JButton btnNewNote;
	
	/** appointment date and time */
	private String[] apptDateTime = new String[2];
	
	/* ------------------------- *
	 *  Nested class definition	 *
	 * ------------------------- */
	
	/**
	 * Helper class for the LogoBar
	 */
	class GradientPanel extends JPanel {

		/**
		 * Create a gradient panel of grey color
		 */
		public GradientPanel() {
			//this.setBorder(BorderFactory.createEmptyBorder(N, N, N, N));
			this.setBorder(BorderFactory.createEmptyBorder());
		}
		
		/**
		 * Overrides the paintComponents inherited from JPanel
		 */
		@Override
	    public void paintComponent(Graphics graphics) {
	        super.paintComponent(graphics);
	        Graphics2D graphics2d = (Graphics2D) graphics;
	        Color color1 = new Color(0x44,0x44,0x44);
	        Color color2 = new Color(0x20,0x20,0x20);
	        int w = getWidth();
	        int h = getHeight();
	        GradientPaint gp = new GradientPaint(
	            0, 0, color1, 0, h, color2);
	        graphics2d.setPaint(gp);
	        graphics2d.fillRect(0, 0, w, h);
	    }
	}

	class DateTimePicker extends JDialog implements ActionListener {
		/** content panel */
		private JPanel panel;
		private JTextField date;
		private DataGrid table;
		private JButton btnPick;
		private JButton btnCancel;
		private String[] dateTime;
		private String[] arr;
		
		public DateTimePicker(String[] dateTime, SalesOrderAPI salesMgmt) {
			this.dateTime = dateTime;
			setResizable(false);
			setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			setupComponents();
			
			this.setVisible(true);
		}
		
		private void setupComponents() {
			this.setTitle("Date-Time Picker");
	        setSize(new Dimension(win_w/2, win_h));
			SalesOrderUI.setLocationCenter(this,win_w/2, win_h);
			
			
			panel = new JPanel();
		
			panel.setLayout(null);
			
			JLabel dateLabel = new JLabel("Date");
			dateLabel.setBounds(20,10,1280*3/16, 26);
			panel.add(dateLabel);
			
			
			date = new JTextField();
			//date.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
			date.setBounds(60, 10, 100, 26);
			panel.add(date);
			
			try {
				table = new DataGrid(salesMgmt.query(Accesses.datetime))  {
					// Set all cells in table not editable
					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};
				table.getSelectionModel().addListSelectionListener(
					    new javax.swing.event.ListSelectionListener() {
					    	@Override
					        public void valueChanged(ListSelectionEvent evt) {
				    			if(!evt.getValueIsAdjusting()) {
				    				btnPick.setEnabled(true);
					    		}
					        }
					    }
					);
			} catch (SQLException e1) {
				logException(e1);
			}
			
			JScrollPane tablePane = new JScrollPane(table);
			tablePane.setSize(win_w/2-20*2, win_h - 105);
			tablePane.setLocation(20, 40);
			panel.add(tablePane);

			btnPick = new JButton("Pick");
			btnPick.setBounds(win_w/2 - 20 - 130, win_h - 50, 61, 29);
			btnPick.addActionListener(DateTimePicker.this);
			btnPick.setEnabled(false);
			btnPick.setActionCommand("btnPick");
			panel.add(btnPick);
			
			btnCancel = new JButton("Cancel");
			btnCancel.setBounds(win_w/2 - 20 - 70, win_h - 50, 80, 29);
			btnCancel.addActionListener(DateTimePicker.this);
			btnCancel.setActionCommand("btnCancel");
			panel.add(btnCancel);
			
			setContentPane(panel);

			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

			addWindowListener(new WindowAdapter() {

	            @Override
	            public void windowClosing(WindowEvent e) {
	                dispose();
	            }
	        });
			date.addActionListener(new ActionListener() {
		        @Override
		        public void actionPerformed(ActionEvent e) {
					String dateInput = new String(date.getText());
					
					btnPick.setEnabled(false);
					if(dateInput.matches("^([1-9]|0[1-9]|[12][0-9]|3[01])[- /.]([1-9]|0[1-9]|1[012])[- /.]((19|20)\\d\\d|\\d\\d)$")) {
						//dispose();
						arr = dateInput.split("/");
						logInfo(arr[0] + " " + arr[1] + " " + arr[2]);
						if(arr[2].matches("\\d{2}")) {
							String prefix = Integer.parseInt(arr[2]) < 50 ? "20" : "19";
							arr[2] = prefix + arr[2];
						}
						logInfo(arr[0] + " " + arr[1] + " " + arr[2]);
						try {
							table.update(salesMgmt.query(Accesses.datetime, "date=strftime(\"%Y-%m-%d\",\""+arr[2]+"-"+String.format("%02d",Integer.parseInt(arr[1]))+"-"+arr[0]+"\")"));
						} catch (SQLException e1) {
							logException(e1);
						}
					} else {
						wrongInput();
					}
				
		        }
		    });

			
		}
		
		private void wrongInput() {
			date.selectAll();
			JOptionPane.showMessageDialog(null, "Invalid input!", "Try again", JOptionPane.ERROR_MESSAGE);
			date.requestFocusInWindow();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
	        logInfo(action);
	        if (action.equals("btnPick")) {
	        	logInfo("btnPick pressed");
	        	dateTime[0] = arr[2]+"-"+arr[1]+"-"+arr[0];
	        	dateTime[1] = table.getValueAt(table.getSelectedRow(), table.getColumn("Time slot").getModelIndex()).toString();
	        	//logInfo(dateTime[0] + " " + dateTime[1]);
	        	dispose();
	        } else if (action.equals("btnCancel")) {
	        	dispose();
	        }
		}

		

	}
	
	
	/**
	 * Create Main Window of FSBA
	 * Obtain instances of Database Connection, class DBC, and user's display name as private instances.
	 * @param dbc Database Connection of JDBC
	 * @param userDisplayName User's Display name
	 * @throws SQLException throws SQLException
	 */
	public SalesOrderUI(String[] user) throws SQLException {
		if(user == null  || user[0].isEmpty() || user[1].isEmpty()){
			logErr("Login Failed");
			return;
		} else {
			this.user = user;
			logInfo("Login as: " + user[1]);
		}

		this.salesMgmt = new SalesOrderAPI();
		
		
		setComponents();
		
		salesMgmt.BIDataDump();
	}
	
	/**
	 * Centers the window to the middle of the screen
	 * @param obj Window's instance
	 */
	static void setLocationCenter(Object obj, int w, int h) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - w) / 2);
        int y = (int) ((dimension.getHeight() - h) / 2);
        ((java.awt.Window) obj).setLocation(x, y);
	}
	
	/**
	 * Set GUI Components of the main window of FSBA
	 * @throws SQLException ignoring
	 */
	private void setComponents() throws SQLException {
		// initialize frame with APPNAME on title bar
		setTitle(Macro.APPNAME);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(win_w, win_h));
        setResizable(false);
        setLocationCenter(this,win_w,win_h);
        
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
       
        order = new Order();
        
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
		
		JLabel userDisplayNameLabel = new JLabel(this.user[1]);
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

		//NOTE: trying to use salesMgmt.query
		salesOrderTable = new DataGrid(salesMgmt.query(Accesses.order)) {
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
		setupSalesOrderInfoPanel();
		salesOrderInfoScrollPane.setViewportView(salesOrderInfo);
		
		// TAB Financial Reports
		//JScrollPane tabFinancialReports = new JScrollPane();
		//tabbedPane.addTab("Financial Reports", null, tabFinancialReports, null);
		
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
        
		// TAB Help
		JScrollPane tabHelp = new JScrollPane();
		tabbedPane.addTab("Help", null, tabHelp, null);
		
		pack();
		setVisible(true);
	}
	
	/** 
	 * Helper function for setting position of components 
	 * @param mode {0, 1}: 0 for label mode and 1 for field mode
	 * @param col column index, zero-based
	 * @param row row index, zero-based
	 * @param dim dimension, 0 for x and 1 for y
	 * @return the requested coordinate component for specified dimension
	 */
	private int coor(int mode, int col, int row, int dim) {
		int xOffset = mode == 1 ? -5 : 0;
		int yOffset = mode == 1 ? 18 : 0;
		return (dim == 0 ? 30 + xOffset + 1280/4 * col : 48 + yOffset + 54 * row );
	}
	
	/**
	 * SalesOrderInfoPanel component setup
	 * @throws SQLException ignore
	 */
	private void setupSalesOrderInfoPanel() throws SQLException {
		final int w = 1280*3/16;
		final int h = 26;
		
		salesOrderInfo = new JPanel();
		salesOrderInfo.setLayout(null);
		
		btnNewOrder = new JButton("New");
		btnNewOrder.setBounds(100, 6, 61, 29);
		btnNewOrder.addActionListener(this);
		btnNewOrder.setActionCommand("newOrder");
		salesOrderInfo.add(btnNewOrder);
		
		btnSave = new JButton("Save");
		btnSave.setBounds(155, 6, 61, 29);
		btnSave.setEnabled(false);
		btnSave.addActionListener(this);
		btnSave.setActionCommand("save");
		salesOrderInfo.add(btnSave);
		
		btnConfirmNewOrder = new JButton("Confirm New Order");
		btnConfirmNewOrder.setBounds(213, 6, 155, 29);
		btnConfirmNewOrder.addActionListener(this);
		btnConfirmNewOrder.setEnabled(false);
		btnConfirmNewOrder.setActionCommand("confirmNewOrder");
		salesOrderInfo.add(btnConfirmNewOrder);
		
		btnNewLineItem = new JButton("New");
		btnNewLineItem.setBounds(100, 150, 61, 29);
		btnNewLineItem.addActionListener(this);
		btnNewLineItem.setEnabled(false);
		btnNewLineItem.setActionCommand("newLineItem");
		salesOrderInfo.add(btnNewLineItem);
		
		btnNewNote = new JButton("New");
		btnNewNote.setBounds(170, 312, 61, 29);
		btnNewNote.addActionListener(this);
		btnNewNote.setEnabled(false);
		btnNewNote.setActionCommand("newNote");
		salesOrderInfo.add(btnNewNote);
		
		btnDateTimePicker = new JButton(" ");
		btnDateTimePicker.setBounds(855, 120, 50, 25);
		btnDateTimePicker.addActionListener(this);
		btnDateTimePicker.setEnabled(false);
		btnDateTimePicker.setActionCommand("DateTimePicker");
		salesOrderInfo.add(btnDateTimePicker);
		
		// Col 0 Row 0
		JLabel lblOrderNo = new JLabel("Sales Order No.");
		lblOrderNo.setBounds(coor(0,0,0,0), coor(0,0,0,1), w, h);
		salesOrderInfo.add(lblOrderNo);
		
		orderNo = new JTextField();
		orderNo.setBounds(coor(1,0,0,0), coor(1,0,0,1), w, h);
		orderNo.setEnabled(false);
		salesOrderInfo.add(orderNo);
		
		// Col 0 Row 1
		
		JLabel lblOrderStatus = new JLabel("Order Status");
		lblOrderStatus.setBounds(coor(0,0,1,0), coor(0,0,1,1), w, h);
		salesOrderInfo.add(lblOrderStatus);
		
		statusList = new DropdownList(salesMgmt.query(Accesses.orderStatus));
		statusList.setBounds(coor(1,0,1,0), coor(1,0,1,1), w, h);
		statusList.setEnabled(false);
		salesOrderInfo.add(statusList);
		
		// Col 0 Row 2
		JLabel lblLineItems = new JLabel("Line Items");
		lblLineItems.setBounds(coor(0,0,2,0), coor(0,0,2,1)-4, w, h);
		salesOrderInfo.add(lblLineItems);
		
		lineItems = new DataGrid(salesMgmt.query(Accesses.lineItems,"id = -1"));
		lineItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane lineItemList = new JScrollPane(lineItems);
		lineItemList.setSize(coor(1,3,2,0) + w - coor(1,0,2,0), 125);
		lineItemList.setLocation(coor(1,0,2,0), coor(1,0,2,1));
		salesOrderInfo.add(lineItemList);
		
		// Col 0 Row 5
		JLabel lblNotesPrintOnOrder = new JLabel("Notes Print on Order");
		lblNotesPrintOnOrder.setBounds(coor(0,0,5,0), coor(0,0,5,1)-4, w, h);
		salesOrderInfo.add(lblNotesPrintOnOrder);
		
		notes = new DataGrid(salesMgmt.query(Accesses.notes));
		
		notes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane notesList = new JScrollPane(notes);
		notesList.setSize((coor(1,3,5,0) + w - coor(1,0,5,0))/2 - coor(1,0,5,0), 100);
		notesList.setLocation(coor(1,0,5,0), coor(1,0,5,1));
		salesOrderInfo.add(notesList);
		
		// Col 2 Row 5
		JLabel lblAuditTrail = new JLabel("Audit Trail");
		lblAuditTrail.setBounds(coor(0,2,5,0), coor(0,2,5,1)-4, w, h);
		salesOrderInfo.add(lblAuditTrail);
		
		auditTrail = new DataGrid(salesMgmt.query(Accesses.auditTrail, "a.sales_id="+order.getOrderNo())) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		JScrollPane auditTrailList = new JScrollPane(auditTrail);
		auditTrailList.setSize((coor(1,3,5,0) + w - coor(1,2,5,0)), 100);
		auditTrailList.setLocation(coor(1,2,5,0), coor(1,2,5,1));
		salesOrderInfo.add(auditTrailList);
		
		// Col 1 Row 0
		JLabel lblLocation = new JLabel("Location");
		lblLocation.setBounds(coor(0,1,0,0), coor(0,1,0,1), w, h);
		salesOrderInfo.add(lblLocation);
		
		location = new DropdownList(salesMgmt.query(Accesses.location));
		location.setBounds(coor(1,1,0,0), coor(1,1,0,1), w, h);
		location.setEnabled(false);
		salesOrderInfo.add(location);
		
		
		// Col 2 Row 0
		
		JLabel lblApptDate = new JLabel("Appointment Date");
		lblApptDate.setBounds(coor(0,2,0,0), coor(0,2,0,1), w, h);
		salesOrderInfo.add(lblApptDate);
		
		apptDate = new JTextField();
		apptDate.setColumns(10);
		apptDate.setBounds(coor(1,2,0,0), coor(1,2,0,1), w, h);
		apptDate.setEnabled(false);
		salesOrderInfo.add(apptDate);
		
		// Col 2 Row 1

		JLabel lblApptTime = new JLabel("Appointment Time");
		lblApptTime.setBounds(coor(0,2,1,0), coor(0,2,1,1), w, h);
		salesOrderInfo.add(lblApptTime);
		
		apptTime = new JTextField();
		apptTime.setColumns(10);
		apptTime.setBounds(coor(1,2,1,0), coor(1,2,1,1), w, h);
		apptTime.setEnabled(false);
		salesOrderInfo.add(apptTime);
		

		// Col 3 Row 0
		
		JLabel lblContactPerson = new JLabel("Contact Person");
		lblContactPerson.setBounds(coor(0,3,0,0), coor(0,3,0,1), w, h);
		salesOrderInfo.add(lblContactPerson);
		
		contactName = new JTextField();
		contactName.setColumns(10);
		contactName.setBounds(coor(1,3,0,0), coor(1,3,0,1), w, h);
		contactName.setEnabled(false);
		salesOrderInfo.add(contactName);
		
		// Col 3 Row 1
		
		JLabel lblContactPhoneNo = new JLabel("Contact Phone No.");
		lblContactPhoneNo.setBounds(coor(0,3,1,0), coor(0,3,1,1), w, h);
		salesOrderInfo.add(lblContactPhoneNo);
		
		contactPhone = new JTextField();
		contactPhone.setColumns(10);
		contactPhone.setBounds(coor(1,3,1,0), coor(1,3,1,1), w, h);
		contactPhone.setEnabled(false);
		salesOrderInfo.add(contactPhone);
	}
	
	/**
	 * function to update the content and state of the components on the panel after the initialization in the beginning 
	 */
	private void updateSalesOrderInfoPanel() {
		boolean active = order.getOrderNo() >= 0;
		ResultSet rs = null;
		String orderNum = "";
		String listStr = "";
		String locationStr = "";
		String apptDateStr = "";
		String apptTimeStr = "";
		String contactNameStr = "";
		String contactPhoneStr = "";
		if(active) {
			btnSave.setEnabled(order.getOrderNo() > 0);
			btnConfirmNewOrder.setEnabled(order.getOrderNo() == 0);
			btnNewLineItem.setEnabled(true);
			btnNewNote.setEnabled(true);
			btnDateTimePicker.setEnabled(true);
			try {
				rs = salesMgmt.query(Accesses.order, "s.id="+order.getOrderNo());
				while(rs.next()) {
					orderNum = rs.getString("Order No.");
					listStr = rs.getString("Status");
					locationStr = rs.getString("Location");
					apptDateStr = rs.getString("Date");
					apptTimeStr = rs.getString("Timeslot");
					contactNameStr = rs.getString("Customer");
					contactPhoneStr = rs.getString("Phone");
				}
			} catch (SQLException e) {
				logException(e);
			} finally {
				try {
					rs.close();
				} catch (SQLException e) {				
					logException(e);
				}
			}
		}
		
		
		orderNo.setText(orderNum);
		
		// Col 0 Row 1	
		statusList.setEnabled(active);
		statusList.setSelectedItem(listStr);

		// Col 1 Row 0
		location.setEnabled(active);
		location.setSelectedItem(locationStr);
		
		// Col 2 Row 0
		apptDate.setText(apptDateStr);
		
		// Col 2 Row 1
		apptTime.setText(apptTimeStr);
		

		// Col 3 Row 0
		contactName.setEnabled(active);
		contactName.setText(contactNameStr);
		
		// Col 3 Row 1
		contactPhone.setEnabled(active);
		contactPhone.setText(contactPhoneStr);
		
		try {
			lineItems.update(salesMgmt.query(Accesses.lineItems,"l.sales_id="+order.getOrderNo()));
			notes.update(salesMgmt.query(Accesses.notes,"n.id="+order.getOrderNo()));
			auditTrail.update(salesMgmt.query(Accesses.auditTrail, "a.sales_id="+order.getOrderNo()));
		} catch (SQLException e) {
			logException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
    public void actionPerformed(ActionEvent ae) {
        String action = ae.getActionCommand();
        logInfo(action);
        if (action.equals("newOrder")) {
        	logInfo("btnNewOrder pressed");
            order.setOrderNo(0);
            updateSalesOrderInfoPanel();
            salesOrderInfo.repaint();
        } else if(action.equals("save")) {
        	
        } else if(action.equals("confirmNewOrder")) {
        	logInfo("btnConfirmNewOrder pressed");
        	logInfo("customer=\""+contactName.getText()+"\",phone=\""+contactPhone.getText()+"\",deliveryLocation_id=\""+location.getSelectedIndex()+"\",date=\""+apptDate.getText()+"\",timeslot_id=\""+salesMgmt.queryID("timeslot",apptTime.getText())+"\"");
        	int[][] rowID = salesMgmt.insertOrder(
        			new OrderComponent("customer=\""+contactName.getText()+"\",phone=\""+contactPhone.getText()+"\",deliveryLocation_id=\""+location.getSelectedIndex()+"\",date=\""+apptDate.getText()+"\",timeslot_id=\""+salesMgmt.queryID("timeslot",apptTime.getText())+"\""), 
        			new OrderComponent(lineItems,SalesOrderAPI.lineItemSets), 
        			new OrderComponent(notes,SalesOrderAPI.notes)
        		);
        	//TODO:
        } else if(action.equals("newLineItem")||action.equals("newNote")) {
        	DataGrid grid = action.equals("newLineItem") ? lineItems : notes;
        	Object[] rowData = new Object[grid.getModel().getColumnCount()];
        	if(action.equals("newLineItem")) {
        		rowData[0] = grid.getModel().getRowCount() + 1;
        	}
        	((DefaultTableModel)grid.getModel()).addRow(rowData);
        } else if(action.equals("DateTimePicker")) {
        	DateTimePicker picker = new DateTimePicker(apptDateTime, salesMgmt);
        	apptDate.setText(apptDateTime[0]);
        	apptTime.setText(apptDateTime[1]);
        }
    }
	

	/** 
	 * Cell listener setup
	 * @param table table to listen
	 */
	private void setupCellListener(DataGrid table) {
		ListSelectionModel rowSelectionModel = table.getSelectionModel();
	    rowSelectionModel.addListSelectionListener(this); // valueChanged()
	}
	
	/**
	 * Event Listener for value changed in cell
	 * @param e Event of List Selection
	 */
	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()) {
			int orderNo = -1;
			try {
				orderNo = (int)salesOrderTable.getValueAt(salesOrderTable.getSelectedRow(), 0);
			} catch (ArrayIndexOutOfBoundsException ex) {
				logException(ex);
			}
			logInfo("Selected Order No.:" + orderNo);
			order.setOrderNo(orderNo);
			updateSalesOrderInfoPanel();
	        salesOrderInfo.repaint();
		}
	}
	
	
	/** 
	 * Table listener setup
	 * @param table table that the cells belongs to for listening
	 */
	private void setupTableListener(DataGrid table) {
		TableModel tableModel = table.getModel();
		tableModel.addTableModelListener(new TableModelListener() {
			 public void tableChanged(TableModelEvent tme) {
			        if (tme.getType() == TableModelEvent.UPDATE) {
			          logInfo("Cell " + tme.getFirstRow() + ", " + tme.getColumn() + " changed."
			              + " The new value: " + tableModel.getValueAt(tme.getFirstRow(), tme.getColumn()));
			        }
			 }
		});
	}
}
