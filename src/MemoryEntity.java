

import java.util.ArrayList;

public class MemoryEntity {
    int index;
	boolean busy;
	int address;
	ArrayList<String> whoNeedsMe;
	public MemoryEntity() {
		busy = false;
		address = 0;
		whoNeedsMe = new ArrayList<String>();
	}
	public String toString() {
		return this.busy + " " + this.address + " ";
	}
	
}
