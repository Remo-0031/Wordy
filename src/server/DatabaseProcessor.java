package server;

import common.wordyApp.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseProcessor {
    private Connection con;

    /**
     * Default constructor for DBConnection
     */
    public DatabaseProcessor(){setCon();}

    /**
     * This method connects the program to the database using the jdbc mysql driver
     */
    public void setCon(){
        try{
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/wordybase", "root", "");
            System.out.println("Connected to Database");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Player> getPlayers() {
        try {
            Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = st.executeQuery("Select * from players");
            ArrayList players = new ArrayList<>();
            while (rs.next()) {
                players.add(new Player(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getBoolean(5), rs.getInt(6), rs.getString(7)));
            }
            return players;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> getWords() {
        try {
            Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = st.executeQuery("SELECT * from words");
            ArrayList<String> words = new ArrayList<>();
            while (rs.next()) {
                words.add(rs.getString(1));
            }
            return words;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateStatus(String username, boolean active) {
        try {
            String query = "UPDATE `players` SET `isActive` = ? WHERE `username` = ? ";
            PreparedStatement ps = con.prepareStatement(query, ResultSet.CONCUR_UPDATABLE, ResultSet.TYPE_SCROLL_INSENSITIVE);
            ps.setBoolean(1, active);
            ps.setString(2, username);
            ps.execute();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public Player[] boardWins(){
        List<Player> playersList = new ArrayList<>();

        try {
            String query = "" +
                    "SELECT " +
                    "`username`, " +
                    "`firstname`, " +
                    "`lastname`, " +
                    "`wins`, " +
                    "`longestWord` " +
                    "FROM `players` " +
                    "ORDER BY `wins` DESC";

            Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String username = resultSet.getString(1);
                String firstname = resultSet.getString(2);
                String lastname = resultSet.getString(3);
                int numofWins = resultSet.getInt(4);
                String longestWord = resultSet.getString(5);

                Player player = new Player(username, "", firstname, lastname, false, numofWins, longestWord);
                playersList.add(player);
            };

            return playersList.toArray(new Player[playersList.size()]);

        } catch (SQLException ex) {
            System.out.println("SQL ERROR: " + ex.getMessage());
            return new Player[0];
        }
    }

    public Player[] boardWords(){
        List<Player> playersList = new ArrayList<>();

        try {
            String query = "" +
                    "SELECT `username`, `firstname`, `lastname`, `wins`, `longestWord` FROM `players` " +
                    "ORDER BY LENGTH(`longestWord`) DESC";

            Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String username = resultSet.getString(1);
                String firstname = resultSet.getString(2);
                String lastname = resultSet.getString(3);
                int numofWins = resultSet.getInt(4);
                String longestWord = resultSet.getString(5);

                Player player = new Player(username, "", firstname, lastname, false, numofWins, longestWord);
                playersList.add(player);
            };

            resultSet.close();
            return playersList.toArray(new Player[playersList.size()]);

        } catch (SQLException ex) {
            System.out.println("SQL ERROR: " + ex.getMessage());
            return new Player[0];
        }
    }

    public int updateWin(String username, int wins){
        try {
            String query = "UPDATE `players` SET `wins` = ? WHERE `username` = ? ";
            PreparedStatement ps = con.prepareStatement(query, ResultSet.CONCUR_UPDATABLE, ResultSet.TYPE_SCROLL_INSENSITIVE);
            ps.setInt(1, wins);
            ps.setString(2, username);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wins;
    }

    public String updateLongestWord(String username, String word){
        try {
            // only replace the stored word if the new one is longer
            String q = "UPDATE `players` SET `longestWord` = ? WHERE `username` = ? " +
                    "AND (`longestWord` IS NULL OR CHAR_LENGTH(`longestWord`) < CHAR_LENGTH(?))";
            PreparedStatement qps = con.prepareStatement(q, ResultSet.CONCUR_UPDATABLE, ResultSet.TYPE_SCROLL_INSENSITIVE);
            qps.setString(1, word);
            qps.setString(2, username);
            qps.setString(3, word);
            qps.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return word;
    }

}
