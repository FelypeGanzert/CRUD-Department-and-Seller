package gui;

import java.net.URL;
import java.util.ResourceBundle;

import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import model.entites.Department;
import model.services.DepartmentService;

public class DepartmentFormController implements Initializable {
	
	@FXML private HBox containerId;
	@FXML private Label IdLabel;
	@FXML private TextField nameTextField;
	@FXML private Button btnSave;
	@FXML private Button btnCancel;
	@FXML private Label ErrorLabel;	
	
	private Department entity;
	private DepartmentService service;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initilizeNodes();
	}
	
	private void initilizeNodes() {
		Constraints.setTextFieldMaxLength(nameTextField, 30);
	}
	
	public void setDepartmentEntity(Department entity) {
		this.entity = entity;
	}
	
	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}
	
	public void updateFormData() {
		if(entity == null) {
			throw new IllegalStateException("Entidade departamento null");
		}
		IdLabel.setText(String.valueOf(entity.getId()));
		nameTextField.setText(entity.getName());
	}
	
	private Department getFormData() {
		Department departmentDate = new Department();
		departmentDate.setId(Utils.tryParseToInt(IdLabel.getText()));
		departmentDate.setName(nameTextField.getText());
		return departmentDate;
	}
	
	public void handleSaveBtn(ActionEvent event) {
		if(entity == null) {
			throw new IllegalStateException("Department is null");
		}
		if(service == null) {
			throw new IllegalStateException("DepartmentService is null");
		}
		
		entity = getFormData();
		if(entity.getName() == null || entity.getName().length() == 0) {
			ErrorLabel.setVisible(true);
			ErrorLabel.setText("Insira um nome para o departamento");
			nameTextField.setStyle("-fx-border-width:  0px 0px 1px 0px; -fx-border-color: red;");
		} else {
			service.saveOrUpdate(entity);
			Utils.currentStage(event).close();
		}
	}
}
