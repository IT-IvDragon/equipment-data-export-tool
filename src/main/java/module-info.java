module com.example.e7tools {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.pcap4j.core;
    requires java.xml.bind;
    requires com.google.gson;
    requires okhttp3;

    opens com.example.e7tools to javafx.fxml;
    exports com.example.e7tools;
    exports com.example.e7tools.controller;
    opens com.example.e7tools.controller to javafx.fxml;
    exports com.example.e7tools.service;
    opens com.example.e7tools.service to javafx.fxml;
}