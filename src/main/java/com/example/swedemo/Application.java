package com.example.swedemo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Application extends javafx.application.Application {

    public static HashMap<String, User> userList = new HashMap<>();

    @Override
    public void start(Stage stage) throws IOException, ParseException {

        // load database
        JSONParser parser = new JSONParser();
        JSONObject data = (JSONObject) parser.parse(new FileReader("src/main/resources/fauxACMEDatabase.json"));
        JSONArray database = (JSONArray) data.get("database");
        JSONObject employeesObj = (JSONObject) database.get(0);
        JSONArray employees = (JSONArray) employeesObj.get("employees");
        for (Object empObj : employees) {
            JSONObject emp = (JSONObject) empObj;
            User user = new User((String) emp.get("id"), (String) emp.get("email"), (String) emp.get("passwordHash"));
            userList.put(user.email, user);
        }

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