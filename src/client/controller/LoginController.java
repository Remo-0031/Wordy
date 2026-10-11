package client.controller;
import client.*;
import common.wordyApp.Wordy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import common.wordyApp.AccountIsLoggedIn;
import common.wordyApp.AccountNotFound;
import common.wordyApp.Player;
import common.wordyApp.WrongPassword;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
/**
 * The LoginController class is responsible for handling user login functionality
 * in the JavaFX application and implements the Initialize interface
 */
public class LoginController implements Initializable {
    MenuController menuController;
    Wordy wordy;
    Player player;

    @FXML
    private Label LoginLblButton;
    @FXML
    private Button btnLogin;
    @FXML
    private Label lblError;
    @FXML
    private PasswordField tfPass;
    @FXML
    private TextField tfUser;
    /**
     * This method is called by the FXMLLoader when the corresponding FXML file is loaded.
     * @param url the location used to resolve relative paths for the root object
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblError.setVisible(false);
    }
    /**
     * This method loads the Menu page of the application.
     * @param event the ActionEvent object that triggered the method
     * @throws IOException if there is an error while loading the Menu page
     */
    public void loadMenuPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(ConsoleVersion.class.getResource("../fxml/MenuMain.fxml"));
        Parent root = loader.load();
        menuController = loader.getController();
        menuController.setWordy(wordy, player);
        Scene scene = new Scene(root);
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setTitle("Menu Page");
        stage.setResizable(false);
        stage.setScene(scene);
    }
    /**
     * This method handles the action when the "Login" button is clicked.
     * @param event the ActionEvent object that triggered the method
     * @throws Exception if there is an error during the login process
     */
    public void btnLoginClicked(ActionEvent event) throws IOException{
        Alert a;
        try {
            this.player = wordy.login(tfUser.getText(), tfPass.getText());

            loadMenuPage(event);
            System.out.println("Logged In");
        } catch (AccountIsLoggedIn ex){
            accountIsLoggedIn(ex);
        } catch (AccountNotFound ex){
            accountNotFound(ex);
        } catch (WrongPassword ex){
            wrongPasswordEntered(ex);
        }
    }

    private void accountIsLoggedIn(AccountIsLoggedIn exception) {
        lblError.setVisible(true);
        lblError.setText("Account Already Logged In");
        tfUser.setText("");
        tfPass.setText("");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(exception.reason);
        alert.show();
    }

    private void accountNotFound(AccountNotFound exception) {
        lblError.setVisible(true);
        lblError.setText("Account Not Found");
        tfUser.setText("");
        tfPass.setText("");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(exception.reason);
        alert.show();
    }

    private void wrongPasswordEntered(WrongPassword exception) {
        lblError.setVisible(true);
        lblError.setText("Wrong Password Entered");
        tfUser.setText("");
        tfPass.setText("");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(exception.reason);
        alert.show();
    }

    /**
     * Sets the wordy object for this instance.
     * @param Word The wordy object to set.
     */
    public void setWordy(Wordy wordy){
        this.wordy = wordy;
        System.out.println(wordy);
    }
}
