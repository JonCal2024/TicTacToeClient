package DataClasses;

import java.time.LocalDateTime;
import java.util.UUID;

public class TTT_GameData {
    private String id;
    private LocalDateTime startingTime;
    private LocalDateTime endTime;
    private int player1Id;
    private int player2Id;
    private int startingPlayerId;
    private int winningPlayerId;

    public TTT_GameData(String id, LocalDateTime startingTime, int player1Id, int player2Id, int startingPlayerId) {
        this.id = id;
        this.startingTime = startingTime;
        this.endTime = null;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.startingPlayerId = startingPlayerId;
        this.winningPlayerId = -1;
    }

    public TTT_GameData(String id, LocalDateTime startingTime, LocalDateTime endTime, int player1Id, int player2Id, int startingPlayerId, int winningPlayerId) {
        this.id = id;
        this.startingTime = startingTime;
        this.endTime = endTime;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.startingPlayerId = startingPlayerId;
        this.winningPlayerId = winningPlayerId;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getPlayer1Id() {
        return player1Id;
    }

    public int getPlayer2Id() {
        return player2Id;
    }

    public int getStartingPlayerId() {
        return startingPlayerId;
    }

    public int getWinningPlayerId() {
        return winningPlayerId;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setWinningPlayerId(int winningPlayerId) {
        this.winningPlayerId = winningPlayerId;
    }
}
