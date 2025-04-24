module com.example.projet_campus {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.sql;
    requires mysql.connector.j;
    requires jbcrypt;
    requires org.apache.pdfbox;

    opens com.example.projet_campus to javafx.fxml;
    exports com.example.projet_campus;
}