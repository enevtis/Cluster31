package neserver.nvs.com;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import javax.net.ssl.SSLSocket;

import cluster.nvs.com.HttpsRequestor;
import cluster.nvs.com.globalData;
import obj.cluster.nvs.com.CluNode;



public class JournalCollectorHandler extends HandlerTemplate{
	
	String test = "";
	public JournalCollectorHandler(globalData gData) {
		super(gData);

	}

	public void getResponse(SSLSocket socket, String paramsString) {
		
		test = paramsString;
		
		parseParams(paramsString);
		String resp = getPage();

		try {
			
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			
			
		out.write(header200());					
		out.write("Content-Length: " + resp.getBytes("UTF-8").length + "\r\n");
		out.write("\r\n");
		out.write(resp);
		
		out.close();
		
		
		} catch (IOException e) {
	
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}
		
		
	}

	
	public String getPage() {
		String out = "";
		String act = params.get("act");
		
		if (!params.containsKey("act")) {
			return "parameter act not found";
		}
		
		switch (act) {
		
		case "show_common_journal":
			out = commonJournal();
			break;
		
		case "show_local_journal":
			out = localJournal();
			break;		
		
		default:
			
			break;
		
		}
		
		return out;
	}

	public String commonJournal() {
		String out = "";
		

		
		for(CluNode n: gData.nodes) {
			
			String url = "https://" + n.params.get("ip") + ":";
			url += gData.commonParams.get("webServicePort") + "/";
			url += "journal?act=show_local_journal";

			HttpsRequestor r = new HttpsRequestor(gData);
			String resp = r.doGetRequest(url);
			resp = resp.trim();
			
				out += "<b>" + n.id + "</b>&nbsp;";
				out +=  resp + "<hr>";
			
		}

		
		
		return out;
	}

	public String localJournal() {
		String out = "";
		String loggerLast = "";
		String loggerFileName = gData.mainPath + File.separator + "log" + File.separator + "logger.trc";
		
		if (gData.journalText == null) gData.journalText = "";
		
		out = "<p style='color:blue;display:inline;'>(" + gData.getShortCurrentTime() + ")</p> " + gData.journalText;
		
//		out = "<p style='color:blue;display:inline;'>(" + gData.getShortCurrentTime() + ")</p> " + printLastNLines(loggerFileName,5);
		
		
		return out;
	}

	private static String printLastNLines(String filePath, int n) {
	    String out = "";
		File file = new File(filePath);
	    StringBuilder builder = new StringBuilder();
	    try {
	        RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
	        long pos = file.length() - 1;
	        randomAccessFile.seek(pos);

	        for (long i = pos - 1; i >= 0; i--) {
	            randomAccessFile.seek(i);
	            char c = (char) randomAccessFile.read();
	            if (c == '\n') {
	                n--;
	                if (n == 0) {
	                    break;
	                }
	            }
	            
	            builder.append(c);

	        }
	        builder.reverse();
	        out=builder.toString();
	        
	
	        
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	
	    return out;
	}
}
