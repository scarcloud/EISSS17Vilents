package application.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

import application.Main;
import application.controller.ISignInUpHandling;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.HttpResponse;

/**
 * Handler-Klasse f�r HTTP-Kommunikation
 * @author Leonid Vilents
 */
public class ServerRequest {
	
	private final String HTTP_PREFIX = "http://%s:%s/";
	
	private final String HTTP_METHOD_GET = "GET";
	private final String HTTP_METHOD_POST = "POST";
	@SuppressWarnings("unused")
	private final String HTTP_METHOD_PUT = "PUT";
	@SuppressWarnings("unused")
	private final String HTTP_METHOD_DELETE = "DELETE";
	
	private final String CONNECTION_ERROR = "Verbindungsfehler";
	
	private final String HEADER_400 = "HTTP 400 Bad Request";
	private final String HEADER_401 = "HTTP 401 Permission Denied";
	private final String HEADER_403 = "HTTP 403 Forbidden";
	private final String HEADER_404 = "HTTP 404 Not found";
	
	public static final String ERROR_400 = "Es ist ein Fehler aufgetreten";
	public static final String ERROR_401 = "Authentifizierung fehlgeschlagen";
	public static final String ERROR_403 = "Der Zugriff auf dieses Element wurde verweigert.";
	public static final String ERROR_404 = "Das angeforderte Element wurde nicht gefunden.";
	
	private final String HEADER_500 = "HTTP 500 Internal Server Error";
	
	public static final String ERROR_500 = "Es ist ein interner Fehler aufgetreten.";



	
	private URL url;
	
	/**
	 * Konstruktor
	 * @param url Die Ziel-URL des Requests
	 */
	public ServerRequest(String url)
	{	
		String fullURL = String.format(url, getFullPrefix());
		Main.log(fullURL);
		try{
			this.url = new URL(fullURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * F�hrt einen HTTP POST-Request auf den Server aus.
	 * @param postData die zu �bertragenden POST-Daten
	 * @return Die Antwort des Servers 
	 * @throws IOException 
	 */
	public HttpResponse post(JSONObject postData) throws IOException
	{
		Main.conn = (HttpURLConnection) url.openConnection();
		Main.conn.setRequestMethod(HTTP_METHOD_POST);
		Main.conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		Main.conn.setRequestProperty("Content-length", Integer.toString(postData.length()));
		Main.conn.setDoOutput(true);
			
		OutputStream out = Main.conn.getOutputStream();
	    out.write(postData.toString().getBytes("UTF-8"));
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
	
	/**
	 * Setzt die URL dieser Instanz neu.
	 * @param url neuer URL-String
	 */
	public void setURL(String url)
	{		
		try{
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Behandelt die Serverantwort und gibt ein simplifiziertes HttpResponse-Objekt zur�ck.
	 * @return
	 */
	private HttpResponse handleResponse() throws IOException
	{	
		StringBuilder sb = new StringBuilder();
		try {
			
		    InputStream is = Main.conn.getInputStream();
		    BufferedReader br = new BufferedReader(new InputStreamReader(is));
		    String line = null;
		    while((line = br.readLine() ) != null) {
		        sb.append(line);
		    }
		    HttpResponse res = new HttpResponse(Main.conn.getResponseCode(), sb.toString());
		    return res;
		} catch (IOException a) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(CONNECTION_ERROR);
			switch(Main.conn.getResponseCode()) {
			case 400:
				alert.setHeaderText(HEADER_400);
				alert.setContentText(ERROR_400);
				break;
			case 401:
				alert.setHeaderText(HEADER_401);
				alert.setContentText(ERROR_401);
				break;
			case 403:
				alert.setHeaderText(HEADER_403);
				alert.setContentText(ERROR_403);
				break;
			case 404:
				alert.setHeaderText(HEADER_404);
				alert.setContentText(ERROR_404);
				break;
			case 500:
				alert.setHeaderText(HEADER_500);
				alert.setContentText(ERROR_500);
				break;
			default:
				alert.setHeaderText("Unbekannter Fehler");
				alert.setContentText("Keine Ahnung was hier abgeht...");
				break;
			}
			alert.showAndWait();
			a.printStackTrace();
			// "Sicheres Logout" bei Fehler
			
			String logoutURL = String.format(ISignInUpHandling.LOGOUT_URI, getFullPrefix());
			this.url = new URL(logoutURL);
			Main.sceneLoader.init();
			return this.get();
		} finally {
			Main.conn.disconnect();
			
		}
	}
	
	private String getFullPrefix()
	{
		return String.format(HTTP_PREFIX, Main.SERVER_IP, Main.SERVER_PORT);
	}
}
