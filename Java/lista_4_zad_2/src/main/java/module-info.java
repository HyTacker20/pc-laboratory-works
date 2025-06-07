module com.example.lista_4_zad_2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.lista_4_zad_2 to javafx.fxml;
    exports com.example.lista_4_zad_2;
}