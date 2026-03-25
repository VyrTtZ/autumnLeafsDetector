module org.example.autumnleavesdetector {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jmh.core;


    opens org.example.autumnleavesdetector to javafx.fxml;
    exports org.example.autumnleavesdetector;
}