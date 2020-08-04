package gui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import application.Main;
import javafx.beans.property.SimpleObjectProperty;
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
import model.services.SellerService;

public class DepartmentListController implements Initializable{

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
		departmentObsList = FXCollections.observableArrayList(list);
		tableViewDepartments.setItems(departmentObsList);
	}

}
