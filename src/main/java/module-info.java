module ru.rsreu.straxov.englishdictionary {
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
    requires org.jsoup;
    requires javafx.media;
    requires javafx.swing;
    requires static lombok;
    requires com.google.gson;
    requires annotations;

    opens ru.rsreu.straxov.englishdictionary to javafx.fxml;
    exports ru.rsreu.straxov.englishdictionary;
    exports ru.rsreu.straxov.englishdictionary.controllers;
    opens ru.rsreu.straxov.englishdictionary.controllers to javafx.fxml;

}