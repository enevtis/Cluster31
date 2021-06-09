package cluster.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;

import neserver.nvs.com.NeServer;

public class WebUIService implements Runnable{

	public globalData gData ;
	
	public WebUIService(globalData gData) {
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
