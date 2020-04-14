package GameInterfaces;

import java.io.Serializable;
import java.util.List;
import Game.Move;

public interface Game extends Serializable {
    void startGame() throws Exception;
    void endGame() throws Exception;
    boolean isFinished();
    void saveGame() throws Exception;

    String getGameId();

    void performMove(Move nextMove) throws Exception;
    void undoMove() throws Exception;
    void restart() throws Exception;

    int getNextPlayer();
    int getWinner() throws Exception;

<<<<<<< HEAD
        <T extends Move> List<Move> getMoveHistory();
=======
    <T extends Move> List<T> getMoveHistory();
>>>>>>> origin/Messages

    Board getBoard() throws Exception;

<<<<<<< HEAD
      //  void addGameListener(GameListener listener);
        //void removeGameListener(GameListener listener);
    }

=======
    void addGameListener(GameListener listener);
    void removeGameListener(GameListener listener);
}
>>>>>>> origin/Messages
