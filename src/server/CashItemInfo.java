package server;

/**
 *
 * @author Lerk
 */
public class CashItemInfo {
	private int itemId;
	private int count;
	private int price;
        private int period;
        private int donor;
	
	public CashItemInfo(int itemId, int count, int price, int period, int donor) {
		this.itemId = itemId;
		this.count = count;
		this.price = price;
                this.period = period;
                this.donor = donor;
	}
	
	public int getId() {
		return itemId;
	}
	
	public int getCount() {
		return count;
	}
	
	public int getPrice() {
		return price;
	}
        
        public int getPeriod() {
            return period;
        }
        
        public int getDonor() {
            return donor;
        }
}