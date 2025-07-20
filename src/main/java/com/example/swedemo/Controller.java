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
import javafx.scene.text.Text;
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
    }
    // LOGIN SCREEN
    @FXML
    protected void onLogBackButton(ActionEvent event){
        switchScene("Startup.fxml",event);
    }
    @FXML
    TextField emailField;
    @FXML
    PasswordField passwordField;
    @FXML
    Text emailSideText;
    @FXML
    Text passwordSideText;
    @FXML
    protected void onConfirmLoginButton(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        User user = userList.get(email);
        if (user == null) {
            emailSideText.setText("No Existing User");
            return;
        }
        emailSideText.setText("");
        int code = user.login(email, password);
        switch (code){
            case 0:
                switchScene("Dashboard.fxml", event);
                break;
            case 2:
                passwordSideText.setText("Incorrect Password");
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
    protected void onOrderBackButton(ActionEvent event) { switchScene("Dashboard.fxml", event); }
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
    private List<String> brands = Arrays.asList("ALO DRINK",
            "ALTERNATIVE BIOLOGY",
            "AMERICAN SPIRITS EXCHANGE",
            "C2O",
            "CAPE FEAR",
            "COSTA",
            "HI-WIRE BREWING",
            "HOLLOW CREEK D",
            "ISLAND COAST",
            "JARITOS",
            "JOSEPH VICTORI",
            "KITU LIFE",
            "KING JUICE",
            "KING SPIRITS",
            "KINGS CALLING",
            "LANGERS",
            "LEAN BODY",
            "LEVENDI WINES",
            "LIQUID DEATH",
            "MIGHTY SWELL",
            "MILESTONE BEVERAGES",
            "MONARCHY BEVERAGES",
            "NEW BELGIUM",
            "NESTLE",
            "NERRAGANSETT",
            "NUTRABOLT / C4",
            "PALMETTO BREWE"
    );

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
                put("SEASONAL MNSHN", "100310");
                put("WTRMLN SHINE", "100311");
                put("HI CTN 105 PRF", "100312");
                put("HCD HI CTN SP", "100313");
                put("W. ALAN 107 PRF", "100314");
                put("W. ALAN SP", "100315");
                put("W.ALAN SBP", "100316");
                put("WILLIAM ALAN", "100317");
                put("HI COTTN BOURBN", "100380");
                put("LOW WTR VODKA", "100381");
                put("WHITE HOT", "100382");
                put("HW MEXHOTCHOC", "060266");
                put("HW WINTRWARMR", "060268");
                put("HW GATOR JUICE", "060274");
                put("HW KELLER PILS", "060276");
                put("HW CARML MACH", "060278");
                put("HW MARG SOUR", "060284");
                put("HW MTN WHEAT", "060287");
                put("HW LOPITCH IPA", "060292");
                put("HW IPA PITCH", "060294");
                put("HW CLOWNCAR", "060298");
                put("HW LAGER", "060306");
                put("HW CHOC WOW", "060308");
                put("HW CHERYREDHOT", "060309");
                put("HW ITALIAN PLS", "060311");
                put("HW REDRYE LGR", "060314");
                put("HW HUGSONBEACH", "060321");
                put("HW CHOCOTACO", "060323");
                put("HW MAI TAI", "060325");
                put("HW CHOC BRULEE", "060327");
                put("HW HIPITCH IPA", "060334");
                put("HW BED O NAILS", "060342");
                put("HW COLD HAZY", "060343");
                put("HW BOHEMIN DRK", "060349");
                put("HW HAZE IDAHO", "060351");
                put("HW A BETTERWAY", "060353");
                put("HW PINAPLUPSID", "060361");
                put("HW VISION HAZY", "060363");
                put("HW RASPBRY FIZ", "060367");
                put("HW DRAGNFRUIT", "060370");
                put("HW GRNDMAPIE", "060372");
                put("HW SS TROPPNCH", "060376");
                put("HW CZECH PILS", "060381");
                put("HW 10YR HIWIRE", "060383");
                put("HW VIET ICECOF", "060397");
                put("HW GETTER LOW", "060401");
                put("HW DBL HIPITCH", "060405");
                put("HW IMPERSTOUT", "060410");
                put("HW ELECTRICSQZ", "060411");
                put("HW JUICYJUICY", "060413");
                put("HW FESTBIER", "060417");
                put("HW STRONGMAN", "060426");
                put("HW 10W40CHOC", "060427");
                put("HW CERVEZA", "060436");
                put("HW BALTC PORTR", "060441");
                put("HW WEST COAST", "060446");
                put("HW HOT CHOC", "060447");
                put("HW DBL CHERRY", "060457");
                put("HW SUPER HAZE", "060460");
                put("HW 10W40 KINGC", "060467");
                put("HW 10W40BANSPL", "060471");
                put("HW GINGR BREAD", "060479");
                put("HW COCOA PORTR", "060497");
                put("HW HZYHZYHZY", "060501");
                put("HW SWTMALTLQR", "060504");
                put("HW SM BLBRYPEA", "060507");
                put("HW BA BARLYWIN", "060509");
                put("HW ELATDWINTER", "060511");
                put("HW FRUIT TART", "060517");
                put("HW CHAI 10W40", "060526");
                put("HW ORIG 10W40", "060527");
                put("HW ZIRKUSFEST", "060532");
                put("HW LMTD LAGER", "060537");
                put("HW LEISURETIME", "060541");
                put("HW MANHATTAN", "060546");
                put("HW MTN WATER", "060547");
                put("HW WICKPICKLES", "060550");
                put("HW MUDSLIDE", "060554");
                put("HW BOOGIEBOARD", "060663");
                put("ISLC LAGER", "066102");
                put("ISLC ACTIVE", "066112");
                put("ISLC ACTIVEVAR", "066137");
                put("JV SB BLUE MOSCATO", "101300");
                put("JV SB PINK MOS ROS", "101301");
                put("JV SB ORANGE MANGO", "101302");
                put("JV SB MOS ROSE BUB", "101303");
                put("JV SB MOS PEACH BU", "101304");
                put("JV FRITZ DE KATZ", "101306");
                put("JV BEL VENTO PINTO", "101307");
                put("JV SANGRIA", "101308");
                put("JV SB PINEAPPLE", "101309");
                put("JV EPIC PROCECCO", "101311");
                put("JV SB MOSCATO ROSE", "101312");
                put("JV BLKCHRY MOSCATO", "101313");
                put("YPSO BLUE", "063108");
                put("YPSO PEACH", "063109");
                put("YPSO LEMONADE", "063111");
                put("YPSO STWBERRY", "063121");
                put("YPSO KIWI", "063131");
                put("YPSO MELON", "063141");
                put("YPSO MANGO", "063191");
                put("YPSO ISLNDWAVE", "063196");
                put("YPSO BLKCHRLEM", "063201");
                put("YPSO ZERO STRW", "063221");
                put("YPSO PUNCH", "063231");
                put("YPSO LT OCNBLU", "063261");
                put("KC LUXURY LAG", "060708");
                put("KC BEACHYWHT", "060718");
                put("CAPE FEAR FRYINPANBOURBON", "301303");
                put("CAPE FEAR GAMEFISHVODKA", "301304");
                put("CAPE FEAR SOLERA WHISKEY", "301305");
                put("CAPE FEAR MARITIME GIN", "301306");
                put("CAPE FEAR RUM", "301307");
                put("CAPE FEAR BEACHBLST RUM", "301309");
                put("CAPE FEAR BILL FISH TEQ", "301310");
                put("KING SPIRITS WALKER BOURBON", "301300");
                put("KING SPIRITS HOPETOWN VODKA", "301301");
                put("KING SPIRITS LIME HOPETOWN", "301302");
                put("KING SPIRITS DEEP DROP", "301308");
                put("KL SC STRBRY", "062021");
                put("KL SC COCMOCHA", "062022");
                put("KL SC FRNCHVAN", "062023");
                put("KL SC SWEETCRM", "062024");
                put("KL SC ESP VAN", "062025");
                put("KL SC HAZELNUT", "062026");
                put("KL SC MOCHA", "062027");
                put("KL SC PMPKNPIE", "062029");
                put("KL SC ESPCARML", "062030");
                put("KL SC WHT CHOC", "062031");
                put("KL SC BLBRYMUF", "062032");
                put("KL SC CRML WAF", "062033");
                put("KL SC CINNROLL", "062034");
                put("KL SC VANILLA", "062060");
                put("KL SUPRCLDBREW", "062070");
                put("LB COOKIECRM", "061303");
                put("LB VANILLA", "061304");
                put("LB CHOCOLATE", "061305");
                put("LB CARAMEL", "061306");
                put("LNG ORG JUICE", "061201");
                put("LNG APPLEJUICE", "061202");
                put("LNG PINEAPPLE", "061203");
                put("LNG CRANBERRY", "061204");
                put("LNG MONGOMANGO", "061205");
                put("LNG GRP JUICE", "061206");
                put("LNG FRUITPUNCH", "061207");
                put("LNG RED GRPFRT", "061208");
                put("LNG BLUE PUNCH", "061209");
                put("LNG ORG BLEND", "061215");
                put("LNG WTRMLNPNCH", "061223");
                put("LNG SWTSOURMIX", "061232");
                put("LNG IMMUNBOOST", "061250");
                put("LEVENDI SWTWTR CABERNET", "102000");
                put("LEVENDI STAGE COACH CAB", "102001");
                put("LEVENDI LEGACY MTN CAB", "102002");
                put("LEVENDI CHARDONNAY", "102003");
                put("LQD ORG STILL", "069200");
                put("LQD SPARKLING", "069201");
                put("LQD BERRY", "069202");
                put("LQD MANGO", "069203");
                put("LQD LIME", "069204");
                put("LQD MELON", "069205");
                put("LQD LIME SPRKLN", "069212");
                put("LQD MANGO SPRKLN", "069213");
                put("LQD MELON SPRKLN", "069214");
                put("LQD BERRY SPRKLN", "069215");
                put("LQD GRIMLEAFR", "069216");
                put("LQD PALMER TEA", "069217");
                put("LQD PEACH TEA", "069218");
                put("LQD SLGHTERBRY", "069231");
                put("LQD OS METAL", "069233");
                put("LQD METALMELON", "069234");
                put("LQD METALLEAFR", "069235");
                put("MS BLACKBERRY", "079300");
                put("MS TECHNIFLVR", "079301");
                put("MS VARIETY", "079302");
                put("MS KEEPWEIRD", "079305");
                put("MS BLOOD ORG", "079336");
                put("IRIS PINOTNOIR", "101900");
                put("MON DPWELLWTR", "068955");
                put("MON SPRKLN WTR", "068956");
                put("GNST LAGER", "034040");
                put("GNST SHANDY", "034050");
                put("GNST ATLANTCLT", "034097");
                put("QUIK CHOCOLATE", "062163");
                put("QUIK FDGBROWNE", "062175");
                put("QUIK STRWBERRY", "062176");
                put("QUIK MLKDRKCHO", "062178");
                put("PROTEINVANILLA", "062195");
                put("PROTEIN CHOCOL", "062196");
                put("PROTEIN STRWBR", "062198");
                put("NES FROOTLOOP", "062199");
                put("QUIK FRSTDFLAK", "062233");
                put("NB ACCUMULATN", "080557");
                put("NB TRIPPEL", "080566");
                put("NB VOODOO HAZE", "080571");
                put("NB DANGERBEACH", "080584");
                put("NB HOLIDAY ALE", "080591");
                put("NB FRUITFORCE", "080602");
                put("NB HOP RAIDER", "080604");
                put("NB OAKSPIRE", "080607");
                put("NB HOPPY VAR", "080619");
                put("NB VOODOO IMP", "080630");
                put("NB FOLLY", "080649");
                put("NB 1985 IPA", "080650");
                put("NB ATOMICCITRS", "080653");
                put("NB CRYO RANGER", "080664");
                put("NB RESVR HONEY", "080690");
                put("NB FAT TIRE", "080700");
                put("NB PINKLMNADE", "080713");
                put("NB VOODOO VAR", "080716");
                put("NB MURAL", "080718");
                put("NB 1554 BLKALE", "080722");
                put("NB VOODOO RNGR", "080732");
                put("NB GRANDRSERVE", "080737");
                put("NB DOMINGA", "080742");
                put("NB  VARIETY", "080750");
                put("NB JUICE FORCE", "080761");
                put("NB ATOMIC PUMP", "080775");
                put("NB FOLLY VARIT", "080783");
                put("NB NITRO COLD", "080787");
                put("NB TRANSKRIEK", "080789");
                put("NB HARVEST ALE", "080797");
                put("NB BOHEMIAN PL", "080798");
        put("C20 3/8 10.5OZ", "062797");
        put("C20 12/1L", "062798");
        put("C20 12/17.5C", "062799");
        put("JAR MANDRIN 8/1.5L BTL", "062700");
        put("JAR PINEAPPLE 8/1.5L BTLS", "062701");
        put("JAR MANDRIN 24/17.7OZ", "062710");
        put("JAR PINEAPPLE 24/17.7OZ BTL", "062711");
        put("JAR MANDRIN 24/12.5OZ", "062720");
        put("JAR TAMARIND 24/12.5OZ", "062721");
        put("JAR LIME 24/12.5OZ BTL", "062722");
        put("JAR FRT PUNCH 24/12.5OZ", "062723");
        put("JAR GRAPEFRT 24/12.5OZ BTL", "062724");
        put("JAR PINEAPPLE 24/12.5OZ BTLS", "062725");
        put("JAR GUAVA 24/12.5OZ BTL", "062726");
        put("JAR STRAWBERRY 24/12.5OZ BTL", "062727");
        put("JAR MANGO 24/12.5OZ BTLS", "062728");
        put("JAR MEX COLA 24/12.5 BTL", "062729");
        put("JAR WATERMELON 24/12.5OZ", "062730");
        put("JAR FIESTA VAR 12/12.5", "062731");
        put("JAR MINERAGUA 12/12.5", "062732");
        put("JAR MANDRIN 12/12OZ", "062733");
        put("JAR SIDRL VAR 12/12.5", "062734");
        put("JAR MEXICO VAR 12/12B", "062740");
        put("JAR MINERAGUA 8/1.5L BTLS", "062745");
        put("JAR MINERAGUA 24/17.7 OZ BTL", "062750");
        put("JAR MINERAGUA 24/12.5OZ", "062755");
        put("JAR SANGRIA 24/17.7 OZ BTL", "062760");
        put("JAR SANGRIA 24/11.16OZ", "062765");
        put("JAR SIDRL APL 24/16.9OZ", "062770");
        put("JAR SIDRL APL 24/12OZ", "062775");
        put("JAR FRUITPUNCH 24/17.7 OZ", "062785");
        put("JAR TAMARIND 24/17.7PET", "062786");
        put("JAR GRAPEFRT 24PK PET", "062787");
        put("C4 STRBRYGUAVA 12/12C", "032205");
        put("C4 TROP PASSN 2/12C", "032206");
        put("C4 BLDORG YUZU 2/12C", "032207");
        put("C4 STRBRYWTRML 12/16 CAN", "032210");
        put("C4 FRZN BOMB 12/16 CAN", "032211");
        put("C4 PURPLEFROST 12/16 CAN", "032212");
        put("C4 ORANGESLICE 12/16 CAN", "032213");
        put("C4 MIDNITECHRY 12/16OZ CAN", "032214");
        put("C4 TROP BLAST 12/16 CAN", "032217");
        put("C4 STRBRYBURST 12/16", "032218");
        put("C4 CHERRYBURST 12/16 CAN", "032219");
        put("C4 SKITTLES 12/16 CAN", "032220");
        put("C4 ULT FRTPNCH 12/16C", "032221");
        put("C4 ULT SNWCONE 12/16", "032222");
        put("C4 ULT ORANGE 12/16", "032223");
        put("C4 ULT FREEDOM 12/16C", "032224");
        put("C4 ULT RASPBRY 12/16", "032225");
        put("C4 BERRYPWRBMB 12/16C", "032226");
        put("C4 PEACHMANGO 12/16 CAN", "032230");
        put("C4 CHERRYBERRY 12/16 CAN", "032231");
        put("C4 WTRMLNBURST 12/16", "032232");
        put("C4 FRZN BOMB 12/12C", "032260");
        put("C4 STRBRYWTRML 12/12C", "032261");
        put("C4 CHERRYBERRY 12/12C", "032262");
        put("C4 PEACHMANGO 12/12 C", "032263");
        put("C4 MANGO FOXTR 12/16C", "032264");
        put("C4 WTRMLNBURST 12/12C", "032265");
        put("C4 STRBRYBURST 6/4C", "032266");
        put("PLM AMBER ALE 4/6 CAN", "083700");
        put("PLM AMBER ALE 1/6BBL", "083706");
        put("PLM SALTDLIME 4/6TT", "083727");
        put("PLM HUGER ST 4/6 CAN", "083800");
        put("PLM HUGER ST 1/2BBL", "083802");
        put("PLM HUGER ST 1/6BBL", "083806");
        put("PLM AMERICAN 4/6C", "083857");
        put("PLM AMERICAN 1/6BBL", "083858");
        put("PLM MANGFRUIT 4/6C", "083862");
        put("PLM MANGFRUIT 1/6BBL", "083863");
        put("PLM PEACH ALE 4/6C", "083865");
        put("PLM PEACH ALE 1/6BBL", "083866");
        put("6/750  COSTA SILVER", "300900");
        put("6/750  COSTA REPOSADO", "300901");
        put("6/750  COSTA ANEJO TEQ", "300902");


    }};
    private List<String> getBrandInventory(String brand) {
        switch (brand) {
            case "C2O":
                return Arrays.asList(
            "C20 3/8 10.5OZ",
            "C20 12/1L",
            "C20 12/17.5C"
                        );
            case "JARITOS":
                return Arrays.asList(
            "JAR MANDRIN 8/1.5L BTL",
            "JAR PINEAPPLE 8/1.5L BTLS",
            "JAR MANDRIN 24/17.7OZ",
            "JAR PINEAPPLE 24/17.7OZ BTL",
            "JAR MANDRIN 24/12.5OZ",
            "JAR TAMARIND 24/12.5OZ",
            "JAR LIME 24/12.5OZ BTL",
            "JAR FRT PUNCH 24/12.5OZ",
            "JAR GRAPEFRT 24/12.5OZ BTL",
            "JAR PINEAPPLE 24/12.5OZ BTLS",
            "JAR GUAVA 24/12.5OZ BTL",
            "JAR STRAWBERRY 24/12.5OZ BTL",
            "JAR MANGO 24/12.5OZ BTLS",
            "JAR MEX COLA 24/12.5 BTL",
            "JAR WATERMELON 24/12.5OZ",
            "JAR FIESTA VAR 12/12.5",
            "JAR MINERAGUA 12/12.5",
            "JAR MANDRIN 12/12OZ",
            "JAR SIDRL VAR 12/12.5",
            "JAR MEXICO VAR 12/12B",
            "JAR MINERAGUA 8/1.5L BTLS",
            "JAR MINERAGUA 24/17.7 OZ BTL",
            "JAR MINERAGUA 24/12.5OZ",
            "JAR SANGRIA 24/17.7 OZ BTL",
            "JAR SANGRIA 24/11.16OZ",
            "JAR SIDRL APL 24/16.9OZ",
            "JAR SIDRL APL 24/12OZ",
            "JAR FRUITPUNCH 24/17.7 OZ",
            "JAR TAMARIND 24/17.7PET",
            "JAR GRAPEFRT 24PK PET"
                        );
            case "NUTRABOLT / C4":
                return Arrays.asList(
            "C4 STRBRYGUAVA 12/12C",
            "C4 TROP PASSN 2/12C",
            "C4 BLDORG YUZU 2/12C",
            "C4 STRBRYWTRML 12/16 CAN",
            "C4 FRZN BOMB 12/16 CAN",
            "C4 PURPLEFROST 12/16 CAN",
            "C4 ORANGESLICE 12/16 CAN",
            "C4 MIDNITECHRY 12/16OZ CAN",
            "C4 TROP BLAST 12/16 CAN",
            "C4 STRBRYBURST 12/16",
            "C4 CHERRYBURST 12/16 CAN",
            "C4 SKITTLES 12/16 CAN",
            "C4 ULT FRTPNCH 12/16C",
            "C4 ULT SNWCONE 12/16",
            "C4 ULT ORANGE 12/16",
            "C4 ULT FREEDOM 12/16C",
            "C4 ULT RASPBRY 12/16",
            "C4 BERRYPWRBMB 12/16C",
            "C4 PEACHMANGO 12/16 CAN",
            "C4 CHERRYBERRY 12/16 CAN",
            "C4 WTRMLNBURST 12/16",
            "C4 FRZN BOMB 12/12C",
            "C4 STRBRYWTRML 12/12C",
            "C4 CHERRYBERRY 12/12C",
            "C4 PEACHMANGO 12/12 C",
            "C4 MANGO FOXTR 12/16C",
            "C4 WTRMLNBURST 12/12C",
            "C4 STRBRYBURST 6/4C"
                        );
            case "PALMETTO BREWE":
                return Arrays.asList(
            "PLM AMBER ALE 4/6 CAN",
            "PLM AMBER ALE 1/6BBL",
            "PLM SALTDLIME 4/6TT",
            "PLM HUGER ST 4/6 CAN",
            "PLM HUGER ST 1/2BBL",
            "PLM HUGER ST 1/6BBL",
            "PLM AMERICAN 4/6C",
            "PLM AMERICAN 1/6BBL",
            "PLM MANGFRUIT 4/6C",
            "PLM MANGFRUIT 1/6BBL",
            "PLM PEACH ALE 4/6C",
            "PLM PEACH ALE 1/6BBL"
                        );
            case "COSTA":
                return Arrays.asList(
            "6/750  COSTA SILVER",
            "6/750  COSTA REPOSADO",
            "6/750  COSTA ANEJO TEQ"
                        );
            case "LANGERS":
            return Arrays.asList(
        "LNG ORG JUICE",
        "LNG APPLEJUICE",
        "LNG PINEAPPLE",
        "LNG CRANBERRY",
        "LNG MONGOMANGO",
        "LNG GRP JUICE",
        "LNG FRUITPUNCH",
        "LNG RED GRPFRT",
        "LNG BLUE PUNCH",
        "LNG ORG BLEND",
        "LNG WTRMLNPNCH",
        "LNG SWTSOURMIX",
        "LNG IMMUNBOOST"
            );
            case "LEVENDI WINES":
                return Arrays.asList(
        "LEVENDI SWTWTR CABERNET",
        "LEVENDI STAGE COACH CAB",
        "LEVENDI LEGACY MTN CAB",
        "LEVENDI CHARDONNAY"
            );
            case "LIQUID DEATH":
                return Arrays.asList(
        "LQD ORG STILL",
        "LQD SPARKLING",
        "LQD BERRY",
        "LQD MANGO",
        "LQD LIME",
        "LQD MELON",
        "LQD LIME SPRKLN",
        "LQD MANGO SPRKLN",
        "LQD MELON SPRKLN",
        "LQD BERRY SPRKLN",
        "LQD GRIMLEAFR",
        "LQD PALMER TEA",
        "LQD PEACH TEA",
        "LQD SLGHTERBRY",
        "LQD OS METAL",
        "LQD METALMELON",
        "LQD METALLEAFR"
            );
            case "MIGHTY SWELL":
                return Arrays.asList(
        "MS BLACKBERRY",
        "MS TECHNIFLVR",
        "MS VARIETY",
        "MS KEEPWEIRD",
        "MS BLOOD ORG"
            );
            case "MILESTONE BEVERAGES":
                return Arrays.asList(
        "IRIS PINOTNOIR"
            );
            case "MONARCHY BEVERAGES":
                return Arrays.asList(
        "MON DPWELLWTR", "MON SPRKLN WTR"
            );
            case "NERRAGANSETT":
                return Arrays.asList(
        "GNST LAGER",
        "GNST SHANDY",
        "GNST ATLANTCLT"
            );
            case "NESTLE":
                return Arrays.asList(
        "QUIK CHOCOLATE",
        "QUIK FDGBROWNE",
        "QUIK STRWBERRY",
        "QUIK MLKDRKCHO",
        "PROTEINVANILLA",
        "PROTEIN CHOCOL",
        "PROTEIN STRWBR",
        "NES FROOTLOOP",
        "QUIK FRSTDFLAK"
            );
            case "NEW BELGIUM":
                return Arrays.asList(
        "NB ACCUMULATN",
        "NB TRIPPEL",
        "NB VOODOO HAZE",
        "NB DANGERBEACH",
        "NB HOLIDAY ALE",
        "NB FRUITFORCE",
        "NB HOP RAIDER",
        "NB OAKSPIRE",
        "NB HOPPY VAR",
        "NB VOODOO IMP",
        "NB FOLLY",
        "NB 1985 IPA",
        "NB ATOMICCITRS",
        "NB CRYO RANGER",
        "NB RESVR HONEY",
        "NB FAT TIRE",
        "NB PINKLMNADE",
        "NB VOODOO VAR",
        "NB MURAL",
        "NB 1554 BLKALE",
        "NB VOODOO RNGR",
        "NB GRANDRSERVE",
        "NB DOMINGA",
        "NB  VARIETY",
        "NB JUICE FORCE",
        "NB ATOMIC PUMP",
        "NB FOLLY VARIT",
        "NB NITRO COLD",
        "NB TRANSKRIEK",
        "NB HARVEST ALE",
        "NB BOHEMIAN PL"
        );
            case "JOSEPH VICTORI":
                return Arrays.asList(
                        "JV SB BLUE MOSCATO",
            "JV SB PINK MOS ROS",
            "JV SB ORANGE MANGO",
            "JV SB MOS ROSE BUB",
            "JV SB MOS PEACH BU",
            "JV FRITZ DE KATZ",
            "JV BEL VENTO PINTO",
            "JV SANGRIA",
            "JV SB PINEAPPLE",
            "JV EPIC PROCECCO",
            "JV SB MOSCATO ROSE",
            "JV BLKCHRY MOSCATO"
                );
            case "KING JUICE":
                return Arrays.asList(
            "YPSO BLUE",
            "YPSO PEACH",
            "YPSO LEMONADE",
            "YPSO STWBERRY",
            "YPSO KIWI",
            "YPSO MELON",
            "YPSO MANGO",
            "YPSO ISLNDWAVE",
            "YPSO BLKCHRLEM",
            "YPSO ZERO STRW",
            "YPSO PUNCH",
            "YPSO LT OCNBLU"
                );
            case "KINGS CALLING":
                return Arrays.asList(
            "KC LUXURY LAG",
            "KC BEACHYWHT"
                );
            case "CAPE FEAR":
                return Arrays.asList(
            "FRYINPANBOURBON",
            "GAMEFISHVODKA",
            "SOLERA WHISKEY",
            "MARITIME GIN",
            "RUM",
            "BEACHBLST RUM",
            "BILL FISH TEQ"
                );
            case "KING SPIRITS":
                return Arrays.asList(
            "WALKER BOURBON",
            "HOPETOWN VODKA",
            "LIME HOPETOWN",
            "DEEP DROP"
                );
            case "KITU LIFE":
                return Arrays.asList(
            "KL SC STRBRY",
            "KL SC COCMOCHA",
            "KL SC FRNCHVAN",
            "KL SC SWEETCRM",
            "KL SC ESP VAN",
            "KL SC HAZELNUT",
            "KL SC MOCHA",
            "KL SC PMPKNPIE",
            "KL SC ESPCARML",
            "KL SC WHT CHOC",
            "KL SC BLBRYMUF",
            "KL SC CRML WAF",
            "KL SC CINNROLL",
            "KL SC VANILLA",
            "KL SUPRCLDBREW"
                );
            case "LEAN BODY":
                return Arrays.asList(
            "LB COOKIECRM",
            "LB VANILLA",
            "LB CHOCOLATE",
            "LB CARAMEL"
                );
            case "ISLAND COAST":
                return Arrays.asList(
                    "ISLC LAGER",
                    "ISLC ACTIVE",
                    "ISLC ACTIVEVAR"
                );
            case "HI-WIRE BREWING":
                return Arrays.asList(
                    "HW MEXHOTCHOC",
                    "HW WINTRWARMR",
                    "HW GATOR JUICE",
                    "HW KELLER PILS",
                    "HW CARML MACH",
                    "HW MARG SOUR",
                    "HW MTN WHEAT",
                    "HW LOPITCH IPA",
                    "HW IPA PITCH",
                    "HW CLOWNCAR",
                    "HW LAGER",
                    "HW CHOC WOW",
                    "HW CHERYREDHOT",
                    "HW ITALIAN PLS",
                    "HW REDRYE LGR",
                    "HW HUGSONBEACH",
                    "HW CHOCOTACO",
                    "HW MAI TAI",
                    "HW CHOC BRULEE",
                    "HW HIPITCH IPA",
                    "HW BED O NAILS",
                    "HW COLD HAZY",
                    "HW BOHEMIN DRK",
                    "HW HAZE IDAHO",
                    "HW A BETTERWAY",
                    "HW PINAPLUPSID",
                    "HW VISION HAZY",
                    "HW RASPBRY FIZ",
                    "HW DRAGNFRUIT",
                    "HW GRNDMAPIE",
                    "HW SS TROPPNCH",
                    "HW CZECH PILS",
                    "HW 10YR HIWIRE",
                    "HW VIET ICECOF",
                    "HW GETTER LOW",
                    "HW DBL HIPITCH",
                    "HW IMPERSTOUT",
                    "HW ELECTRICSQZ",
                    "HW JUICYJUICY",
                    "HW FESTBIER",
                    "HW STRONGMAN",
                    "HW 10W40CHOC",
                    "HW CERVEZA",
                    "HW BALTC PORTR",
                    "HW WEST COAST",
                    "HW HOT CHOC",
                    "HW DBL CHERRY",
                    "HW SUPER HAZE",
                    "HW 10W40 KINGC",
                    "HW 10W40BANSPL",
                    "HW GINGR BREAD",
                    "HW COCOA PORTR",
                    "HW HZYHZYHZY",
                    "HW SWTMALTLQR",
                    "HW SM BLBRYPEA",
                    "HW BA BARLYWIN",
                    "HW ELATDWINTER",
                    "HW FRUIT TART",
                    "HW CHAI 10W40",
                    "HW ORIG 10W40",
                    "HW ZIRKUSFEST",
                    "HW LMTD LAGER",
                    "HW LEISURETIME",
                    "HW MANHATTAN",
                    "HW MTN WATER",
                    "HW WICKPICKLES",
                    "HW MUDSLIDE",
                    "HW BOOGIEBOARD"
                );
            case "HOLLOW CREEK D":
                return Arrays.asList(
                        "HONEY MNSHN",
                        "SEASONAL MNSHN",
                        "WTRMLN SHINE",
                        "HI CTN 105 PRF",
                        "HCD HI CTN SP",
                        "W. ALAN 107 PRF",
                        "W. ALAN SP",
                        "W.ALAN SBP",
                        "WILLIAM ALAN",
                        "HI COTTN BOURBN",
                        "LOW WTR VODKA",
                        "WHITE HOT"
                );
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