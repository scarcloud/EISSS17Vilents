package application.controller.casemodder;

import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import application.Main;
import application.controller.IDashboardController;
import application.util.EURI;
import application.util.ServerRequest;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import model.HttpResponse;

/**
 * Controller-Klasse f�r das Casemodder-Dashboard
 * @author Leonid Vilents
 */
public class DashboardController implements IDashboardController{
		
	@FXML
	private Pane eventPane;
	
	@FXML
	private Label repLabel;
	
	@FXML
	private Pane seekStatusPane;
	
	/*
	 * (non-Javadoc)
	 * @see application.controller.IDashboardController#initialize()
	 */
	@FXML
	@Override
	public void initialize()
	{
		ServerRequest req = new ServerRequest(EURI.DASHBOARD.uri());
		
		HttpResponse res = req.get();
		
		//TODO Events implementieren
		
		try {
			JSONObject content = res.getContent();
			repLabel.setText(Integer.toString(content.getInt("rep")));
			seekStatusPane.setVisible(content.getBoolean("canActivateSeekStatus"));
			
			if(content.getInt("newMessages") > 0) {
				int count = content.getInt("newMessages");
				eventPane.getChildren().add(Main.snippetLoader.getNewMessageDashboardEvent(count));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	protected void activateSeekStatus()
	{
		Alert conf = new Alert(AlertType.CONFIRMATION);
		conf.setTitle("Sponsorsuchstatus setzen");
		conf.setHeaderText("Sponsorsuchstatus setzen?");
		conf.setContentText("Das Setzen des Sponsorsuchstatus bedeutet, dass Sponsoren dies sehen und Dich in ein Team einladen k�nnen. Bist Du sicher, dass Du das m�chtest?");
		
		ButtonType buttonYes = new ButtonType("Ja", ButtonData.YES);
		ButtonType buttonNo = new ButtonType("Nein", ButtonData.NO);
		
		conf.getButtonTypes().setAll(buttonYes, buttonNo);
		
		Optional<ButtonType> result = conf.showAndWait();
		
		if (result.get() == buttonYes) {
			ServerRequest req = new ServerRequest(EURI.SEEKSTATUS.uri());
			
			HttpResponse res = req.get();
			if (res.getStatusCode() == 201) {
				Alert info = new Alert(AlertType.INFORMATION);
				info.setTitle("Dein Suchstatus");
				info.setHeaderText("Suchstatus erfolgreich gesetzt");
				info.setContentText("Du hast Deinen Sponsorsuchstatus erfolgreich gesetzt. Viel Erfolg!");
				info.showAndWait();
			}
		} else {
			return;
		}
	}
}
