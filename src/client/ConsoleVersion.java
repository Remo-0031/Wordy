package client;

import common.wordyApp.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConsoleVersion {
    static Wordy wordy;
    static int matchId;
    static long timer;
    static ScheduledExecutorService exec;

    public static void main(String[] args) {
        try {

            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objref = orb.resolve_initial_references("NameService");

            NamingContextExt ncRef = NamingContextExtHelper.narrow(objref);

            String name = "Word";
            wordy = WordyHelper.narrow(ncRef.resolve_str(name));
            showMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showMenu() {
        System.out.println("1. Play");
        System.out.println("2. Top Wins Leaderboards");
        System.out.println("3. Top Longest Words Leaderboards");
        System.out.println("4. Logout");
        doEvent(Integer.parseInt(new Scanner(System.in).nextLine()));
    }

    private static void doEvent(int i) {
        switch (i){
            case 1:
                try {
                    System.out.println("Enter Username: ");
                    String username = new Scanner(System.in).nextLine();
                    matchId = wordy.play(username);
                    gameRoom(username);
                } catch (NoOtherPlayersAvailable e) {
                    System.out.println(e.getMessage());
                }
//                try {
//                    wordy.login("wordyApp","123");
//                } catch (AccountIsLoggedIn accountIsLoggedIn) {
//                    accountIsLoggedIn.printStackTrace();
//                } catch (AccountNotFound accountNotFound) {
//                    accountNotFound.printStackTrace();
//                } catch (WrongPassword wrongPassword) {
////                    wrongPassword.printStackTrace();
//                    System.out.println("Wrong Password");
//                }catch (org.omg.CORBA.BAD_PARAM e ){
//                    System.out.println("Bad");
//                }
                break;
            case 2:
                Player[] playerWinsLeaderboard = wordy.getLeaderBoardsWins();
                printPlayers(playerWinsLeaderboard);
                break;
            case 3:
                Player[] playerLongestWordLeaderboard = wordy.getLeaderBoardsWords();
                printPlayers(playerLongestWordLeaderboard);
            case 4:
                System.exit(0);

        }
    }

    //TEMPORARY
    private static void printPlayers(Player[] players) {
        String format = "%-5s %-20s %-20s %-20s %15s %-20s \n";

        System.out.printf(format, "#", "Username", "Firstname", "Lastname", "Wins", "Longest Word");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");

        for (int i = 0; i < players.length; i++) {
            String username = players[i].username;
            String firstname = players[i].firstName;
            String lastname = players[i].lastName;
            int wins = players[i].wins;
            String longestWord = players[i].longestWord;
            System.out.printf(format, (i + 1) + ".)", username, firstname, lastname, wins, longestWord);
        }
    }

    /**
     * Starts a game session for the given username by displaying the letter set
     * and prompting the user to enter words until the timer reaches zero.
     * @param username the username of the player
     */
    private static void gameRoom(String username) {
        // Create a scanner to read user input
        Scanner scanner = new Scanner(System.in);
        startGameTimer();
        try {
            Thread.sleep(1000); // Wait for 1 second before starting the game
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Display the letter set for the current game
        System.out.println(wordy.getLetterSet(matchId));

        // Prompt the user to enter words until the timer reaches zero
        while (timer != 0){
            System.out.println("Enter a word");
            String word = scanner.nextLine();
            try {
                // Submit the word to the game server
                wordy.submitWord(matchId, username, word);
            } catch (WordIsNotValid e) {
                System.out.println("Word invalid:");
                continue;
            } catch (WordIsShort e) {
                System.out.println("Word to short;");
                continue;
            }
        }
        // Display the winner of the round
        System.out.println(wordy.getRoundWinner(matchId,username) + "is the winner");
        if (wordy.getWinner(matchId,username).equalsIgnoreCase("no winner")){
            startGameTimer();
            System.out.println("Reaching here " + timer);
            System.out.println(wordy.getLetterSet(matchId));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (timer != 0){
                System.out.println("Enter a word");
                String word = scanner.nextLine();
                try {
                    // Submit the word to the game server
                    wordy.submitWord(matchId, username, word);
                } catch (WordIsNotValid e) {
                    System.out.println("Word invalid:");
                } catch (WordIsShort e) {
                    System.out.println("Word to short;");
                }
            }
        }
        System.out.println(wordy.getRoundWinner(matchId,username) + "is the winner");
        System.out.println(wordy.getWinner(matchId,username));
    }


    /**
     * Starts the game timer which updates the 'timer' variable every 200ms
     */
    private static void startGameTimer() {
        exec = new ScheduledThreadPoolExecutor(1);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                timer = wordy.getTime(matchId);
                if (timer == 0){
                    System.out.println("time Ran Out Stop TYPING");
                    exec.shutdown();
                }
            }
        };
        exec.scheduleAtFixedRate(run, 0, 200, TimeUnit.MILLISECONDS);
    }
}
