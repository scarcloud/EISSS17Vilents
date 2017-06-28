package application.controller;

import java.io.File;
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
import javafx.stage.FileChooser;
import model.HttpResponse;

public class SignupSponsorController implements ISignInUpHandling {
	
	@FXML
	private Hyperlink signupAsCasemodderLink;
	
	@FXML
	private TextField email;
	
	@FXML
	private PasswordField password;
	
	@FXML
	private DatePicker dateOfBirth;
	
	@FXML
	private Label errorLabel;
	
	@FXML
	private Button fileUploadButton;
	
	@FXML
	private TextField filePath;
	
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
	protected void handleSignupCasemodderLink(ActionEvent event)
	{
		SceneLoader.loadScene(event, FILENAME_SIGNUP_CASEMODDER);
	}
	
	@FXML
	protected void handleFileChoice(ActionEvent event)
	{
		final FileChooser fileChooser = getFileChooser();
		File file = fileChooser.showOpenDialog(SceneLoader.getStage(event));
        if (file != null) {
            filePath.setText(file.getAbsolutePath());
        }
	}
	
	@FXML
	protected void handleSignupButton(ActionEvent event)
	{
		FormValidator validator = new FormValidator();
		
		errorLabel.setText(validator.validateEmail(email.getText(), IS_NOT_LOGIN));
		if (!errorLabel.getText().isEmpty()) {
			return;
		}
		
		errorLabel.setText(validator.validatePassword(password.getText(), IS_NOT_LOGIN));
		if (!errorLabel.getText().isEmpty()) {
			return;
		}
		
		errorLabel.setText(validator.valiateDateOfBirth(dateOfBirth.getValue(), IS_SPONSOR));
		if (!errorLabel.getText().isEmpty()) {
			return;
		}
		
		errorLabel.setText(validator.validateFile(filePath.getText()));
		if (!errorLabel.getText().isEmpty()) {
			return;
		}
		
		ServerRequest req = new ServerRequest(SIGNUP_STRING);
		
		//TODO: File-Upload verarbeiten
		
		try {
			HttpResponse res = req.post(getSignupData());
			switch (res.getStatusCode()) {
			case 201:
			default:
				System.out.println(res.toString());
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
			obj.put("type", "sponsor");
			obj.put("dateOfBirth", dateOfBirth.getValue().toString());
			return obj.toString();
		} catch (JSONException e){
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gibt ein vorkonfiguriertes FileChooser-Objekt zur�ck.
	 * @return das vorkonfigurierte FileChooser-Objekt
	 */
	private FileChooser getFileChooser()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Akkreditierungsdatei ausw�hlen");
		fc.setInitialDirectory(new File(System.getProperty("user.home")));
		fc.getExtensionFilters().addAll(
			new FileChooser.ExtensionFilter("Alle Dateien (*.*)", "*.*"),
			new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
		);
		return fc;
	}
}
