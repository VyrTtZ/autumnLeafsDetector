module org.example.autumnleavesdetector {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.autumnleavesdetector to javafx.fxml;
    exports org.example.autumnleavesdetector;
}