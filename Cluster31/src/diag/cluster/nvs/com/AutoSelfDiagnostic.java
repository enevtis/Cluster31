package diag.cluster.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;

import cluster.nvs.com.globalData;

public class AutoSelfDiagnostic implements Runnable {
	globalData gData = null;

	public AutoSelfDiagnostic(globalData gData) {
		this.gData = gData;
	}

	@Override
	public void run() {

		try {
//			showSockets();
//			checkJVMparameters();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			gData.logger.severe(errors.toString());

		}

	}

private void showSockets() {
	
	gData.logger.info("Socket totals=" + gData.sockets);
}
	
	
	private void checkJVMparameters() {
		
	
		List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
		long total = 0;
		long totalMax = 0;
		for (MemoryPoolMXBean memoryPoolMXBean : pools)
		{
		  if (memoryPoolMXBean.getType() == MemoryType.HEAP)
		  {
		    long peakUsed = memoryPoolMXBean.getPeakUsage().getUsed();
		    long maxUsed = memoryPoolMXBean.getPeakUsage().getMax();
		    gData.saveToLog("Peak used for: " + memoryPoolMXBean.getName() + " is: " + peakUsed, "jvm");
		    total += peakUsed;
		    totalMax += maxUsed;
		  }
		}

		float percent = (float)total / (float) totalMax * 100f;
		gData.saveToLog("Total heap peak used: " + total + " max: " + totalMax + " " + String.format("%.1f", percent) + "%", "jvm");
		

	}

}
