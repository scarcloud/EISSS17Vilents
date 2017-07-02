package application.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import application.Main;
import model.HttpResponse;

public class ServerRequest {
	
	private final String HTTP_METHOD_GET = "GET";
	private final String HTTP_METHOD_POST = "POST";
	@SuppressWarnings("unused")
	private final String HTTP_METHOD_PUT = "PUT";
	@SuppressWarnings("unused")
	private final String HTTP_METHOD_DELETE = "DELETE";
	
	private final String CONNECTION_ERROR = "Verbindungsfehler";
	
	private URL url;
	
	public ServerRequest(String url)
	{
		try{
			this.url = new URL(String.format(url,  Main.SERVER_IP, Main.SERVER_PORT));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * F�hrt einen HTTP POST-Request auf den Server aus.
	 * @param postData die zu �bertragenden POST-Daten
	 * @return Die Antwort des Servers 
	 */
	public HttpResponse post(String postData) throws IOException
	{
		Main.conn = (HttpURLConnection) url.openConnection();
		Main.conn.setRequestMethod(HTTP_METHOD_POST);
		Main.conn.setRequestProperty("Content-Type", "application/json");
		Main.conn.setRequestProperty("Content-length", Integer.toString(postData.length()));
		Main.conn.setDoOutput(true);
			
		OutputStreamWriter out = new OutputStreamWriter(Main.conn.getOutputStream());  
	    out.write(postData);
	    out.flush();
	    out.close();
		    
	    return this.handleResponse();
	}
	
	/**
	 * F�hrt einen HTTP GET-Request auf den Server aus.
	 * @return HttpResponse-Objekt
	 */
	public HttpResponse get()
	{
		HttpResponse res = null;
		try {
			Main.conn = (HttpURLConnection) url.openConnection();
			Main.conn.setRequestMethod(HTTP_METHOD_GET);
			res = this.handleResponse();
		} catch (IOException a) {
			a.printStackTrace();
			try {
				res = new HttpResponse(Main.conn.getResponseCode(), CONNECTION_ERROR);
			} catch (IOException e) {
				e.printStackTrace();
				res = new HttpResponse(500, CONNECTION_ERROR);
			}
		}
		return res;
	}
	
	public void setURL(String url)
	{
		try{
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	private HttpResponse handleResponse() throws IOException
	{
		StringBuilder sb = new StringBuilder();
	    InputStream is = Main.conn.getInputStream();
	    BufferedReader br = new BufferedReader(new InputStreamReader(is));
	    String line = null;
	    while((line = br.readLine() ) != null) {
	        sb.append(line);
	    }
	    HttpResponse res = new HttpResponse(Main.conn.getResponseCode(), sb.toString());
	    Main.conn.disconnect();
	    return res;
	}
}