package application.controller.snippet;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import application.Main;
import application.util.CryptoUtil;
import application.util.conn.EURI;
import application.util.conn.ServerRequest;
import application.util.ui.DialogCreator;
import application.util.ui.EDialog;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import model.HttpResponse;

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
	
	@FXML
	private Button markAsReadButton;
	
	private int messageId;
	
	private int senderId;
	
	private boolean messageOpen;
	
	/**
	 * Löscht die Nachricht
	 * @param event
	 */
	@FXML
	protected void deleteMessage(ActionEvent event) {
		Alert conf = DialogCreator.getYesNoConfirmation(
			EDialog.TITLE_DELETE_MESSAGE.text(),
			EDialog.HEADER_DELETE_MESSAGE.text(),
			EDialog.CONTENT_DELETE_MESSAGE.text()
		);
		Optional<ButtonType> result = conf.showAndWait();
		if (result.get() == conf.getButtonTypes().get(0)) {
			ServerRequest req = new ServerRequest(EURI.MESSAGE.uri(), messageId);
			
			HttpResponse res = req.delete();
			if (res.getStatusCode() == 204) {
				((VBox) contentPane.getParent()).getChildren().remove(contentPane);
			}
		} else {
			return;
		}
	}
	
	/**
	 * Ermöglicht das Antworten
	 */
	@FXML
	protected void replyToSender() {
		Main.layoutManager.getNewMessageTab(senderId, senderLabel.getText());
	}
	
	/**
	 * Markiert die Nachricht als gelesen
	 * @param event
	 */
	@FXML
	protected void markAsRead(ActionEvent event) {
		ServerRequest req = new ServerRequest(EURI.MESSAGE.uri(), messageId);
		
		try {
			HttpResponse res = req.put(new JSONObject());
			if (res.getStatusCode() == 204) {
				contentPane.setStyle("-fx-border-color: black;");
				contentPane.getChildren().remove(markAsReadButton);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Zeigt die Nachricht an oder blendet sie aus.
	 */
	@FXML
	protected void toggleMessage() {
		// Beim ersten Toggle ist die Nachricht mindestens ungelesen 
		if(contentPane.getStyle().contains("-fx-background-color: #1abe58;")) {
			contentPane.setStyle("-fx-border-color: black;");
			contentPane.getChildren().remove(markAsReadButton);
		}
		if (messageOpen) {
			contentPane.getChildren().remove(contentPane.getChildren().get(contentPane.getChildren().size() - 1));
			contentPane.setPrefHeight(62);
			messageOpen = false;
			return;
		} else {
			messageOpen = true;
			ServerRequest req = new ServerRequest(EURI.MESSAGE.uri(), messageId);
			JSONObject resContent = req.get().getContent();
			Label messageLabel = new Label();
			messageLabel.setLayoutX(45);
			messageLabel.setLayoutY(45);
			messageLabel.setPrefHeight(90);
			messageLabel.setPrefWidth(500);
			messageLabel.setPadding(new Insets(10));
			messageLabel.setFont(Font.font("Calibri", 14));
			messageLabel.setStyle("-fx-color: black; -fx-background-color: white");
			try {
				messageLabel.setText(CryptoUtil.decodeBase64(resContent.getString("inhalt")));
				contentPane.setPrefHeight(160);
				contentPane.getChildren().add(messageLabel);
				return;
			} catch (JSONException je) {
				messageLabel.setStyle("-fx-color: red");
				messageLabel.setText("JSONException");
				je.printStackTrace();
				return;
			}
		}
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
		this.messageOpen = false;
		if(!isUnread) {
		  contentPane.getChildren().remove(markAsReadButton);
		}
	}
}