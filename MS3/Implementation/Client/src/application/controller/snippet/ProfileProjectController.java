package application.controller.snippet;

import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ProfileProjectController {
	@FXML
	private ImageView projectPicture;
	
	@FXML
	private Label titleLabel;
	
	private int projectId;
	
	public void initializeWithData(int pId, String title)
	{
		this.projectId = pId;
		titleLabel.setText(title);
	}
	
	
	@FXML
	protected void viewProject()
	{
		Main.layoutManager.getSingleProjectTab(this.projectId, Main.layoutManager.getUserId());
	}
}
