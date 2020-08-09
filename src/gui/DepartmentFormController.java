package gui;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import model.entites.Department;
import model.exceptions.ValidationException;
import model.services.DepartmentService;

public class DepartmentFormController implements Initializable {

	@FXML
	private HBox containerId;
	@FXML
	private Label IdLabel;
	@FXML
	private TextField nameTextField;
	@FXML
	private Button btnSave;
	@FXML
	private Button btnCancel;
	@FXML
	private Label ErrorLabel;

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
		if (entity == null) {
			throw new IllegalStateException("Entidade departamento null");
		}
		IdLabel.setText(String.valueOf(entity.getId()));
		nameTextField.setText(entity.getName());
	}

	private Department getFormData() {
		Department departmentDate = new Department();
		ValidationException exception = new ValidationException("Erro validação");
		departmentDate.setId(Utils.tryParseToInt(IdLabel.getText()));
		departmentDate.setName(nameTextField.getText());

		if (departmentDate.getName() == null || departmentDate.getName().trim().equals("")) {
			exception.addError("name", "Insira um nome para o departamento");
		}

		if (exception.getErrors().size() > 0) {
			throw exception;
		}
		return departmentDate;
	}

	public void handleSaveBtn(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Department is null");
		}
		if (service == null) {
			throw new IllegalStateException("DepartmentService is null");
		}

		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			Utils.currentStage(event).close();
		} catch (DbException e) {
			Alerts.showAlert("DbException", "Erro ao salvar as informações", e.getMessage(), AlertType.ERROR);
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		}
	}

	public void setErrorMessages(Map<String, String> errors) {
		Set<String> fieldsName = errors.keySet();

		if (fieldsName.contains("name")) {
			ErrorLabel.setVisible(true);
			nameTextField.setStyle("-fx-border-width:  0px 0px 1px 0px; -fx-border-color: red;");
			ErrorLabel.setText(errors.get("name"));
		}
	}

	public void handleCancelBtn(ActionEvent event) {
		Utils.currentStage(event).close();
	}
}
