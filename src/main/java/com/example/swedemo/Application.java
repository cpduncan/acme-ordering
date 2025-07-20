package com.example.swedemo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Application extends javafx.application.Application {

    public static HashMap<String, User> userList = new HashMap<>();
    public static String currentUserId = null;
    public static ArrayList<String> employeeIds = new ArrayList<>();
    public static ArrayList<String> customerIds = new ArrayList<>();

    @Override
    public void start(Stage stage) throws IOException, ParseException {
        loadDatabase();
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("Startup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("ACME Distribution Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void loadDatabase() {
        try {
        JSONParser parser = new JSONParser();
        JSONObject data = null;
            data = (JSONObject) parser.parse(new FileReader("src/main/resources/fauxACMEDatabase.json"));
        JSONArray database = (JSONArray) data.get("database");
        JSONObject employeesObj = (JSONObject) database.get(0);
        JSONArray employees = (JSONArray) employeesObj.get("employees");
        for (Object empObj : employees) {
            JSONObject emp = (JSONObject) empObj;
            User user = new User((String) emp.get("id"), (String) emp.get("email"), (String) emp.get("passwordHash"));
            userList.put(user.email, user);
            employeeIds.add((String) emp.get("id"));
        }
        JSONObject customersObj = (JSONObject) database.get(1);
        JSONArray customers = (JSONArray) customersObj.get("customers");
        for (Object custObj : customers) {
            JSONObject cust = (JSONObject) custObj;
            customerIds.add((String) cust.get("id"));
        }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}