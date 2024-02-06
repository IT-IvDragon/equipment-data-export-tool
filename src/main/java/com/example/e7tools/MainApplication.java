package com.example.e7tools;

import com.example.e7tools.constant.UITextConstant;
import com.example.e7tools.controller.MainController;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        //载入view
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        // 从主舞台获取 HostServices 对象
        HostServices hostServices = getHostServices();
        // 将 HostServices 对象传递给 Controller 类
        MainController controller = fxmlLoader.getController();
        controller.setHostServices(hostServices);

        stage.setTitle(UITextConstant.MAIN_SCENE_TITLE);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}