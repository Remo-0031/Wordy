package client.controller;

import client.*;
import client.controller.leaderboards.PlayerLongestWord;
import client.controller.leaderboards.PlayerWins;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import common.wordyApp.Player;
import common.wordyApp.Wordy;

import java.io.IOException;

public class LeaderboardsController {

    private MenuController menuController;

    @FXML
    private TableView<PlayerLongestWord> tblWord;
    @FXML
    private TableView<PlayerWins> tblWin;
    @FXML
    private TableColumn<PlayerLongestWord, String> colPlayerWord;
    @FXML
    private TableColumn<PlayerLongestWord, String> colWord;
    @FXML
    private TableColumn<PlayerWins, String> colPlayerWin;
    @FXML
    private TableColumn<PlayerWins, Integer> colWin;
    @FXML
    private Button btnReturn;

    private Wordy wordy;

    private Player player;

    public void setWordy(Wordy wordy, Player player) {
        this.wordy = wordy;
        this.player = player;
    }

    public void setupTableViews() {
        colPlayerWord.setCellValueFactory(new PropertyValueFactory<>("username"));
        colWord.setCellValueFactory(new PropertyValueFactory<>("longestWord"));

        colPlayerWin.setCellValueFactory(new PropertyValueFactory<>("username"));
        colWin.setCellValueFactory(new PropertyValueFactory<>("wins"));

        ObservableList<PlayerLongestWord> longestWords = getLeaderboardLongestWords();
        ObservableList<PlayerWins> topWins = getLeaderboardWins();

        tblWord.setItems(longestWords);
        tblWin.setItems(topWins);
    }

    public ObservableList<PlayerLongestWord> getLeaderboardLongestWords() {
        Player[] leaderboardsLongestWords = this.wordy.getLeaderBoardsWords();
        ObservableList<PlayerLongestWord> outputList = FXCollections.observableArrayList();

        for (Player player : leaderboardsLongestWords) {
            String username = player.username;
            String longestWord = player.longestWord;
            PlayerLongestWord playerLongestWord = new PlayerLongestWord(username, longestWord);
            outputList.add(playerLongestWord);
        }

        return outputList;
    }

    public ObservableList<PlayerWins> getLeaderboardWins() {
        Player[] leaderboardsWins = this.wordy.getLeaderBoardsWins();
        ObservableList<PlayerWins> outputList = FXCollections.observableArrayList();

        for (Player player : leaderboardsWins) {
            String username = player.username;
            int wins = player.wins;

            PlayerWins playerWins = new PlayerWins(username, wins);

            outputList.add(playerWins);
        }

        return outputList;
    }

    public void btnReturnClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("../fxml/MenuMain.fxml"));
        Parent root = loader.load();
        menuController = loader.getController();
        menuController.setWordy(wordy, player);

        Scene scene = new Scene(root);
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setTitle("Menu Page");
        stage.setScene(scene);
    }

}
