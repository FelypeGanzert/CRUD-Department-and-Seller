package gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import application.Main;
import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import icons.Icons;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
	@FXML private Label lblDepartmentTotal;
	@FXML private Button btnNewDepartment;
	@FXML private Button btnListSellers;
	@FXML private TableView<Department> tableViewDepartments;
	@FXML private TableColumn<Department, Integer> tableColumnId;
	@FXML private TableColumn<Department, String> tableColumnNome;
	@FXML private TableColumn<Department, Integer> tableColumnVendedores;
	@FXML private TableColumn<Department, Department> tableColumnEdit;
	@FXML private TableColumn<Department, Department> tableColumnDelete;
	
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
	
	public void handleListSellers(ActionEvent event) {
		loadView("/gui/SellerList.fxml", (SellerListController controller) -> {
			controller.setSellerService(new SellerService());
			controller.updateTableView();
			controller.setDepartmentService(new DepartmentService());
			controller.setDepartmentsToComboBoxFilter(tableViewDepartments.getSelectionModel().getSelectedItem());
		});	
		Main.getPrimaryStage().setMinWidth(800);
	}
		
	private void initializeNodes() {
		tableColumnNome.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnVendedores.setCellValueFactory(cellData -> new SimpleObjectProperty<>(sellerService.findByDepartment(cellData.getValue()).size()));
		// Edit buttons
		initButtons(tableColumnEdit, 15, Icons.PEN_SOLID, "grayIcon", (department, event) -> {
			createDepartmentDialogForm(department, "/gui/DepartmentForm.fxml", Utils.currentStage(event));
		});
		// Delete buttons
		initButtons(tableColumnDelete, 15, Icons.TRASH_SOLID, "redIcon", (department, event) -> {
			removeEntity(department);
		});
		// Listener to selected department
		tableViewDepartments.getSelectionModel().selectedItemProperty().addListener(
	            (observable, oldSelection, newSelection) -> {
	            	if(newSelection != null) {
	            		btnListSellers.setText("Vendedores de [ID=" + newSelection.getId() + "] " +newSelection.getName());
	            		btnListSellers.setDisable(false);
	            	} else {
	            		btnListSellers.setDisable(true);
	            	}
	            }
	    );
		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewDepartments.prefHeightProperty().bind(stage.heightProperty());
	}
	
	private <T, T2> void initButtons(TableColumn<Department, Department> tableColumn,
			int size, String svgIcon, String className, BiConsumer<Department, ActionEvent> buttonAction) {

		tableColumn.setMinWidth(size + 20);
		tableColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumn.setCellFactory(param -> new TableCell<Department, Department>() {
			private final Button button = Utils.createIconButton(svgIcon, size, className);

			@Override
			protected void updateItem(Department department, boolean empty) {
				super.updateItem(department, empty);
				if (department == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> {
					buttonAction.accept(department, event);
				});
			}
		});
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
		tableViewDepartments.refresh();
		lblDepartmentTotal.setText("(Total de: " + Utils.pointSeparator(departmentObsList.size()) + ")");
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
			e.printStackTrace();
			Alerts.showAlert("IOException", "Erro ao exibir tela", e.getMessage(), AlertType.ERROR);
		}
	}
	
	private void removeEntity(Department entity) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Deletar departamento");
		alert.setHeaderText("Id: " + entity.getId() + " - " + entity.getName());
		alert.setContentText("Tem certeza que deseja deletar?");
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {
			try {
				departmentService.delete(entity);
				onDataChanged();
			} catch (DbException e) {
				e.printStackTrace();
				Alerts.showAlert("DbException", "Erro ao deletar", e.getMessage(), AlertType.ERROR);
			}
		}
	}
	
	private synchronized <T> void loadView(String absolutePath, Consumer<T> initializingAction) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absolutePath));
			VBox newVBox = loader.load();
			
			Scene mainScene = Main.getMainScene();
			VBox vBoxMainScene = (VBox) mainScene.lookup("#paneInfos");
			
			vBoxMainScene.getChildren().clear();
			vBoxMainScene.getChildren().addAll(newVBox);
			vBoxMainScene.setStyle(newVBox.getStyle());
			
			T controller = loader.getController();
			initializingAction.accept(controller);
		} catch (IOException e) {
			e.printStackTrace();
			Alerts.showAlert("IOException", "Erro ao exibir tela", e.getMessage(), AlertType.ERROR);
			e.printStackTrace();
		} catch(IllegalStateException e) {
			e.printStackTrace();
			Alerts.showAlert("IllegalStateException", "Erro ao exibir tela", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChanged() {
		updateTableView();
	}

}
