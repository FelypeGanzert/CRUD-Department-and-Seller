package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import application.Main;
import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import icons.Icons;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.entites.Department;
import model.entites.Seller;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerListController implements Initializable, DataChangeListener{

	@FXML private VBox mainVBox;
	@FXML private Button btnNewSeller;
	@FXML private Label lblSellerTotal;
	@FXML private TextField txtNameFilter;
	@FXML private CheckBox checkBoxExactNameFilter;
	@FXML private ComboBox<Department> comboBoxDepartmentFilter;
	@FXML private TableView<Seller> tableViewSellers;
	@FXML private TableColumn<Seller, Integer> tableColumnId;
	@FXML private TableColumn<Seller, String> tableColumnNome;
	@FXML private TableColumn<Seller, String> tableColumnEmail;
	@FXML private TableColumn<Seller, Date> tableColumnBirthDate;
	@FXML private TableColumn<Seller, Double> tableColumnBaseSalary;
	@FXML private TableColumn<Seller, String> tableColumnDepartmentName;
	
	@FXML private TableColumn<Seller, Seller> tableColumnEdit;
	@FXML private TableColumn<Seller, Seller> tableColumnDelete;
	
	private SellerService sellerService;
	private DepartmentService departmentService;
	private ObservableList<Seller> sellerObsList;
	private ObservableList<Department> departmentObsList;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeNodes();
		Constraints.setTextFieldMaxLength(txtNameFilter, 40);
		txtNameFilter.textProperty().addListener((observable, oldValue, newValue) -> {
			filterSellers();
		});
		checkBoxExactNameFilter.selectedProperty().addListener((observable, oldValue, newValue) -> {
			filterSellers();
		});
		comboBoxDepartmentFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
			filterSellers();
		});
		
	}
	
	private void filterSellers() {
		List<Seller> filteredList;
		String value = txtNameFilter.getText();
		boolean exact = checkBoxExactNameFilter.isSelected();
		Department department = comboBoxDepartmentFilter.getValue();
		if(exact) {
			filteredList = sellerObsList.stream()
					.filter(seller -> seller.getName().toUpperCase().startsWith(value.toUpperCase()))
					.collect(Collectors.toList());
		} else {
			filteredList = sellerObsList.stream()
					.filter(seller -> seller.getName().toUpperCase().contains(value.toUpperCase()))
					.collect(Collectors.toList());
		}
		if(department != null && department.getId() != null) {
			filteredList = filteredList.stream()
					.filter(seler -> seler.getDepartment().equals(department))
					.collect(Collectors.toList());
		}
		ObservableList<Seller> filteredObsList =  FXCollections.observableArrayList(filteredList);
		tableViewSellers.setItems(filteredObsList);
		tableViewSellers.refresh();
	}
	
	public void setDepartmentsToComboBoxFilter(Department selectedDepartment) {
		if(this.departmentService == null) {
			throw new IllegalStateException("DepartmentService is null to add to comboBox");
		}
		List<Department> list = new ArrayList<>();
		list.add(new Department(null, null));
		list.addAll(departmentService.findAdll());
		departmentObsList = FXCollections.observableArrayList(list);
		comboBoxDepartmentFilter.setItems(departmentObsList);
		comboBoxDepartmentFilter.setConverter(new StringConverter<Department>() {
			@Override
			public String toString(Department object) {
				return object.getName();
			}
			@Override
			public Department fromString(String string) {
				return null;
			}
		});
		if(selectedDepartment != null) {
			comboBoxDepartmentFilter.setValue(selectedDepartment);
		}
	}
	
	public void handleNewSeller(ActionEvent event) {
		createSellerDialogForm(new Seller(), "/gui/SellerForm.fxml", Utils.currentStage(event));
	}
		
	private void initializeNodes() {
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnNome.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		tableColumnBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
		Utils.formatTableColumnDate(tableColumnBirthDate, "dd/MM/yyyy");
		tableColumnBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
		Utils.formatTableColumnDoubleCurrency(tableColumnBaseSalary);
		tableColumnDepartmentName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartment().getName()));
		// Edit buttons
		initButtons(tableColumnEdit, 15, Icons.PEN_SOLID, "grayIcon", (seller, event) -> {
			createSellerDialogForm(seller, "/gui/SellerForm.fxml", Utils.currentStage(event));
		});
		// Delete buttons
		initButtons(tableColumnDelete, 15, Icons.TRASH_SOLID, "redIcon", (seller, event) -> {
			removeEntity(seller);
		});
		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewSellers.prefHeightProperty().bind(stage.heightProperty());
	}
	
	private <T, T2> void initButtons(TableColumn<Seller, Seller> tableColumn,
			int size, String svgIcon, String className, BiConsumer<Seller, ActionEvent> buttonAction) {

		tableColumn.setMinWidth(size + 20);
		tableColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumn.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = Utils.createIconButton(svgIcon, size, className);

			@Override
			protected void updateItem(Seller seller, boolean empty) {
				super.updateItem(seller, empty);
				if (seller == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> {
					buttonAction.accept(seller, event);
				});
			}
		});
	}
	
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}
	
	public void setDepartmentService(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}
	
	public void updateTableView() {
		if(sellerService == null) {
			throw new IllegalStateException("Seller service not initialized");
		}
		List<Seller> list = sellerService.findAdll();
		list.sort((p1, p2) -> p1.getName().toUpperCase().compareTo(p2.getName().toUpperCase()));
		sellerObsList = FXCollections.observableArrayList(list);
		tableViewSellers.setItems(sellerObsList);
		tableViewSellers.refresh();
		lblSellerTotal.setText("(Total de: " + Utils.pointSeparator(sellerObsList.size()) + ")");
	}
	
	private void createSellerDialogForm(Seller obj, String absolutePath, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absolutePath));
			AnchorPane anchorPane = loader.load();
			
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Informações do Vendedor");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(parentStage);
			dialogStage.setResizable(false);
			
			SellerFormController controller = loader.getController();
			controller.setSellerEntity(obj);
			controller.setSellerService(new SellerService());
			controller.subscribeDataChangeListener(this);
			controller.updateFormData();
			
			Scene departmentFormScene = new Scene(anchorPane);
			departmentFormScene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
			dialogStage.setScene(departmentFormScene);
			
			dialogStage.showAndWait();
		} catch (IOException e) {
			Alerts.showAlert("IOException", "Erro ao exibir tela", e.getMessage(), AlertType.ERROR);
			e.printStackTrace();
		}
	}
	
	private void removeEntity(Seller entity) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Deletar vendedor");
		alert.setHeaderText("Id: " + entity.getId() + " - " + entity.getName());
		alert.setContentText("Tem certeza que deseja deletar?");
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {
			try {
			sellerService.delete(entity);
				onDataChanged();
			} catch (DbException e) {
				Alerts.showAlert("DbException", "Erro ao deletar", e.getMessage(), AlertType.ERROR);
			}
		}
	}

	@Override
	public void onDataChanged() {
		updateTableView();
		filterSellers();
	}

}
