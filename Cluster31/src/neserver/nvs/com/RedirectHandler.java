package neserver.nvs.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cluster.nvs.com.globalData;

public class RedirectHandler extends HandlerTemplate{

	public RedirectHandler(globalData gData) {
		super(gData);
		// TODO Auto-generated constructor stub
	}

	public void getResponse(SSLSocket socket, String paramsString) {
		
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

		disableSslVerification();
		
		String https_url = "https://" + params.get("ip");
		https_url +=  ":" + gData.commonParams.get("webServicePort");		
		https_url += "/" + params.get("page");	
		https_url += "?";			
		
		String redirectParameters = "";
		
		for (Map.Entry<String, String> set : params.entrySet()) {
		    
			if (!(set.getKey().equals("ip") || set.getKey().equals("page"))) {
				
				https_url += set.getKey() + "=" + set.getValue() + "&";
			}
			
		}
		
		https_url = https_url.substring(0, https_url.length() - 1);

		out = doGetRequest(https_url);
		
		return out;

	}
	private String doGetRequest(String https_url) {
		String out = "";
		
	      URL url;
	      try {

	         url = new URL(https_url);
	         HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
		      
	         BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		   	                
		   	       String input;
		   	                
		   	       while ((input = br.readLine()) != null){
		   	          out += input;
		   	       }
		   	       br.close();
	         
	            
	      } catch (Exception e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				out = errors.toString();
				gData.logger.severe(errors.toString());
	      } 
		
		
		
		return out;
	}

	static {
	    disableSslVerification();
	}

	private static void disableSslVerification() {
	    try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            @Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            @Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            @Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
	        }
	        };

	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            @Override
				public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };

	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (KeyManagementException e) {
	        e.printStackTrace();
	    }
	}
}
