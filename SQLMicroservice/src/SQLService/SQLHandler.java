package SQLService;

import DataClasses.*;
import Database.DBManager;
import Messages.*;
import TicTacToe.TTT_Move;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SQLHandler implements Runnable{
    private static SQLHandler instance = new SQLHandler();

    private BlockingQueue<Packet> requests;
    private Thread thread;

    private SQLHandler() {
        requests = SQLServer.getInstance().getRequests();

        thread = new Thread(this);
        thread.run();
    }

    public static SQLHandler getInstance() {return instance;}

    @Override
    public void run() {
        try {
            while(!thread.isInterrupted()) {
                Packet packet = requests.take();

                switch(packet.getType()) {
                    case "ENC-MSG": // encapsulated message
                        EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                        switch (ENC.getType()) {
                            case "GMP-MSG": // Games played
                                GamesPlayedMessage GMP = (GamesPlayedMessage) ENC.getMsg();

                                // Update Message with list of gameinfo
                                List<Object> games = DBManager.getInstance().list(TTT_GameData.class);
                                List<GameInfo> infoList = new ArrayList<>();
                                for(Object obj: games) {
                                    TTT_GameData game = (TTT_GameData) obj;
                                    User second_player = null;
                                    if(game.getPlayer1Id() == GMP.getPlayerId())
                                        second_player = (User) DBManager.getInstance().get(User.class, String.valueOf(game.getPlayer1Id()));
                                    else if(game.getPlayer2Id() == GMP.getPlayerId())
                                        second_player = (User) DBManager.getInstance().get(User.class, String.valueOf(game.getPlayer1Id()));
                                    infoList.add(new GameInfo(second_player.getUsername(), game.getStartingTime(), game.getId()));
                                }
                                GMP.setGameInfoList(infoList);

                                // Return to MainServer
                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "GVW-MSG": // Game Viewers
                                GameViewersMessage GVW = (GameViewersMessage) ENC.getMsg();

                                List<Object> viewers = DBManager.getInstance().query(TTT_ViewerData.class, GVW.getGameId());
                                List<Spectator> spectators = new ArrayList<>();

                                for(Object obj : viewers) {
                                    User viewer = (User) DBManager.getInstance().get(User.class, String.valueOf(((TTT_ViewerData) obj).getViewer_id()));
                                    spectators.add(new Spectator(viewer.getUsername()));
                                }

                                GVW.setSpectators(spectators);
                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "DAC-MSG": // Deactivate Account
                                DeactivateAccountMessage DAC = (DeactivateAccountMessage) ENC.getMsg();

                                User deactivatedAccount = (User) DBManager.getInstance().get(User.class, String.valueOf(DAC.getUserId()));

                                deactivatedAccount.setIsActive(false);

                                DBManager.getInstance().update(deactivatedAccount);
                                break;
                            case "UPA-MSG": // Update Account Info
                                UpdateAccountInfoMessage UPA = (UpdateAccountInfoMessage) ENC.getMsg();

                                List<Object> users = DBManager.getInstance().list(User.class);
                                boolean UAC_Failed = false;

                                for(Object obj: users) {
                                    User user = (User) obj;
                                    if(user.getUsername() == UPA.getUpdatedUser().getUsername()) {
                                        AccountFailedMessage ACF = (AccountFailedMessage) MessageFactory.getMessage("ACF-MSG");
                                        SQLServer.getInstance().sendPacket(new Packet("ENC-MSG", new EncapsulatedMessage("ACF-MSG", ENC.getidentifier(), ACF)));
                                        UAC_Failed = true;
                                        break;
                                    }
                                }

                                if(!UAC_Failed) {
                                    AccountSuccessfulMessage ACS = (AccountSuccessfulMessage) MessageFactory.getMessage("ACS-MSG");
                                    SQLServer.getInstance().sendPacket(new Packet("ENC-MSG", new EncapsulatedMessage("ACS-MSG", ENC.getidentifier(), ACS)));

                                    DBManager.getInstance().update(UPA.getUpdatedUser());
                                }

                                break;
                            case "GLG-MSG": // Game Log
                                GameLogMessage GLG = (GameLogMessage) ENC.getMsg();

                                TTT_GameData gameData = (TTT_GameData) DBManager.getInstance().get(TTT_GameData.class, GLG.getGameId());

                                GLG.setGameStarted(gameData.getStartingTime());
                                GLG.setGameEnded(gameData.getEndTime());

                                User player = (User) DBManager.getInstance().get(User.class, String.valueOf(gameData.getPlayer1Id()));
                                GLG.setPlayer1Username(player.getUsername());
                                player = (User) DBManager.getInstance().get(User.class, String.valueOf(gameData.getPlayer2Id()));
                                GLG.setPlayer2Username(player.getUsername());
                                player = (User) DBManager.getInstance().get(User.class, String.valueOf(gameData.getWinningPlayerId()));
                                GLG.setWinner(player.getUsername());

                                List<Object> moveData = DBManager.getInstance().query(TTT_MoveData.class, GLG.getGameId());
                                List<MoveInfo> moves = new ArrayList<>();

                                int playerNum = 1;

                                for(Object obj : moveData) {
                                    TTT_MoveData move = (TTT_MoveData) obj;

                                    moves.add(new MoveInfo(new TTT_Move((playerNum == 1 ? playerNum++ : playerNum--), move.getRow(), move.getColumn()), move.getTime()));
                                }

                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "STS-MSG": // Stats
                                StatsMessage STS = (StatsMessage) ENC.getMsg();



                                SQLServer.getInstance().sendPacket(packet);
                                break;

                            //------------------------------------------------------------------------------------------
                            //                                        Create Account
                            //------------------------------------------------------------------------------------------
                            case "CAC-MSG": // create account
                                CreateAccountMessage CAC = (CreateAccountMessage) ENC.getMsg();

                                users = DBManager.getInstance().list(User.class);
                                boolean CAC_Failed = false;
                                for(Object obj: users) {
                                    User user = (User) obj;
                                    if(user.getUsername() == CAC.getNewUser().getUsername()) {
                                        AccountFailedMessage ACF = (AccountFailedMessage) MessageFactory.
                                            getMessage("ACF-MSG");
                                        SQLServer.getInstance().sendPacket(new Packet("ENC-MSG",
                                                new EncapsulatedMessage("ACF-MSG", ENC.getidentifier(), ACF)));
                                        CAC_Failed = true;
                                        break;
                                    }
                                }

                                if(!CAC_Failed) {
                                    AccountSuccessfulMessage ACS = (AccountSuccessfulMessage) MessageFactory.
                                            getMessage("ACS-MSG");
                                    SQLServer.getInstance().sendPacket(new Packet("ENC-MSG",
                                            new EncapsulatedMessage("ACS-MSG", ENC.getidentifier(), ACS)));
                                    SQLServer.getInstance().sendPacket(packet);
                                    DBManager.getInstance().insert(CAC.getNewUser());
                                }
                                break;

                            //------------------------------------------------------------------------------------------
                            //                                             Login
                            //------------------------------------------------------------------------------------------
                            case "LOG-MSG": // login
                                LoginMessage LOG = (LoginMessage) packet.getData();

                                users = DBManager.getInstance().list(User.class);
                                boolean LOG_Failed = true;
                                for(Object obj: users) {
                                    User user = (User) obj;
                                    if(user.getUsername() == LOG.getUsername() && user.getPassword() == LOG.getPassword()) {
                                        // Send Login Success
                                        LoginSuccessfulMessage LOS = (LoginSuccessfulMessage) MessageFactory.getMessage("LOS-MSG");
                                        LOS.setUser(user);
                                        SQLServer.getInstance().sendPacket(new Packet("ENC-MSG",
                                                new EncapsulatedMessage("LOS-MSG", ENC.getidentifier(), LOS)));
                                        LOG_Failed = false;
                                        break;
                                    }
                                }

                                if(LOG_Failed) {
                                    // Send Login Failed
                                    LoginFailedMessage LOF = (LoginFailedMessage) MessageFactory.getMessage("LOF-MSG");
                                    SQLServer.getInstance().sendPacket(new Packet("ENC-MSG",
                                            new EncapsulatedMessage("LOF-MSG", ENC.getidentifier(), LOF)));
                                }

                                break;
                        }
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                          Save Move
                    //--------------------------------------------------------------------------------------------------
                    case "MOV-MSG": // move
                        MoveMessage MOV = (MoveMessage) packet.getData();
                        TTT_MoveData move = new TTT_MoveData(MOV.getGameId(), MOV.getMovingPlayerId(),
                                MOV.getMoveInfo().getTimeMade(), MOV.getMoveInfo().getNextMove().getRow(),
                                MOV.getMoveInfo().getNextMove().getColumn(), 0);
                        DBManager.getInstance().insert(move);
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                          Save Game
                    //--------------------------------------------------------------------------------------------------
                    case "SAV-MSG": // save
                        SaveGameMessage SAV = (SaveGameMessage) packet.getData();
                        if(SAV.isInsert())
                            DBManager.getInstance().insert(SAV.getGame());
                        else if(SAV.isUpdate())
                            DBManager.getInstance().update(SAV.getGame());
                        break;
                }
            }
        } catch (InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public synchronized void terminateServer() {
        thread.interrupt();
        requests = null;
    }
}
