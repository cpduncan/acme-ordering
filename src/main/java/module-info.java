module com.example.swedemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires json.simple;
    requires org.controlsfx.controls;



    opens com.example.swedemo to javafx.fxml;
    exports com.example.swedemo;
}