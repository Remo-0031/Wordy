package client.controller.leaderboards;

public class PlayerWins {

    private String username;
    private int wins;

    public PlayerWins(String username, int wins) {
        this.username = username;
        this.wins = wins;
    }

    public String getUsername() {
        return username;
    }

    public int getWins() {
        return wins;
    }

}
