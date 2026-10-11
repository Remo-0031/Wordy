package client;

import client.controller.*;
import common.wordyApp.Wordy;
import common.wordyApp.WordyHelper;
import javafx.application.Application;
import javafx.fxml.*;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/**
 * The ClientMain class extends the JavaFX Application class
 * and serves as the entry point for the client-side of the application.
 */
public class ClientMain extends Application {
    static Wordy wordy;
    LoginController loginController;
    public ClientMain() {
    }
    /**
     * Main method for the client application.
     * Initializes the ORB and resolves the NameService object using the ORB.
     * @param args the command line arguments passed to the application
     */
    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args,null);
            org.omg.CORBA.Object objref  = orb.resolve_initial_references("NameService");

            NamingContextExt ncRef = NamingContextExtHelper.narrow(objref);

            String name = "Word";
            wordy = WordyHelper.narrow(ncRef.resolve_str(name));
            System.out.println(wordy);

            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Starts the application by loading the LoginMain.fxml file and displaying it in a new stage.
     * @param stage the primary stage for the application
     * @throws Exception if an error occurs while loading the LoginMain.fxml file
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("../fxml/LoginMain.fxml"));
        Parent root = loader.load();
        this.loginController = loader.getController();
        this.loginController.setWordy(wordy);
        Scene scene = new Scene(root);
        stage.setTitle("Login Page");
        stage.setScene(scene);
        stage.show();
    }
    /**
     * The setWordy method sets the Wordy object for the current instance of the class.
     * @param wordy the Wordy object to be set for the current instance.
     */
    public void setWordy(Wordy wordy) {
        this.wordy = wordy;
    }
    /**
     * The getWordy method gets the Wordy object for the current instance of the class.
     */
    public Wordy getWordy() {
        return wordy;
    }
}
