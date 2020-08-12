package gui;

import java.net.URL;
import java.time.ZoneId;
import java.util.ArrayList;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import model.entites.Seller;
import model.exceptions.ValidationException;
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
	
	@FXML private Label labelErrorName;
	@FXML private Label labelErrorEmail;
	@FXML private Label labelErrorBirthDate;
	@FXML private Label labelErrorBaseSalary;

	private Seller entity;
	private SellerService service;
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

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

	public void setSellerEntity(Seller entity) {
		this.entity = entity;
		if(this.entity.getId() != null) {
			containerId.setVisible(true);
		}
	}

	public void setSellerService(SellerService service) {
		this.service = service;
	}

	public void updateFormData() {
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
	}

	private Seller getFormData() {
		Seller sellerData = new Seller();
		ValidationException exception = new ValidationException("Erro validação");
		sellerData.setId(Utils.tryParseToInt(IdLabel.getText()));
		sellerData.setName(nameTextField.getText());

		if (sellerData.getName() == null || sellerData.getName().trim().equals("")) {
			exception.addError("name", "Insira um nome para o vendedor");
		}

		if (exception.getErrors().size() > 0) {
			throw exception;
		}
		return sellerData;
	}

	public synchronized void handleSaveBtn(ActionEvent event) {
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
			Alerts.showAlert("DbException", "Erro ao salvar as informações", e.getMessage(), AlertType.ERROR);
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		}
	}

	public void setErrorMessages(Map<String, String> errors) {
		Set<String> fieldsName = errors.keySet();

		if (fieldsName.contains("name")) {
			labelErrorName.setVisible(true);
			nameTextField.setStyle("-fx-border-width:  0px 0px 1px 0px; -fx-border-color: red;");
			labelErrorName.setText(errors.get("name"));
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
