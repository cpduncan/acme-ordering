package com.example.swedemo;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        // TODO: Contact Us Screen?
    }
    // LOGIN SCREEN
    @FXML
    protected void onLogBackButton(ActionEvent event){
        switchScene("Startup.fxml",event);
    }
    @FXML
    TextField emailField;
    @FXML
    TextField passwordField;
    @FXML
    protected void onConfirmLoginButton(ActionEvent event) {
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
                switchScene("Dashboard.fxml", event);
                break;
            case 2:
                System.out.println("Incorrect Password"); // TODO: text appear
                break;
        }
    }

    // DASHBOARD SCREEN
    @FXML
    protected void onNewOrderButton(ActionEvent event) { switchScene("OrderForm.fxml", event); }
    @FXML
    protected void onNewCustomerButton(ActionEvent event) { switchScene("NewCustomer.fxml", event); }
    @FXML
    protected void onLogOutButton(ActionEvent event) { switchScene("Login.fxml",event); }

    // NEW ORDER SCREEN
    @FXML
    protected void onAddProductButton(ActionEvent event) {
        System.out.println("Add Product");
    }
    @FXML
    protected void onSubmitOrderButton(ActionEvent event) {
        System.out.println("Submit Order");
    }
    @FXML
    TextField productField;
    @FXML
    ContextMenu productContextMenu;
    @FXML
    protected void onProductFieldFocus(MouseEvent event) {
        productContextUpdate(productField.getText());
    }
    @FXML
    protected void onProductFieldKeypress(KeyEvent event) {
        productContextUpdate(productField.getText() + event.getText());
    }
    private void productContextUpdate(String text) {
        List<String> suggestions = Arrays.asList("Apple", "Banana", "Cherry", "Date"); // get suggestions for brand
        if (!text.isEmpty()) {
            suggestions = suggestions.stream().filter(item -> item.toLowerCase().startsWith(text.toLowerCase())).collect(Collectors.toList());
        }
        productContextMenu.getItems().clear();
        for (String item : suggestions) {
            MenuItem menuItem = new MenuItem(item);
            menuItem.setOnAction(e -> {
                productField.setText(item);
                productContextMenu.hide();
            });
            productContextMenu.getItems().add(menuItem);
        }
        productContextMenu.show(productField, Side.BOTTOM, 0, 0);
    }

    // NEW CUSTOMER SCREEN
    @FXML
    protected void onNewCusBackButton(ActionEvent event){ switchScene("Dashboard.fxml",event); }
    @FXML
    TextField businessNameField;
    @FXML
    TextField streetAddressField;
    @FXML
    TextField cityField;
    @FXML
    TextField stateField;
    @FXML
    TextField beerLisenceField;
    @FXML
    TextField deliveryConstraintsField;
    @FXML
    TextField pointOfContactField;
    @FXML
    TextField phoneNumberField;
    @FXML
    CheckBox paymentFormCash;
    @FXML
    CheckBox paymentFormEBT;
    @FXML
    CheckBox paymentFormFintech;
    @FXML
    CheckBox loadingDockCheckbox;
    @FXML
    protected void onPaymentForm(ActionEvent event) {
        CheckBox node = (CheckBox) event.getSource();
        if (node != paymentFormCash) {
            paymentFormCash.setIndeterminate(false);
            paymentFormCash.setSelected(false);
        }
        if (node != paymentFormEBT) {
            paymentFormEBT.setIndeterminate(false);
            paymentFormEBT.setSelected(false);
        }
        if (node != paymentFormFintech) {
            paymentFormFintech.setIndeterminate(false);
            paymentFormFintech.setSelected(false);
        }
    }
    @FXML
    protected void onSubmitNewCustomerButton(ActionEvent event) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject data = null;
            data = (JSONObject) parser.parse(new FileReader("src/main/resources/fauxACMEDatabase.json"));
            JSONArray database = (JSONArray) data.get("database");
            JSONObject customersObj = (JSONObject) database.get(1);
            JSONArray customers = (JSONArray) customersObj.get("customers");
            Map customer = new LinkedHashMap(5);
            customer.put("store", businessNameField.getText());
            try {
                Random rand = new Random();
                String nums = "";
                for (int i = 0; i < 9; i++) {
                    nums += rand.nextInt(10) + "";
                }
                customer.put("id", businessNameField.getText().replaceAll("\\s", "").substring(0, 5) + "_" + nums);
                System.out.println(businessNameField.getText().replaceAll("\\s", "").substring(0, 5) + "_" + nums);
            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException(e);
            }
            customer.put("address", (streetAddressField.getText() + " " + cityField.getText() + " " + stateField.getText()));
            customer.put("phoneNumber", phoneNumberField.getText());
            customer.put("owner", pointOfContactField.getText());
            customers.add(customer);
            try (FileWriter file = new FileWriter("src/main/resources/fauxACMEDatabase.json")) {
                file.write(data.toJSONString());
                file.flush();
            }
            switchScene("Dashboard.fxml",event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


}