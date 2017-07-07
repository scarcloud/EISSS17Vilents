package application.controller.sponsor;

import org.json.JSONException;

import application.Main;
import application.controller.IProfileController;
import application.util.EBoolean;
import application.util.conn.EURI;
import application.util.conn.ServerRequest;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import model.HttpResponse;

public class ProfileController implements IProfileController{

	private int profileId;
	
	@FXML
	private ImageView profilePicture;
	
	@FXML
	private Label nameLabel;
	
	@FXML
	private Label corpLabel;
	
	@FXML
	private Label descriptionLabel;
	
	@FXML
	private Button editButton;
	
	@FXML
	private Button contactButton;
	
	
	@Override
	public void initWithData(int id) {
		
		this.profileId = id;
		
		ServerRequest req = new ServerRequest(EURI.PROFILE_SP.uri(), id);
		
		HttpResponse res = req.get();
		try{
			if((!res.getContent().isNull("vorname")) && (!res.getContent().isNull("nachname"))){
				nameLabel.setText(String.format("%s %s", res.getContent().getString("vorname"), res.getContent().getString("nachname")));
			}
			descriptionLabel.setText(res.getContent().getString("beschreibung"));
			if(res.getContent().getBoolean("userOwnsProfile") == EBoolean.PROFILE_OWNER.value()) {
				contactButton.setVisible(false);
			} else {
				editButton.setVisible(false);
			}
			//TODO: Aktivitšten einbinden
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	protected void composeMessageToUser()
	{
		Main.layoutManager.getNewMessageTab(profileId, getName());
	}

	@Override
	public String getName() {
		return nameLabel.getText();
	}

}
