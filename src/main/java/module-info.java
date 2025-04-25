module org.example.mediaplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;

    opens org.example.mediaplayer to javafx.fxml;
    exports org.example.mediaplayer;
}
