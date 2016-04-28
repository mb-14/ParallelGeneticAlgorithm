package com.mb14;


public class Machine {

	static int machineStatus = Macros.MACHINE_IDLE;
	public static Component[] compList;
	static int oldStatus = 0;
	
	public static void setOldStatus(int status) {
		oldStatus = status;
		
	}

	public static int getOldStatus() {
		return oldStatus;
	}
}
