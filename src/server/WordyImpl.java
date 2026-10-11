package server;

import common.wordyApp.*;

import java.time.LocalTime;
import java.util.*;


public class WordyImpl extends WordyPOA {

    static int currentGame = 1000;
    static Game game;
    static Thread timer;
    static Map<Integer,Timer> roundTimer = new HashMap<>();
    static Map<Integer,Integer> remainingTime = new HashMap<>();
    static Map<Integer,Integer> timerManager = new HashMap<>();
    static ArrayList<Round> roundManager = new ArrayList<>();
    static ArrayList<Player> playersInWaiting = new ArrayList<>();
    static HashMap<Integer,HashMap<String,String>> wordBank = new HashMap<>();
    static Map<Integer,Boolean> roundFinished = new HashMap<>();
    static Map<Integer,String> lastRoundWinner = new HashMap<>();
    static Map<Integer,String> gameWinner = new HashMap<>();
    static final int ROUND_SECONDS = 15;
    static final int ROUND_BREAK_SECONDS = 3;
    DatabaseProcessor dbp;

    @Override
    public Player login(String username, String password) throws AccountIsLoggedIn, AccountNotFound, WrongPassword {
        dbp = new DatabaseProcessor();
        for (Player p : dbp.getPlayers()){
            if (p.username.equals(username)){
                if (p.password.equals(password)){
                    if (p.isActive){
                        // If the user is already logged in, throw an AccountIsLoggedIn exception
                        throw new AccountIsLoggedIn("Account Already Logged In");

                    } else {
                        // If the user is not logged in, change their status to "active"
                        dbp.updateStatus(p.username, true);
                        return p;
                    }
                } else {
                    // If the password is incorrect, throw a WrongPassword exception
                    throw new WrongPassword("Wrong Password Entered");
                }
            }
        }
        // If the username is not found in the list of credentials, throw an AccountNotFound exception
        throw new AccountNotFound("Account Not Found");

    }

    @Override
    public boolean logout(String username) {
        dbp.updateStatus(username, false);
        return true;
    }


