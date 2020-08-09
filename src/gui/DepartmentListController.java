package gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import application.Main;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entites.Department;
import model.services.DepartmentService;
import model.services.SellerService;

public class DepartmentListController implements Initializable, DataChangeListener{

	@FXML private VBox mainVBox;
	@FXML private Button btnNewDepartment;
	@FXML private Button btnListSellers;
	@FXML private TableView<Department> tableViewDepartments;
	@FXML private TableColumn<Department, String> tableColumnNome;
	@FXML private TableColumn<Department, Integer> tableColumnVendedores;
	
	private DepartmentService departmentService;
	private SellerService sellerService;
	private ObservableList<Department> departmentObsList;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeNodes();
		
	}
	
	public void handleNewDepartment(ActionEvent event) {
		createDepartmentDialogForm(new Department(), "/gui/DepartmentForm.fxml", Utils.currentStage(event));
	}
		
	private void initializeNodes() {
		tableColumnNome.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnVendedores.setCellValueFactory(cellData -> new SimpleObjectProperty<>(sellerService.	quantityByDepartment(cellData.getValue())));		
		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewDepartments.prefHeightProperty().bind(stage.heightProperty());
	}
	

	public void setDepartmentService(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}
	
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}
	
	public void updateTableView() {
		if(departmentService == null) {
			throw new IllegalStateException("Department service not initialized");
		}
		if(sellerService == null) {
			throw new IllegalStateException("Seller service not initialized");
		}
		List<Department> list = departmentService.findAdll();
		list.sort((p1, p2) -> p1.getName().toUpperCase().compareTo(p2.getName().toUpperCase()));
		departmentObsList = FXCollections.observableArrayList(list);
		tableViewDepartments.setItems(departmentObsList);
	}
	
	private void createDepartmentDialogForm(Department obj, String absolutePath, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absolutePath));
			AnchorPane anchorPane = loader.load();
			
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Informações do Departamento");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(parentStage);
			dialogStage.setResizable(false);
			
			DepartmentFormController controller = loader.getController();
			controller.setDepartmentEntity(obj);
			controller.setDepartmentService(new DepartmentService());
			controller.subscribeDataChangeListener(this);
			controller.updateFormData();			
			
			Scene departmentFormScene = new Scene(anchorPane);
			departmentFormScene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
			dialogStage.setScene(departmentFormScene);
			
			dialogStage.showAndWait();
		} catch (IOException e) {
			Alerts.showAlert("IOException", "Erro ao exibir tela", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChanged() {
		updateTableView();
	}

}
