package auto.cluster.nvs.com;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;

import cluster.nvs.com.globalData;

public class AutoSelfStatusWatch implements Runnable{
	globalData gData = null;
	
	public AutoSelfStatusWatch(globalData gData) {
		this.gData = gData;
	}

	@Override
	public void run() {
	
		checkMasterStatus();
//		checkMemory();
		
		
		
	}

private void checkMasterStatus() {
	
	String fullPath = gData.mainPath + File.separator + "tmp" + File.separator;
	
	File f = new File(fullPath + "master");
	
	gData.IamMaster = f.exists() ? true: false;

}
private void checkMemory() {
	
	for (MemoryPoolMXBean mpBean: ManagementFactory.getMemoryPoolMXBeans()) {
	    if (mpBean.getType() == MemoryType.HEAP) {
	        
	    	gData.logger.info(mpBean.getName() + " " + mpBean.getUsage() );
	    	
	    }
	}
	
	
}
}