    @Override
    public int play(String username) throws NoOtherPlayersAvailable {
        // If no game is currently ongoing or the game has not started yet
        if (game == null || game.started == true) {
            // Create a new game object
            game = new Game();

            // Add the current player to the list of players waiting to join the game
            playersInWaiting.add(findPlayer(username));

            // Create a new game in the database for the current player
            createGame(username);
            System.out.println("Game Created");

            // Set a timer of 10 seconds before the game starts
            timer = new Thread(() -> {
                try {
                    int x = 15;
                    while (x!=-1){
                        Thread.sleep(1000);
                        System.out.println(x);
                        x--;
                    }
                    game.started = true;

                    // Assign a set of letters to the round
                    asignLetterSetToRound(game.gameID);

                    // Interrupt the timer thread to stop the timer
                    timer.interrupt();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            System.out.println("Here");
            // Start the timer thread
            timer.start();

            // Wait for the timer thread to finish
            while (timer.isAlive()){

            }

            // Set the players of the game to the list of players waiting to join
            game.players = playersInWaiting.toArray(new Player[playersInWaiting.size()]);

            // Create a new list to store the player records for the current round
            ArrayList<PlayerRecord> pr = new ArrayList<>();

            // If there are at least two players waiting to join the game
            if (playersInWaiting.size() > 1) {

                // Increment the current game counter
                currentGame++;

                timerManager.put(game.gameID, LocalTime.now().getSecond());
                // Create a new round in the database for the game
                // Set the timer for the game to 15 seconds
                createRound(game.gameID, username);

                // Start the game timer thread
                playersInWaiting = new ArrayList<>();
                // Return the ID of the game
                return game.gameID;
            }
            // If there is only one player waiting to join the game
            else {
                // Reset the list of players waiting to join
                playersInWaiting = new ArrayList<>();

                // Set the game object to null to indicate that no game is currently ongoing
                game = null;

                // Throw an exception to indicate that no other players are available to join the game
                throw new NoOtherPlayersAvailable("No other players");
            }
        }
        // If a game is currently ongoing and the current player is waiting to join the game
        else {
            // Add the current player to the list of players waiting to join the game
            playersInWaiting.add(findPlayer(username));
            System.out.println("Player in waiting");
            // Wait for the timer thread to finish
            while (timer.isAlive()) {
            }

            // Return the ID of the ongoing game
            return game.gameID;
        }

    }

    @Override
    public int updateWin(String username, int win){
        return dbp.updateWin(username, win);
    }

    public String updateLongestWord(String username, String word){
        return dbp.updateLongestWord(username, word);
    }


    private void asignLetterSetToRound(int gameID) {

    }

    @Override
    public String getLetterSet(int gameID) {
        for (Round round : roundManager){
            if (round.gameID==gameID){
                return round.letterSet;
            }
        }
        return null;
    }

    public String generateRandomLetters() {
        //Define vowels and consonants
        String vowels = "aeiou";
        String consonant = "bcdfghjklmnpqrstvwxyz";
        //Create a StringBuilder object to store the randomly generated letters
        StringBuilder sb = new StringBuilder();
        //Generate 17 random letters
        for(int x = 0; x < 17; x++){
            if (x < 5){
                //Add a random vowel to the StringBuilder object
                sb.append(vowels.charAt(new Random().nextInt(vowels.length())));
            }else {
                //Add a random consonant to the StringBuilder object
                sb.append(consonant.charAt(new Random().nextInt(consonant.length())));
            }
        }
        //Return the randomly generated letters as a String
        return sb.toString();
    }
    @Override
    public boolean submitWord(int gameID, String username, String word) throws WordIsNotValid, WordIsShort {
        //Throw a WordIsShort exception if the submitted word is less than 5 characters
        if (word.length()<5){
            throw new WordIsShort("Word too short");
        }
        //Throw a WordIsNotValid exception if the submitted word is not in the main.words list
        if (!dbp.getWords().contains(word)){
            throw new WordIsNotValid("Invalid word");
        }
        synchronized (WordyImpl.class) {
            //Check if the gameID is in the wordBank HashMap, and add it if it's not
            if (!wordBank.containsKey(gameID)){
                wordBank.put(gameID,new HashMap<>());
            }
            //Check if the username is in the wordBank HashMap for this game, and add it if it's not
            if (!wordBank.get(gameID).containsKey(username)){
                wordBank.get(gameID).put(username,new String());
            }
            //Throw a WordIsNotValid exception if the time for this round has run out
            if (getTime(gameID)==0){
                throw new WordIsNotValid("Invalid Word");
            }
            //Update the user's submitted word if it's longer than their previous submission
            if (wordBank.get(gameID).get(username).length()<word.length()){
                wordBank.get(gameID).put(username,word);
            }
        }
        //Return true to indicate that the word was successfully submitted
        return true;
    }



    /**
     * Returns the winner of the most recently finished round, or "no winner".
     * Rounds are finished by the server's round timer (see tickRound), so this has no side effects
     * and every client gets the same answer no matter when it asks.
     */
    @Override
    public String getRoundWinner(int gameID,String username) {
        synchronized (WordyImpl.class) {
            String winner = lastRoundWinner.get(gameID);
            return winner == null ? "no winner" : winner;
        }
    }


    // This method takes in a HashMap of players and their words, and returns the player with the longest word as the winner.
    // Returns "no winner" if nobody submitted a word or if two or more players tie for the longest word.
    private String getRoundWin(HashMap<String, String> stringStringHashMap) {
        if (stringStringHashMap == null) return "no winner";
        String winnerUser = null;
        int longest = 0;
        boolean tie = false;
        for (Map.Entry<String, String> entry : stringStringHashMap.entrySet()) {
            int length = entry.getValue().length();
            if (length > longest) {
                longest = length;
                winnerUser = entry.getKey();
                tie = false;
            } else if (length == longest && length > 0) {
                tie = true;
            }
        }
        if (winnerUser == null || tie) {
            return "no winner";
        }
        return winnerUser;
    }

    /**
     * Returns the username of the game winner (first player to 3 round wins), or "no winner" while the game is still going.
     */
    @Override
    public String getWinner(int gameID, String username) {
        synchronized (WordyImpl.class) {
            String winner = gameWinner.get(gameID);
            return winner == null ? "no winner" : winner;
        }
    }

    /**
     * Runs on the round timer. Ends the round when time runs out (scores the round winner and checks for a game winner),
     * keeps the round ended for ROUND_BREAK_SECONDS so every client notices, then starts the next round.
     */
    private void tickRound(int gameID) {
        synchronized (WordyImpl.class) {
            int currentTime = LocalTime.now().getSecond();
            int elapsedTime = (currentTime >= timerManager.get(gameID)) ? currentTime - timerManager.get(gameID) : 60 - timerManager.get(gameID) + currentTime;
            int remaining = ROUND_SECONDS - elapsedTime;
            remainingTime.put(gameID, Math.max(remaining, 0));

            Round round = findRound(gameID);
            if (round == null) return;

            if (remaining <= 0 && !roundFinished.getOrDefault(gameID, false)) {
                roundFinished.put(gameID, true);
                String winner = getRoundWin(wordBank.get(gameID));
                lastRoundWinner.put(gameID, winner);
                System.out.println("Round winner: " + winner);
                for (PlayerRecord p : round.players) {
                    if (p.username.equals(winner)) {
                        p.wins += 1;
                    }
                    if (p.wins >= 3) {
                        gameWinner.put(gameID, p.username);
                        round.winnerUsername = p.username;
                    }
                }
                if (gameWinner.containsKey(gameID)) {
                    System.out.println("Game winner: " + gameWinner.get(gameID));
                    roundTimer.get(gameID).cancel();
                }
            } else if (remaining <= -ROUND_BREAK_SECONDS && roundFinished.getOrDefault(gameID, false)) {
                // start the next round
                roundFinished.put(gameID, false);
                timerManager.put(gameID, currentTime);
                remainingTime.put(gameID, ROUND_SECONDS);
                if (wordBank.containsKey(gameID)) {
                    for (String user : wordBank.get(gameID).keySet()) {
                        wordBank.get(gameID).put(user, "");
                    }
                }
                round.letterSet = generateRandomLetters();
                System.out.println("New round letters: " + round.letterSet);
            }
        }
    }

    private Round findRound(int gameID) {
        for (Round r : roundManager) {
            if (r.gameID == gameID) {
                return r;
            }
        }
        return null;
    }

    @Override
    public Player[] getLeaderBoardsWins() {
        return dbp.boardWins();
    }

    @Override
    public Player[] getLeaderBoardsWords() {
        return dbp.boardWords();
    }

    @Override
    public int getTime(int gameID) {
        synchronized (WordyImpl.class) {
            return remainingTime.get(gameID);
        }
    }

    @Override
    public boolean isGameCreator(String username) {
        for (Round r : roundManager){
            return r.gameCreator.equals(username);
        }
        return false;
    }

    @Override
    public PlayerRecord[] getRoundRecords(int gameID) {
        for (Round round : roundManager){
            if (round.gameID == gameID){
                return round.players;
            }
        }
        return null;
    }


    private void createGame(String username) {
        game.gameID=currentGame;
        game.ended=false;
        game.started=false;
        game.winnerUsername=null;
        game.startTime= System.currentTimeMillis();
    }

    private Player findPlayer(String username) {
        for (Player p : dbp.getPlayers()){
            if (p.username.equals(username)){
                return p;
            }
        }
        return null;
    }

    /**
     * Creates a new round for the given game, initializes player records, and adds it to the round manager
     *
     * @param gameID the ID of the game
     * @param username the creator of the round
     */
    private void createRound(int gameID,String username) {
        PlayerRecord[] pr =new PlayerRecord[playersInWaiting.size()];
        for (int x = 0; x < playersInWaiting.size();x++){
            pr[x] = new PlayerRecord(playersInWaiting.get(x).username,0);
        }
        // generate random letters and add a new round to the round manager
        remainingTime.put(gameID, ROUND_SECONDS);
        roundFinished.put(gameID, false);
        roundManager.add(new Round(gameID, null, pr, generateRandomLetters(),username));

        // the round timer drives the whole game: it ends rounds, scores them and starts the next one
        roundTimer.put(gameID,new Timer(true));
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                tickRound(gameID);
            }
        };
        roundTimer.get(gameID).scheduleAtFixedRate(task,0,200);
    }

}
