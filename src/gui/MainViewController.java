package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import application.Main;
import gui.util.Alerts;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import model.services.DepartmentService;
import model.services.SellerService;

public class MainViewController implements Initializable {

	@FXML MenuItem menuItemSeller;
	@FXML MenuItem menuItemDepartment;
	@FXML MenuItem menuItemAbout;
	@FXML VBox paneInfos;
	
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	public void onMenuItemSellerAction(ActionEvent event) {
		loadView("/gui/SellerList.fxml", (SellerListController controller) -> {
			controller.setSellerService(new SellerService());
			controller.updateTableView();
			controller.setDepartmentService(new DepartmentService());
			controller.setDepartmentsToComboBoxFilter(null);
		});	
		Main.getPrimaryStage().setMinWidth(800);
	}
	
	public void onMenuItemDepartmentAction(ActionEvent event) {
		loadView("/gui/DepartmentList.fxml", (DepartmentListController controller) -> {
			controller.setDepartmentService(new DepartmentService());
			controller.setSellerService(new SellerService());
			controller.updateTableView();
		});	
		Main.getPrimaryStage().setMinWidth(550);
	}
	
	public void onMenuAboutAction() {	
		loadView("/gui/AboutView.fxml", x -> {});
		Main.getPrimaryStage().setMinWidth(400);
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
		} catch(IllegalStateException e) {
			Alerts.showAlert("IllegalStateException", "Erro ao exibir tela", e.getMessage(), AlertType.ERROR);
		}
	}
}
