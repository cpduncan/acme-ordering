package com.example.swedemo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
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
    protected void onNewOrderButton(ActionEvent event) {
        Application.loadDatabase();
        switchScene("OrderForm.fxml", event);
    }
    @FXML
    protected void onNewCustomerButton(ActionEvent event) { switchScene("NewCustomer.fxml", event); }
    @FXML
    protected void onLogOutButton(ActionEvent event) { switchScene("Login.fxml",event); }

    // ORDER SCREEN
    @FXML
    TextField quantityField;
    @FXML
    TableView<Product> orderTable;
    @FXML
    TableColumn<Product, String> idCol;
    @FXML
    TableColumn<Product, String> productCol;
    @FXML
    TableColumn<Product, String> quantCol;
    @FXML
    protected void onAddProductButton(ActionEvent event) {
        if (productField.getText().isEmpty() || quantityField.getText().isEmpty() || customerIdField.getText().isEmpty() || brandField.getText().isEmpty())
            return;
        products.add(new Product(itemIds.get(productField.getText()), productField.getText(), quantityField.getText()));
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        orderTable.getColumns().setAll(idCol, productCol, quantCol);
        ObservableList<Product> data = FXCollections.observableArrayList();
        for (Product product : products) {
            data.add(product);
        }
        orderTable.setItems(data);
    }
    @FXML
    protected void onSubmitOrderButton(ActionEvent event) {
        String filePath = "src/main/resources/OrderForm.txt";
        String order = "\nAcme Distributing\nAccount ID#: " + customerIdField.getText() +
                "\nDelivery Date: 08/09/2025\nSales Rep ID: " + Application.currentUserId +
                "\nDelivery Rep ID: JhonsonT_22345\nID       Item Name * Quantity\n";
        for (Product product : products) {
            order += (product.productId + "   " + product.productName + " * " + product.quantity + "\n");
        }
        try (FileWriter fw = new FileWriter(filePath, true)) {
            fw.write(order);
        } catch (IOException e) {
            e.printStackTrace();
        }
        switchScene("Dashboard.fxml", event);
    }
    public static class Product {
        private final String productId;
        private final String productName;
        private final String quantity;
        public Product(String productId, String productName, String quantity) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;}
        public String getProductId() {return productId;}
        public String getProductName() {return productName;}
        public String getQuantity() {return quantity;}
    }
    private ArrayList<Product> products = new ArrayList<>();
    @FXML
    TextField customerIdField;
    @FXML
    ContextMenu customerIdContextMenu;
    private List<String> customerIds = Application.customerIds;
    @FXML
    protected void onCustomerIdFieldFocus(MouseEvent event) {
        productContextUpdate(customerIdField.getText(), customerIds, customerIdContextMenu, customerIdField);
    }
    @FXML
    protected void onCustomerIdFieldKeypress(KeyEvent event) {
        productContextUpdate(customerIdField.getText() + event.getText(), customerIds, customerIdContextMenu, customerIdField);
    }
    @FXML
    TextField brandField;
    @FXML
    ContextMenu brandContextMenu;
    @FXML
    protected void onBrandFieldFocus(MouseEvent event) {
        productContextUpdate(brandField.getText(), brands, brandContextMenu, brandField);
    }
    @FXML
    protected void onBrandFieldKeypress(KeyEvent event) {
        productContextUpdate(brandField.getText() + event.getText(), brands, brandContextMenu, brandField);
    }
    @FXML
    TextField productField;
    @FXML
    ContextMenu productContextMenu;
    @FXML
    protected void onProductFieldFocus(MouseEvent event) {
        List<String> suggestions = getBrandInventory(brandField.getText());
        productContextUpdate(productField.getText(), suggestions, productContextMenu, productField);
    }
    @FXML
    protected void onProductFieldKeypress(KeyEvent event) {
        List<String> suggestions = getBrandInventory(brandField.getText());
        productContextUpdate(productField.getText() + event.getText(), suggestions, productContextMenu, productField);
    }
    private void productContextUpdate(String text, List<String> suggestions, ContextMenu contextMenu, TextField textField) {
        if (suggestions == null) {
            return;
        }
        if (!text.isEmpty()) {
            suggestions = suggestions.stream().filter(item -> item.toLowerCase().startsWith(text.toLowerCase())).collect(Collectors.toList());
        }
        contextMenu.getItems().clear();
        for (String item : suggestions) {
            MenuItem menuItem = new MenuItem(item);
            menuItem.setOnAction(e -> {
                textField.setText(item);
                contextMenu.hide();
            });
            contextMenu.getItems().add(menuItem);
        }
        contextMenu.show(textField, Side.BOTTOM, 0, 0);
    }
    private List<String> brands = Arrays.asList("ALO DRINK", "ALTERNATIVE BIOLOGY", "AMERICAN SPIRITS EXCHANGE");
    private HashMap<String, String> itemIds = new HashMap<>() {{
                put("ALO EXPOSED", "063401");
                put("ALO BLUSH", "063402");
                put("ALO COMFORT", "063405");
                put("ALO ALLURE", "063411");
                put("ALO CRISP", "063421");
                put("ALO SPRING", "063431");
                put("GW RAINBOWSHRB", "067001");
                put("GW PEARPINAPL", "067002");
                put("GW TANGERINE", "067003");
                put("GW CANDYSHOP", "067004");
                put("GW WATERMELON", "067005 ");
                put("LONGBALL BOUBN", "300460");
                put("STILLWTR RYE", "300461");
                put("FRONT 9 BOURBN", "300462");
    }};
    private List<String> getBrandInventory(String brand) {
        switch(brand) {
            case "ALO DRINK":
                return Arrays.asList(
                        "ALO EXPOSED",
                        "ALO BLUSH",
                        "ALO COMFORT",
                        "ALO ALLURE",
                        "ALO CRISP",
                        "ALO SPRING"
                );
            case "ALTERNATIVE BIOLOGY":
                return Arrays.asList(
                        "GW RAINBOWSHRB",
                        "GW PEARPINAPL",
                        "GW TANGERINE",
                        "GW CANDYSHOP",
                        "GW WATERMELON"
                );
            case "AMERICAN SPIRITS EXCHANGE":
                return Arrays.asList(
                        "LONGBALL BOUBN",
                        "STILLWTR RYE",
                        "FRONT 9 BOURBN"
                );
            default:
                return null;
        }
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