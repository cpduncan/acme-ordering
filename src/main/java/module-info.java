module com.example.swedemo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.swedemo to javafx.fxml;
    exports com.example.swedemo;
}