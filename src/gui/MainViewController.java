package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import application.Main;
import gui.util.Alerts;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;

public class MainViewController implements Initializable {

	@FXML MenuItem menuItemSeller;
	@FXML MenuItem menuItemDepartment;
	@FXML MenuItem menuItemAbout;
	@FXML VBox paneInfos;
	
	public void initialize(URL location, ResourceBundle resources) {
		
	}
	
	public void onMenuItemSellerAction() {
		System.out.println("Seller menu");
	}
	
	public void onMenuItemDepartmentAction() {
		loadView("/gui/DepartmentList.fxml");		
	}
	
	public void onMenuAboutAction() {	
		loadView("/gui/AboutView.fxml");
	}
	
	private synchronized void loadView(String absolutePath) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absolutePath));
			VBox newVBox = loader.load();
			
			Scene mainScene = Main.getMainScene();
			VBox vBoxMainScene = (VBox) mainScene.lookup("#paneInfos");
			
			vBoxMainScene.getChildren().clear();
			vBoxMainScene.getChildren().addAll(newVBox);
			vBoxMainScene.setStyle(newVBox.getStyle());
		} catch (IOException e) {
			Alerts.showAlert("IOException", "Erro ao exibir tela", e.getMessage(), AlertType.ERROR);
		} catch(IllegalStateException e) {
			Alerts.showAlert("IllegalStateException", "Erro ao exibir tela", e.getMessage(), AlertType.ERROR);
			
		}
		
	}

}
