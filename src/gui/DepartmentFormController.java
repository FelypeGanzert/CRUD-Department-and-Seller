package gui;

import java.net.URL;
import java.util.ResourceBundle;

import gui.util.Constraints;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class DepartmentFormController implements Initializable {
	
	@FXML private HBox containerId;
	@FXML private Label IdLabel;
	@FXML private TextField nameTextField;
	@FXML private Button btnSave;
	@FXML private Button btnCancel;
	@FXML private Label ErrorLabel;	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initilizeNodes();
	}
	
	private void initilizeNodes() {
		Constraints.setTextFieldMaxLength(nameTextField, 30);
	}
	
	public void handleSaveBtn() {
		if(nameTextField.getText().length() == 0) {
			ErrorLabel.setVisible(true);
			ErrorLabel.setText("Insira um nome para o departamento");
			nameTextField.setStyle("-fx-border-width:  0px 0px 1px 0px; -fx-border-color: red;");
		} else {
			System.out.println("novo departamento ok");
		}
		
	}

}
