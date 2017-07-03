package application.util;

import java.io.IOException;

import application.controller.snippet.ProjectOverviewController;
import javafx.fxml.FXMLLoader;
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
}
