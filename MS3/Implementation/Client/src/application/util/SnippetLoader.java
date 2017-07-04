package application.util;

import java.io.IOException;
import java.util.Date;

import application.controller.snippet.MessageOverviewController;
import application.controller.snippet.ProjectOverviewController;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

/**
 * Hilfsklasse f�r das Laden von Snippets
 * @author Leonid Vilents
 */
public class SnippetLoader implements IFXMLLoader{
	
	public Pane getProjectOverviewSnippet(int pId, String title)
	{
		FXMLLoader loader = new FXMLLoader(
			getClass()
			.getResource(FXML_SNIPPET_PATH + "project_overview.fxml")
		);
		try {
			Pane content = loader.load();
			ProjectOverviewController controller = loader.<ProjectOverviewController>getController();
			controller.initWithData(pId, title);
			return content;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public AnchorPane getMessageOverviewSnippet(boolean isUnread, int mId, int sId, Date date, String firstName, String lastName) {
		FXMLLoader loader = new FXMLLoader(
			getClass()
			.getResource(FXML_SNIPPET_PATH + "message_overview.fxml") 
		);
		try {
			AnchorPane content = loader.load();
			MessageOverviewController controller = loader.<MessageOverviewController>getController();
			controller.initWithData(isUnread, mId, sId, date, firstName, lastName);
			return content;
		} catch (LoadException l) {
			l.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}