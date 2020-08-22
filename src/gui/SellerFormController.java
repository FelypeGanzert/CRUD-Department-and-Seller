package gui;

import java.net.URL;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import model.entites.Department;
import model.entites.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	@FXML private HBox containerId;
	@FXML private Label IdLabel;
	@FXML private TextField nameTextField;
	@FXML private TextField emailTextField;
	@FXML private DatePicker birthDateDatePicker;
	@FXML private TextField baseSalaryTextField;
	@FXML private Button btnSave;
	@FXML private Button btnCancel;
	@FXML private ComboBox<Department> comboBoxDepartment;
	
	@FXML private Label labelErrorName;
	@FXML private Label labelErrorEmail;
	@FXML private Label labelErrorBirthDate;
	@FXML private Label labelErrorBaseSalary;
	@FXML private Label labelErrorDepartment;

	private Seller entity;
	private SellerService service;
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
	
	private DepartmentService departmentService;
	private ObservableList<Department> departmentObsList;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initilizeNodes();
	}

	private void initilizeNodes() {
		Constraints.setTextFieldMaxLength(nameTextField, 70);
		Constraints.setTextFieldDouble(baseSalaryTextField);
		Constraints.setTextFieldMaxLength(emailTextField, 60);
		Utils.formatDatePicker(birthDateDatePicker, "dd/MM/yyyy");
	}
	
	public void setDepartmentToComboBox(Department department) {
		Utils.setDepartmentsToComboBox(comboBoxDepartment, department,
				departmentService, departmentObsList);
	}

	public void setSellerEntity(Seller entity) {
		this.entity = entity;
		if(this.entity.getId() != null) {
			containerId.setVisible(true);
		}
	}

	public void setSellerService(SellerService service) {
		this.service = service;
	}
	
	public void setDepartmentService(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	public void updateFormData() {
		setDepartmentToComboBox(new Department());
		if (entity == null) {
			throw new IllegalStateException("Entidade seller null");
		}
		IdLabel.setText(String.valueOf(entity.getId()));
		nameTextField.setText(entity.getName());
		emailTextField.setText(entity.getEmail());
		Locale.setDefault(new Locale("pt-BR"));
		baseSalaryTextField.setText(String.format("%.2f", entity.getBaseSalary()).replace(".", ","));
		if(entity.getBirthDate() != null) {
			birthDateDatePicker.setValue(entity.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		}
		if(entity.getDepartment() != null) {
			comboBoxDepartment.getSelectionModel().select(entity.getDepartment());
		}
	}

	private Seller getFormData() {
		Seller sellerData = new Seller();
		ValidationException exception = new ValidationException("Erro validação");
		
		sellerData.setId(Utils.tryParseToInt(IdLabel.getText()));
		sellerData.setName(nameTextField.getText());
		if (sellerData.getName() == null || sellerData.getName().trim().equals("")) {
			exception.addError("name", "Insira um nome");
		}
		
		sellerData.setEmail(emailTextField.getText());
		if (sellerData.getEmail() == null || sellerData.getEmail().trim().equals("")
				|| !sellerData.getEmail().contains("@")) {
			exception.addError("email", "Insira um email válido");
		}
		
		if (birthDateDatePicker.getValue() != null) {
			sellerData.setBirthDate(
				Date.from(birthDateDatePicker.getValue().
				atStartOfDay(ZoneId.systemDefault()).toInstant()));
		} else {
			exception.addError("birthDate", "Insira um nascimento");			
		}
		
		if(baseSalaryTextField.getText() == null || baseSalaryTextField.getText().trim().equals("")) {
			exception.addError("baseSalary", "Insira um salário");
		} else {
			sellerData.setBaseSalary(Utils.tryParseToDouble(baseSalaryTextField.getText().replace(",", ".")));
		}
		
		sellerData.setDepartment(comboBoxDepartment.getValue());
		if (sellerData.getDepartment() == null  || sellerData.getDepartment().getId() == null) {
			exception.addError("department", "Selecione um departamento");
		}
		
		if (exception.getErrors().size() > 0) {
			throw exception;
		}
		return sellerData;
	}

	public synchronized void handleSaveBtn(ActionEvent event) {
		resetErrorMessages();
		if (entity == null) {
			throw new IllegalStateException("Seller is null");
		}
		if (service == null) {
			throw new IllegalStateException("SellerService is null");
		}

		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			Utils.currentStage(event).close();
			notifyDataChangeListeners();
		} catch (DbException e) {
			e.printStackTrace();
			Alerts.showAlert("DbException", "Erro ao salvar as informações", e.getMessage(), AlertType.ERROR);
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		}
	}
	
	private void resetErrorMessages() {
		nameTextField.getStyleClass().remove("border-error");
		emailTextField.getStyleClass().remove("border-error");
		birthDateDatePicker.getStyleClass().remove("border-error");
		baseSalaryTextField.getStyleClass().remove("border-error");
		comboBoxDepartment.getStyleClass().remove("border-error");
		
		labelErrorName.setVisible(false);
		labelErrorEmail.setVisible(false);
		labelErrorBirthDate.setVisible(false);
		labelErrorBaseSalary.setVisible(false);
		labelErrorDepartment.setVisible(false);
	}

	public void setErrorMessages(Map<String, String> errors) {
		Set<String> fieldsName = errors.keySet();

		if (fieldsName.contains("name")) {
			labelErrorName.setVisible(true);
			nameTextField.getStyleClass().add("border-error");
			labelErrorName.setText(errors.get("name"));
		}
		
		if (fieldsName.contains("email")) {
			labelErrorEmail.setVisible(true);
			emailTextField.getStyleClass().add("border-error");
			labelErrorEmail.setText(errors.get("email"));
		}
		
		if (fieldsName.contains("birthDate")) {
			labelErrorBirthDate.setVisible(true);
			birthDateDatePicker.getStyleClass().add("border-error");
			labelErrorBirthDate.setText(errors.get("birthDate"));
		}
		
		if (fieldsName.contains("baseSalary")) {
			labelErrorBaseSalary.setVisible(true);
			baseSalaryTextField.getStyleClass().add("border-error");
			labelErrorBaseSalary.setText(errors.get("baseSalary"));
		}
		
		if (fieldsName.contains("department")) {
			labelErrorDepartment.setVisible(true);
			comboBoxDepartment.getStyleClass().add("border-error");
			labelErrorDepartment.setText(errors.get("department"));
		}
	}

	public void handleCancelBtn(ActionEvent event) {
		Utils.currentStage(event).close();
	}
	
	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}
	
	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}
}
