package com.example.swedemo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

public class Application extends javafx.application.Application {

    public static HashMap<String, User> userList = new HashMap<>();

    @Override
    public void start(Stage stage) throws IOException {

        User user = new User("0", "jake@john.com", "password"); // test user
        userList.put(user.email, user);

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("Startup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("ACME Distribution Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}