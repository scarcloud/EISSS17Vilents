package application.controller.snippet;

import application.Main;
import application.util.EBoolean;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SponsoringCandidateController {
	
	private int candidateId;
	
	@FXML
	private Label repLabel;
	
	public void initWithData(int candidateId, int rep)
	{
		this.candidateId = candidateId;
		this.repLabel.setText(String.format("%d R", rep));
	}
	
	@FXML
	protected void viewProfile()
	{
		Main.layoutManager.getProfileTabContent(candidateId, EBoolean.CASEMODDER.value());
	}
}
