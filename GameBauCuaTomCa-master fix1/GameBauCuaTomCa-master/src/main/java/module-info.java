module com.baucua.baucuatomca {
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
    requires java.sql;
    requires javafx.media;
    requires com.google.gson;

    opens com.baucua.baucuatomca to javafx.fxml;
    exports com.baucua.baucuatomca;

    // Add client package to module exports
    exports client;
}