import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.math.BigInteger;

public class TrojkatPascalaJavaFX extends Application {

    private TextField poleRozmiaru;
    private VBox panelTrojkata;
    private ScrollPane scrollPane;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Trójkąt Pascala");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #F0F8FF;");

        HBox gornyPanel = new HBox(10);
        gornyPanel.setPadding(new Insets(10));
        gornyPanel.setAlignment(Pos.CENTER);
        gornyPanel.setStyle("-fx-background-color: #E6E6FA;");

        Label etykieta = new Label("Rozmiar trójkąta:");
        etykieta.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        poleRozmiaru = new TextField();
        poleRozmiaru.setPrefWidth(80);
        poleRozmiaru.setOnAction(e -> generujTrojkat());

        Button przyciskGeneruj = new Button("Pokaż");
        przyciskGeneruj.setOnAction(e -> generujTrojkat());

        gornyPanel.getChildren().addAll(etykieta, poleRozmiaru, przyciskGeneruj);
        root.setTop(gornyPanel);

        panelTrojkata = new VBox(5);
        panelTrojkata.setAlignment(Pos.CENTER);
        panelTrojkata.setPadding(new Insets(15));
        panelTrojkata.setStyle("-fx-background-color: #F0F8FF;");

        scrollPane = new ScrollPane(panelTrojkata);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 600, 450);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void generujTrojkat() {
        try {
            String tekstRozmiaru = poleRozmiaru.getText().trim();
            if (tekstRozmiaru.isEmpty()) {
                pokazBlad("Pole jest puste!");
                return;
            }

            int rozmiar = Integer.parseInt(tekstRozmiaru);

            if (rozmiar <= 0) {
                pokazBlad("Rozmiar musi być większy od zera!");
                return;
            }

            if (rozmiar > 50) {
                pokazBlad("Za duży rozmiar - max 50!");
                return;
            }

            panelTrojkata.getChildren().clear();

            BigInteger[][] trojkat = obliczTrojkat(rozmiar);

            int maxSzer = String.valueOf(trojkat[rozmiar-1][rozmiar/2]).length();

            for (int i = 0; i < rozmiar; i++) {
                HBox wiersz = new HBox(8);
                wiersz.setAlignment(Pos.CENTER);

                for (int j = 0; j <= i; j++) {
                    String wartosc = trojkat[i][j].toString();
                    Text liczba = new Text(String.format("%" + maxSzer + "s", wartosc));
                    liczba.setFont(Font.font("Monospaced", 14));
                    liczba.setFill(Color.DARKBLUE);
                    wiersz.getChildren().add(liczba);
                }

                panelTrojkata.getChildren().add(wiersz);
            }

            scrollPane.setVvalue(0);
            scrollPane.setHvalue(0.5);

        } catch (NumberFormatException e) {
            pokazBlad("To nie jest prawidłowa liczba!");
        } catch (Exception e) {
            pokazBlad("Wystąpił błąd: " + e.getMessage());
        }
    }

    private BigInteger[][] obliczTrojkat(int rozmiar) {
        BigInteger[][] wynik = new BigInteger[rozmiar][];

        for (int i = 0; i < rozmiar; i++) {
            wynik[i] = new BigInteger[i+1];
            wynik[i][0] = BigInteger.ONE;

            for (int j = 1; j < i; j++) {
                wynik[i][j] = wynik[i-1][j-1].add(wynik[i-1][j]);
            }

            if (i > 0) {
                wynik[i][i] = BigInteger.ONE;
            }
        }

        return wynik;
    }

    private void pokazBlad(String komunikat) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(komunikat);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}