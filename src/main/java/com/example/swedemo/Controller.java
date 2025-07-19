package com.example.swedemo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

public class Controller {

    private Stage stage;
    private Scene scene;
    private Parent parent;
    private HashMap<String, User> userList = Application.userList;


    private void switchScene(String fxmlFile, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // STARTUP SCREEN
    @FXML
    protected void onLoginButton(ActionEvent event) {
        switchScene("Login.fxml", event);
    }
    @FXML
    protected void onContactUsButton() {
        // TODO: Contact Us Screen
    }

    // LOGIN SCREEN // this is a text change
    @FXML
    TextField emailField;
    @FXML
    TextField passwordField;
    @FXML
    protected void onConfirmLoginButton() {
        String email = emailField.getText();
        String password = passwordField.getText();
        User user = userList.get(email);
        if (user == null) {
            System.out.println("no existing user"); // TODO: text appear
            return;
        }
        int code = user.login(email, password);
        switch (code){
            case 0:
                System.out.println("Switch screen"); // TODO: switch to other scene
                break;
            case 2:
                System.out.println("Incorrect Password"); // TODO: text appear
                break;
        }
    }


}