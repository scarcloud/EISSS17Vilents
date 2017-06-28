package application.controller;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import application.util.FormValidator;
import application.util.PasswordUtil;
import application.util.SceneLoader;
import application.util.ServerRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.HttpResponse;

/**
 * Controller-Klasse f�r signup_casemodder.fxml
 * @author L�on
 */
public class SignupCasemodderController implements ISignInUpHandling{
	
	@FXML
	private Hyperlink signUpAsSponsorLink;
	
	@FXML
	private TextField email;
	
	@FXML
	private PasswordField password;
	
	@FXML
	private DatePicker dateOfBirth;
	
	@FXML
	private Label errorLabel;
	
	@FXML
	private Button signupButton;
	
	@FXML
	private Hyperlink backToLoginLink;
	
	@FXML
	protected void handleBackToLoginLink(ActionEvent event)
	{
		SceneLoader.loadScene(event, FILENAME_LOGIN);
	}
	
	@FXML
	protected void handleSignupSponsorLink(ActionEvent event)
	{
		SceneLoader.loadScene(event, FILENAME_SIGNUP_SPONSOR);
	}
	
	@FXML
	protected void handleSignupButton(ActionEvent event)
	{
		FormValidator validator = new FormValidator();
		
		errorLabel.setText(validator.validateEmail(email.getText(), IS_NOT_LOGIN));
		if(!errorLabel.getText().isEmpty()){
			return;
		}
		
		errorLabel.setText(validator.validatePassword(password.getText(), IS_NOT_LOGIN));
		if(!errorLabel.getText().isEmpty()){
			return;
		}
		
		errorLabel.setText(validator.valiateDateOfBirth(dateOfBirth.getValue(), IS_NOT_SPONSOR));
		if(!errorLabel.getText().isEmpty()){
			return;
		}
		
		ServerRequest req = new ServerRequest(SIGNUP_STRING);
		
		try {
			HttpResponse res = req.post(getSignupData());
			switch (res.getStatusCode()) {
			case 500:
				errorLabel.setText(ERROR_500);
			case 201:
			default:
				//TODO: Weiterleitungslogik implementieren
				System.out.println(res.getContent());
				break;
			}
			return;
		} catch (IOException e) {
			e.printStackTrace();
			errorLabel.setText("IOException");
		}
	}
	
	private String getSignupData()
	{
		JSONObject obj = new JSONObject();
		try{
			obj.put("email", email.getText());
			obj.put("password", PasswordUtil.getHash(PasswordUtil.ALGORITHM_SHA256, password.getText()));
			obj.put("type", "casemodder");
			obj.put("dateOfBirth", dateOfBirth.getValue().toString());
			return obj.toString();
		} catch (JSONException e){
			e.printStackTrace();
			return null;
		}
	}
}
