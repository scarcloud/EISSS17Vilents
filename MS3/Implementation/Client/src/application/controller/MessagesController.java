package application.controller;

import java.time.Instant;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import application.Main;
import application.util.conn.EURI;
import application.util.conn.ServerRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.HttpResponse;

/**
 * Controllerklasse f�r die Nachrichten�bersicht
 * @author Leonid Vilents
 */
public class MessagesController {
	
	@FXML
	private ScrollPane overviewContent;
	
	/**
	 * Ziegt das Erstellungsinterface f�r eine neue Nachricht.
	 * @param event
	 */
	@FXML
	protected void composeNewMessage(ActionEvent event) {
		
	}
	
	/**
	 * Initialisiert die Nachrichten�bersicht mit den Nachrichten des Benutzers.
	 */
	@FXML
	protected void initialize()
	{
		ServerRequest req = new ServerRequest(EURI.MESSAGES.uri());
		
		HttpResponse res = req.get();
		
		if(res.getStatusCode() == 200) {
			try {
				JSONArray content = res.getContent().getJSONArray("messages");
				
				// VBox als Content-Tr�ger f�r einzelne Nachrichten
				VBox box = new VBox();
				
				for(int i = 0; i < content.length(); i++) {
					JSONObject object = content.getJSONObject(i);
					Date date = Date.from(Instant.parse(object.getString("zeitstempel")));
					
					// Snippet f�r Nachricht aus JSON-Objekt erstellen
					AnchorPane messageContent = Main.snippetLoader.getMessageOverviewSnippet(
						object.getInt("ungelesen") == 1 ? true : false,
						object.getInt("n_id"),
						object.getInt("b_id"),
						date,
						object.getString("vorname"),
						object.getString("nachname")
					);
					box.getChildren().add(messageContent);
				}
				
				// VBox in die �bersicht setzen
				overviewContent.setContent(box);
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
		}
	}
}
