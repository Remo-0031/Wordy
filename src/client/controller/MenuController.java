package client.controller;

import client.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import common.wordyApp.NoOtherPlayersAvailable;
import common.wordyApp.Player;
import common.wordyApp.Wordy;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * The MenuController class is responsible for managing the menu in a JavaFX application
 * and provides methods for handling user interactions with the menu buttons.
 */
public class MenuController implements Initializable {
    LoginController loginController;
    MenuController menuController;
    GameController gameController;
    LeaderboardsController leaderboardsController;
    static Wordy wordy;
    Player player;
    Alert waitAlert;
    int GameID;
    boolean stayInWaitingRoom;
    //BoardController boardController;
    Connection con;
    //Player thisPlayer;

    @FXML
    Label lblName;
    @FXML
    Button btnStart;
    @FXML
    Button btnBoard;
    @FXML
    Button btnLogout;
    /**
     * This method handles the action when the "Start" button is clicked.
     * @param event the ActionEvent object that triggered the method
     * @throws Exception if there is an error while starting the game
     */
    public void btnStartClicked(ActionEvent event) throws IOException {
        Node node = (Node) event.getSource();
        Stage loadingStage = (Stage) node.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("../fxml/WaitingMain.fxml"));
        Parent root = loader.load();
        loadingStage.setScene(new Scene(root));
        loadingStage.setResizable(false);
        loadingStage.show();

        Platform.runLater(() -> {
            try {
                GameID = wordy.play(player.username);
                loadGame(loadingStage);
            } catch (NoOtherPlayersAvailable noOtherPlayersAvailable) {
                try {
                    loadMenuPage(event, loadingStage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setContentText(noOtherPlayersAvailable.reason);
                a.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadGame(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("../fxml/GameMain.fxml"));
        Parent root = (Parent) loader.load();
        gameController = loader.getController();
        gameController.setGame(wordy, GameID, player);
        gameController.setButtonList();
        gameController.setUpTimer();
        Scene scene = new Scene(root);
        stage.setTitle("Game Room");
        stage.setResizable(false);
        stage.setScene(scene);
    }


    /**
     * This method handles the action when the "Board" button is clicked.
     * @param event the ActionEvent object that triggered the method
     * @throws Exception if there is an error while displaying the leaderboard
     */
    public void btnBoardClicked(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("../fxml/LeaderboardsMain.fxml"));
        Parent root = loader.load();

        leaderboardsController = loader.getController();
        leaderboardsController.setWordy(wordy, player);
        leaderboardsController.setupTableViews();

        URL stylesheetLocation = ClientMain.class.getResource("../assets/stylesheets/leaderboards.css");
        String styleSheet = Objects.requireNonNull(stylesheetLocation).toString();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(styleSheet);

        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setTitle("LeaderBoard Page");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    /**
     * This method loads the login page of the application.
     * @param event the ActionEvent object that triggered the method
     * @throws IOException if there is an error while loading the login page
     */
    public void loadLoginPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("../fxml/LoginMain.fxml"));
        Parent root = loader.load();
        loginController = loader.getController();
        loginController.setWordy(wordy);

        Scene scene = new Scene(root);
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setTitle("Login Page");
        stage.setScene(scene);
    }
    /**
     * This method handles the action when the "Logout" button is clicked.
     * @param event the ActionEvent object that triggered the method
     * @throws Exception if there is an error during the logout process
     */
    public void btnLogoutClicked(ActionEvent event) throws IOException, SQLException {
        this.wordy.logout(player.username);

        loadLoginPage(event);
    }

    public void setWordy(Wordy wordy,Player player) {
        this.wordy = wordy;
        this.player = player;
        lblName.setText(player.firstName+" "+player.lastName);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void loadMenuPage(ActionEvent event,Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("../fxml/MenuMain.fxml"));
        Parent root = loader.load();
        menuController = loader.getController();
        menuController.setWordy(wordy, player);
        Scene scene = new Scene(root);
        stage.setTitle("Menu Page");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
