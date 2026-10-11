package client.controller.leaderboards;


public class PlayerStandingRound {
    public int playerScore;
    public String playerName;

    public PlayerStandingRound(String playerName, int playerScore){
        this.playerScore = playerScore;
        this.playerName = playerName;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public String getPlayerName() {
        return playerName;
    }

}
