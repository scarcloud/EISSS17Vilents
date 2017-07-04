package application.controller.snippet;

import java.text.DateFormat;
import java.util.Date;

import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * Controllerklasse für Nachrichtensnippets in der Nachrichtenübersicht
 * @author Leonid Vilents
 */
public class MessageOverviewController {
	
	@FXML
	private AnchorPane contentPane;
	
	@FXML
	private Label dateLabel;
	
	@FXML
	private Label senderLabel;
	
	private int messageId;
	
	private int senderId;
	
	/**
	 * Löscht die Nachricht
	 * @param event
	 */
	@FXML
	protected void deleteMessage(ActionEvent event) {
		//TODO: implementieren
	}
	
	/**
	 * Ermöglicht das Antworten
	 * @param event
	 */
	@FXML
	protected void replyToSender(ActionEvent event) {
		//TODO: implementieren
	}
	
	/**
	 * Markiert die Nachricht als gelesen
	 * @param event
	 */
	@FXML
	protected void markAsRead(ActionEvent event) {
		//TODO: implementieren
	}
	
	/**
	 * Zeigt die Nachricht an.
	 */
	@FXML
	protected void showMessage() {
		Main.log("Triggered");
	}
	
	/**
	 * Initialisiert das Snippet mit Daten
	 * @param isUnread 	Ungelesen-Flag
	 * @param mId		Nachricht-ID
	 * @param sId		Benutzer-ID des Absenders
	 * @param date		Datum der Nachricht
	 * @param firstName	Vorname des Absenders
	 * @param lastName	Nachname des Absenders
	 */
	public void initWithData(boolean isUnread, int mId, int sId, Date date, String firstName, String lastName) {
		String senderLabelstr = String.format("%s %s", firstName, lastName);
		String styleStr = "-fx-border-color: black; " + (isUnread ? "-fx-background-color: #1abe58;" : "");
		
		contentPane.setStyle(styleStr);

		this.messageId = mId;
		this.senderId = sId;
		this.dateLabel.setText(DateFormat.getDateInstance().format(date));
		this.senderLabel.setText(senderLabelstr);
	}
}