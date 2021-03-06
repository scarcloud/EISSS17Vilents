package application.util.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;

/**
 * Hilfsklasse f�r das Erstellen von Dialogen
 * @author Leonid Vilents
 */
public class DialogCreator {

	/**
	 * Gibt einen "Ja, Nein"-Best�tigungsdialog zur�ck
	 * @param title		Dialogtitel
	 * @param header	Dialog-Headertext
	 * @param content	Dialog-Text
	 * @return			Dialog-Objekt
	 */
	public static Alert getYesNoConfirmation(String title, String header, String content)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		
		ButtonType buttonYes = new ButtonType("Ja", ButtonData.YES);
		ButtonType buttonNo = new ButtonType("Nein", ButtonData.NO);
		
		alert.getButtonTypes().setAll(buttonYes, buttonNo);
		
		return alert;
	}
	
	/**
	 * Gibt einen Informationsdialog zur�ck
	 * @param title		Dialogtitel
	 * @param header	Dialog-Headertext
	 * @param content	Dialog-Text
	 * @return			Dialog-Objekt
	 */
	public static Alert getInfo(String title, String header, String content)
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		
		return alert;
	}
}
