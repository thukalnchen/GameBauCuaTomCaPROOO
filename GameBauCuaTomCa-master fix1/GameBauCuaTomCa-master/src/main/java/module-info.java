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
    requires rest.api.sdk;
    requires java.desktop;
    requires spark.core;
    requires jdk.httpserver;


    opens com.baucua.baucuatomca to javafx.fxml;


    // Add client package to module exports
    exports client;
    exports payment;

    exports server;
}