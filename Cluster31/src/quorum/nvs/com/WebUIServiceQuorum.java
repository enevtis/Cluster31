package quorum.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;

import cluster.nvs.com.globalData;
import neserver.nvs.com.NeServer;

public class WebUIServiceQuorum implements Runnable{

	public globalData gData ;
	
	public WebUIServiceQuorum(globalData gData) {
		this.gData = gData;	
		
	}
	
	@Override
	public void run() {
		
		try {
			
			NeServer srv= new NeServer(gData);
			srv.activate();
		}
		catch (Exception e) {
			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
			
		}
		
	}

}
