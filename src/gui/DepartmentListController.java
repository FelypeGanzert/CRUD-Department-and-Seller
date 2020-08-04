package gui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.entites.Department;
import model.services.DepartmentService;

public class DepartmentListController implements Initializable{

	@FXML private VBox mainVBox;
	@FXML private Button btnNewDepartment;
	@FXML private Button btnListSellers;
	@FXML private TableView<Department> tableViewDepartments;
	@FXML private TableColumn<Department, String> tableColumnNome;
	@FXML private TableColumn<Department, Integer> tableColumnVendedores;
	
	private DepartmentService service;
	private ObservableList<Department> departmentObsList;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeNodes();
		
	}
		
	private void initializeNodes() {
		tableColumnNome.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnVendedores.setCellValueFactory(new PropertyValueFactory<>("sellersQuantity"));
		
		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewDepartments.prefHeightProperty().bind(stage.heightProperty());
	}
	

	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}
	
	public void updateTableView() {
		if(service == null) {
			throw new IllegalStateException("Department service not initialized");
		}
		List<Department> list = service.findAdll();
		departmentObsList = FXCollections.observableArrayList(list);
		tableViewDepartments.setItems(departmentObsList);
	}

}
