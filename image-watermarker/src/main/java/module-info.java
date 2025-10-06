module com.mywatermark {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;
    requires com.google.gson;

    opens com.mywatermark to javafx.fxml;
    exports com.mywatermark;
}
