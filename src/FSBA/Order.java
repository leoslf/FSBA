package FSBA;

/**
 * Order object
 * @author leosin
 *
 */
public class Order {
	/** The order number */
	private int orderNo = -1;
	
	/** 
	 * Mutator of orderNo
	 * @param orderNo The order number to be assigned.
	 */
	public void setOrderNo(int orderNo) {
		this.orderNo = orderNo;
	}
	
	/** 
	 * Accessor of order No
	 * @return The order number stored.
	 */
	public int getOrderNo() {
		return orderNo;
	}
}