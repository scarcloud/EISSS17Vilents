package application.controller.casemodder;

import org.json.JSONException;
import org.json.JSONObject;

import application.util.ServerRequest;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import model.HttpResponse;

/**
 * 
 * @author L�on
 *
 */
public class DashboardController {
	
	private final String DASHBOARD_STRING = "http://%s:%s/dashboard";
	
	@FXML
	private Pane eventPane;
	
	@FXML
	private Label repLabel;
	
	@FXML
	protected void initialize()
	{
		ServerRequest req = new ServerRequest(DASHBOARD_STRING);
		
		HttpResponse res = req.get();
		
		try {
			JSONObject content = new JSONObject(res.getContent());
			repLabel.setText(Integer.toString(content.getInt("value")));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}