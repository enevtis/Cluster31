package cluster.nvs.com;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



public class HttpsRequestor {
	public globalData gData;
	
	public HttpsRequestor(globalData gData) {
		this.gData = gData;
		
	}
	
	
	
	public String doGetRequest(String https_url) {
		String out = "";
		
		Authenticator.setDefault(new Authenticator() {

		    @Override
		    protected PasswordAuthentication getPasswordAuthentication() {          
		        String user = gData.commonParams.get("webAdminUser");
		        String password = gData.commonParams.get("webAdminPassword");
		    	return new PasswordAuthentication(user, password.toCharArray());
		    }
		});
		disableSslVerification();
		

		HttpsURLConnection con = null;
		URL url;

		try {

	         url = new URL(https_url);
	         con = (HttpsURLConnection)url.openConnection();
	         con.setConnectTimeout(gData.pingTimeout); 
	         
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
				
				gData.logger.severe(https_url + "\n" + errors.toString());
	      } 
		
		
		
		return out;
	}
	public boolean checkNode(String ip) {
		boolean out = false;
		String https_url = "https://" + ip + ":" + gData.commonParams.get("webServicePort");
		
		
		
		


		HttpsURLConnection con = null;
		URL url;

		
		try {

	         url = new URL(https_url);
	         
	         con = (HttpsURLConnection) url.openConnection();


	 		Authenticator.setDefault(new Authenticator() {

			    @Override
			    protected PasswordAuthentication getPasswordAuthentication() {          
			        String user = gData.commonParams.get("webAdminUser");
			        String password = gData.commonParams.get("webAdminPassword");
			    	return new PasswordAuthentication(user, password.toCharArray());
			    }
			});
			
	 		disableSslVerification();
	         con.setConnectTimeout(gData.pingTimeout); //set timeout to 5 seconds
	         
	         
	         out = con.getResponseCode() == HttpURLConnection.HTTP_OK ? true: false;
	         
	            
	      } catch (Exception e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				gData.logger.severe(errors.toString());
	      } finally {
	    	  
	    	  con.disconnect();
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
