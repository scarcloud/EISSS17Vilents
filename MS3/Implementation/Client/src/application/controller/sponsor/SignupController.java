package application.controller.sponsor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONException;
import org.json.JSONObject;

import application.Main;
import application.util.EBoolean;
import application.util.FormValidator;
import application.util.CryptoUtil;
import application.util.conn.EURI;
import application.util.conn.ServerRequest;
import application.util.ui.EFXML;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import model.HttpResponse;

/**
 * Controller f�r die Sponsoren-Registrierungsmaske.
 * @author Leonid Vilents
 */
public class SignupController {
	
	private File file;
	
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
	
	/**
	 * Zur Login-Maske wechseln
	 */
	@FXML
	protected void handleBackToLoginLink()
	{
		Main.sceneLoader.loadScene(EFXML.LOGIN.fxml());
	}
	
	/**
	 * Zur Casemodder-Registrierungsmaske wechseln
	 */
	@FXML
	protected void handleSignupCasemodderLink()
	{
		Main.sceneLoader.loadScene(EFXML.CM_SIGNUP.fxml());
	}
	
	/**
	 * Auswahlbildschirm f�r Datei-Upload zeigen, nach erfolgreicher Auswahl den absoluten Dateipfad anzeigen
	 */
	@FXML
	protected void handleFileChoice()
	{
		final FileChooser fileChooser = getFileChooser();
		File file = fileChooser.showOpenDialog(Main.sceneLoader.getPrimaryStage());
        if (file != null) {
        	this.file = file;
            filePath.setText(file.getAbsolutePath());
        }
	}
	
	/**
	 * Registrierungvorgang
	 */
	@FXML
	protected void handleSignupButton()
	{
		FormValidator validator = new FormValidator();
		
		errorLabel.setText(validator.validateEmail(email.getText(), EBoolean.NOT_LOGIN.value()));
		if (!errorLabel.getText().isEmpty()) {
			return;
		}
		
		errorLabel.setText(validator.validatePassword(password.getText(), EBoolean.NOT_LOGIN.value()));
		if (!errorLabel.getText().isEmpty()) {
			return;
		}
		
		errorLabel.setText(validator.valiateDateOfBirth(dateOfBirth.getValue(), EBoolean.NOT_CASEMODDER.value()));
		if (!errorLabel.getText().isEmpty()) {
			return;
		}
		
		errorLabel.setText(validator.validateFile(filePath.getText()));
		if (!errorLabel.getText().isEmpty()) {
			return;
		}
		
		
		ServerRequest req = new ServerRequest(EURI.SIGNUP.uri());
		
		try {
			HttpResponse res = req.post(getSignupData());
			switch (res.getStatusCode()) {
			case 201:
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Registrierung");
				alert.setHeaderText("Registrierung erfolgreich!");
				alert.setContentText("Sie k�nnen Sich jetzt mit ihren Benutzerdaten einloggen.");
				alert.showAndWait();
				Main.sceneLoader.loadScene(EFXML.LOGIN.fxml());
			default:
				break;
			}
			return;
		} catch (IOException e) {
			e.printStackTrace();
			errorLabel.setText("IOException");
		} catch (NullPointerException n) {
			n.printStackTrace();
			errorLabel.setText("NullPointerException");
			return;
		}
	}
	
	/**
	 * Verpackt die angegebenen Daten aus der Maske in einen JSON-String.
	 * @return JSONObject als String, oder null, falls ein Fehler aufgetreten ist.
	 */
	private JSONObject getSignupData()
	{
		JSONObject obj = new JSONObject();
		try{
			obj.put("email", email.getText());
			obj.put("password", CryptoUtil.getHash(CryptoUtil.ALGORITHM_SHA256, password.getText()));
			obj.put("type", "sponsor");
			obj.put("dateOfBirth", dateOfBirth.getValue().toString());
			obj.put("accreditFile", convertFileToString());
			return obj;
		} catch (JSONException e){
			e.printStackTrace();
			return null;
		} catch (IOException e) {
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
	
	/**
	 * Konvertiert eine Datei in einen Base64-String.
	 * @param filePath der absolute Dateipfad
	 * @return
	 */
	private String convertFileToString() throws IOException{
		
		byte[] bytes = Files.readAllBytes(this.file.toPath());
		return new String(DatatypeConverter.printBase64Binary(bytes));
	}
}
