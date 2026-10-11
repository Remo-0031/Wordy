package client.controller;

import client.*;
import client.controller.leaderboards.PlayerStandingRound;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.Window;
import common.wordyApp.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GameController implements Initializable {
    Wordy wordy;
    GameController gameController;
    MenuController menuController;
    String letterSet;
    int gameID;
    Player player;
    ArrayList<Button> buttonList = new ArrayList<>();
    static ScheduledExecutorService exec;
    int timer;
    boolean gameCreator = false;
    boolean roundOver;
    int roundNumber;
    String lastRoundResult;

    @FXML
    private Button btn1;

    @FXML
    private Button btn10;

    @FXML
    private Button btn11;

    @FXML
    private Button btn12;

    @FXML
    private Button btn13;

    @FXML
    private Button btn14;

    @FXML
    private Button btn15;

    @FXML
    private Button btn16;

    @FXML
    private Button btn17;

    @FXML
    private Button btn2;

    @FXML
    private Button btn3;

    @FXML
    private Button btn4;

    @FXML
    private Button btn5;

    @FXML
    private Button btn6;

    @FXML
    private Button btn7;

    @FXML
    private Button btn8;

    @FXML
    private Button btn9;

    @FXML
    TextField tfWord;

    @FXML
    private Label lblStatus;

    @FXML
    private TableColumn<PlayerStandingRound, String> PNAMECOL;

    @FXML
    private TableView<PlayerStandingRound> TABLE;

    @FXML
    private TableColumn<PlayerStandingRound, Integer> WINCOL;

    public void setGame(Wordy wordy,int gameID,Player player) {
        this.wordy = wordy;
        try {
            wordy.submitWord(gameID,player.username,"");
        } catch (WordIsNotValid | WordIsShort ignored){}
        this.gameID = gameID;
        this.player = player;
        this.gameCreator= wordy.isGameCreator(player.username);
        setColRight();
        System.out.println(gameCreator);
        letterSet = wordy.getLetterSet(gameID);
        buttonList.add(btn1);
        buttonList.add(btn2);
        buttonList.add(btn3);
        buttonList.add(btn4);
        buttonList.add(btn5);
        buttonList.add(btn6);
        buttonList.add(btn7);
        buttonList.add(btn8);
        buttonList.add(btn9);
        buttonList.add(btn10);
        buttonList.add(btn11);
        buttonList.add(btn12);
        buttonList.add(btn13);
        buttonList.add(btn14);
        buttonList.add(btn15);
        buttonList.add(btn16);
        buttonList.add(btn17);
    }

    private void setColRight() {
        WINCOL.setCellValueFactory(new PropertyValueFactory<>("playerScore"));
        PNAMECOL.setCellValueFactory(new PropertyValueFactory<>("playerName"));

        ObservableList<PlayerStandingRound> list = getPlayerStanding();
        TABLE.setItems(list);
    }

    private ObservableList<PlayerStandingRound> getPlayerStanding() {
        PlayerRecord[] pr = this.wordy.getRoundRecords(gameID);
        ObservableList<PlayerStandingRound> list = FXCollections.observableArrayList();
        for (PlayerRecord p : pr){
            String user = p.username;
            int wins = p.wins;
            PlayerStandingRound psr = new PlayerStandingRound(user,wins);
            list.add(psr);
        }
        return list;
    }

    public void setButtonList(){
        letterSet = wordy.getLetterSet(gameID);
        int x = 0;
        for (Button b : buttonList){
            b.setText(String.valueOf(letterSet.charAt(x)));
            b.setDisable(false);
            x++;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tfWord.setOnKeyPressed(keyEvent -> {
            // ignore typing between rounds
            if (roundOver) {
                keyEvent.consume();
                return;
            }
            boolean keyIsEnter = keyEvent.getCode().equals(KeyCode.ENTER);
            boolean keyIsBackspace = keyEvent.getCode().equals(KeyCode.BACK_SPACE);

            if (!keyIsEnter &&! keyIsBackspace){
                if(!checkIfPartOfLetterSet(keyEvent.getCode().toString())){
                    Alert  a  = new Alert(Alert.AlertType.INFORMATION);
                    a.setContentText("Not part of letter Set");
                    a.show();
                    keyEvent.consume();
                }
            }
            if (keyIsEnter){
                submitWord();
                setButtonList();
            }
        });

    }

    private boolean checkIfPartOfLetterSet(String s) {
        for(Button b : buttonList){
            if (b.getText().equalsIgnoreCase(s)){
                b.setText("-");
                b.setDisable(true);
                return true;
            }
        }
        return false;
    }

    private void submitWord() {
        try {
            wordy.submitWord(gameID, player.username, tfWord.getText());


            setColRight();

            if (player.longestWord.length() < tfWord.getText().length()) {
                wordy.updateLongestWord(player.username, tfWord.getText());
                player.longestWord = tfWord.getText();
            }

        } catch (WordIsNotValid e) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setContentText("Word is not Valid");
            a.show();
        } catch (WordIsShort e) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setContentText("Word is too Short");
            a.show();
        }
    }

    /**
     * Called once when the round timer reaches 0. The server has already scored the round,
     * so this only shows the round result and checks whether someone has won the game.
     */
    private void endRound() {
        String roundWinner = wordy.getRoundWinner(gameID, player.username);
        if (roundWinner.equalsIgnoreCase("no winner")) {
            lastRoundResult = "No winner in round " + roundNumber;
        } else if (roundWinner.equals(player.username)) {
            lastRoundResult = "You won round " + roundNumber + "!";
        } else {
            lastRoundResult = roundWinner + " won round " + roundNumber;
        }
        tfWord.setEditable(false);
        setColRight();
        lblStatus.setText(lastRoundResult + "\nNext round starting...");

        String winner = wordy.getWinner(gameID, player.username);
        if (!winner.equalsIgnoreCase("no winner")) {
            exec.shutdownNow();
            try {
                Window window = btn1.getScene().getWindow();
                Stage stage = (Stage) window.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(ClientMain.class.getResource("../fxml/MenuMain.fxml"));
                Parent parent = fxmlLoader.load();
                menuController = fxmlLoader.getController();
                menuController.setWordy(wordy,player);
                stage.setScene(new Scene(parent));
                stage.show();
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setContentText(winner + " is the winner of the game you were Playing!");
                if(winner.equals(player.username)) wordy.updateWin(player.username, player.wins+1);
                a.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Called once when the server has started the next round (timer is back above 0).
     */
    private void startNextRound() {
        roundNumber++;
        tfWord.setText("");
        tfWord.setEditable(true);
        setButtonList();
        setColRight();
    }

    public void setUpTimer(){
        roundOver = false;
        roundNumber = 1;
        lastRoundResult = null;
        exec = new ScheduledThreadPoolExecutor(1);
        Runnable run = () -> Platform.runLater(() -> {
            if (exec.isShutdown()) return;
            timer = wordy.getTime(gameID);
            if (!roundOver && timer < 1) {
                roundOver = true;
                endRound();
            } else if (roundOver && timer >= 1) {
                roundOver = false;
                startNextRound();
            }
            if (!roundOver) {
                String status = "Round " + roundNumber + "  -  " + timer + "s left";
                if (lastRoundResult != null) status += "\n" + lastRoundResult;
                lblStatus.setText(status);
            }
        });
        exec.scheduleAtFixedRate(run, 10, 500, TimeUnit.MILLISECONDS);
    }
}
