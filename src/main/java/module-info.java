module org.example.autumnleavesdetector {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.autumnleavesdetector to javafx.fxml;
    exports org.example.autumnleavesdetector;
}