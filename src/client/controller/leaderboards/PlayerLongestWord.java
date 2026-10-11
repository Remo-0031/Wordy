package client.controller.leaderboards;

public class PlayerLongestWord {

    private String username;
    private String longestWord;

    public PlayerLongestWord(String username, String longestWord) {
        this.username = username;
        this.longestWord = longestWord;
    }

    public String getUsername() {
        return username;
    }

    public String getLongestWord() {
        return longestWord;
    }

}
