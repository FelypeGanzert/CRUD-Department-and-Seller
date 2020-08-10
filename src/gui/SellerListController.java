package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

import application.Main;
import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entites.Seller;
import model.services.SellerService;
import svgIcons.Icons;

public class SellerListController implements Initializable, DataChangeListener{

	@FXML private VBox mainVBox;
	@FXML private Button btnNewSeller;
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
	private ObservableList<Seller> sellerObsList;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeNodes();
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
	
	public void updateTableView() {
		if(sellerService == null) {
			throw new IllegalStateException("Seller service not initialized");
		}
		List<Seller> list = sellerService.findAdll();
		list.sort((p1, p2) -> p1.getName().toUpperCase().compareTo(p2.getName().toUpperCase()));
		sellerObsList = FXCollections.observableArrayList(list);
		tableViewSellers.setItems(sellerObsList);
		tableViewSellers.refresh();
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
	}

}
