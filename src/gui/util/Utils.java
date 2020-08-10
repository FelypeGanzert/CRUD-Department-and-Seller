package gui.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class Utils {

	private static final DecimalFormatSymbols DOLAR = new DecimalFormatSymbols(Locale.US);
	public static final DecimalFormat DINHEIRO_DOLAR = new DecimalFormat("¤ ###,###,##0.00", DOLAR);
	private static final Locale BRAZIL = new Locale("pt", "BR");
	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);
	public static final DecimalFormat DINHEIRO_REAL = new DecimalFormat("¤ ###,###,##0.00", REAL);

	public static Stage currentStage(ActionEvent event) {
		return (Stage) ((Node) event.getSource()).getScene().getWindow();
	}

	public static Integer tryParseToInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Button createIconButton(String svgAbsolutePath, int size, String iconClass) {
		SVGPath path = new SVGPath();
		path.setContent(svgAbsolutePath);
		Bounds bounds = path.getBoundsInLocal();

		// scale to size size x size (max)
		double scaleFactor = size / Math.max(bounds.getWidth(), bounds.getHeight());
		path.setScaleX(scaleFactor);
		path.setScaleY(scaleFactor);
		path.getStyleClass().add("button-icon");

		Button button = new Button();
		button.setPickOnBounds(true); // make sure transparent parts of the button register clicks too
		button.setGraphic(path);
		button.setAlignment(Pos.CENTER);
		button.getStyleClass().add("icon-button");
		button.getStyleClass().add(iconClass);
		return button;
	}

	public static <T> void formatTableColumnDate(TableColumn<T, Date> tableColumn, String format) {
		tableColumn.setCellFactory(column -> {
			TableCell<T, Date> cell = new TableCell<T, Date>() {
				private SimpleDateFormat sdf = new SimpleDateFormat(format);

				@Override
				protected void updateItem(Date item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setText(null);
					} else {
						setText(sdf.format(item));
					}
				}
			};
			return cell;
		});
	}

	public static <T> void formatTableColumnDoubleCurrency(TableColumn<T, Double> tableColumn) {
		tableColumn.setCellFactory(column -> {
			TableCell<T, Double> cell = new TableCell<T, Double>() {
				@Override
				protected void updateItem(Double item, boolean empty) {
					super.updateItem(item, empty);
					if (empty) {
						setText(null);
					} else {
						setText(mascaraDinheiro(item, DINHEIRO_REAL));
					}
				}
			};
			return cell;
		});
	}

	public static String mascaraDinheiro(double valor, DecimalFormat moeda){  
		return moeda.format(valor);  
	}

}
