package SQLService;

import DataClasses.GameInfo;
import DataClasses.TTT_GameData;
import DataClasses.TTT_ViewerData;
import DataClasses.User;
import Database.DBManager;
import Messages.*;

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

                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "DAC-MSG": // Deactivate Account
                                DeactivateAccountMessage DAC = (DeactivateAccountMessage) ENC.getMsg();


                                break;
                            case "UPA-MSG": // Update Account Info
                                UpdateAccountInfoMessage UPA = (UpdateAccountInfoMessage) ENC.getMsg();



                                AccountSuccessfulMessage ACS = (AccountSuccessfulMessage) MessageFactory.getMessage("ACS-MSG");
                                SQLServer.getInstance().sendPacket(new Packet("ENC-MSG", new EncapsulatedMessage("ACS-MSG", ENC.getidentifier(), ACS)));
                                break;
                            case "GLG-MSG": // Game Log
                                GameLogMessage GLG = (GameLogMessage) ENC.getMsg();



                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "STS-MSG": // Stats
                                StatsMessage STS = (StatsMessage) ENC.getMsg();



                                SQLServer.getInstance().sendPacket(packet);
                                break;
                        }
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                             Login
                    //--------------------------------------------------------------------------------------------------
                    case "LOG-MSG": // login
                        LoginMessage LOG = (LoginMessage) packet.getData();



                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                        Create Account
                    //--------------------------------------------------------------------------------------------------
                    case "CAC-MSG": // create account
                        CreateAccountMessage CAC = (CreateAccountMessage) packet.getData();

                        List<Object> users = DBManager.getInstance().list(User.class);
                        boolean CAC_Failed = false;

                        for(Object obj: users) {
                            User user = (User) obj;
                            if(user.getUsername() == CAC.getNewUser().getUsername()) {
                                SQLServer.getInstance().sendPacket(new Packet("ACF-MSG",
                                        (AccountFailedMessage) MessageFactory.getMessage("ACF-MSG")));
                                CAC_Failed = true;
                                break;
                            }
                        }

                        if(!CAC_Failed) {
                            SQLServer.getInstance().sendPacket(new Packet("ACS-MSG",
                                    (AccountSuccessfulMessage) MessageFactory.getMessage("ACS-MSG")));

                            DBManager.getInstance().insert(CAC.getNewUser());
                        }

                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                          Save Move
                    //--------------------------------------------------------------------------------------------------
                    case "MOV-MSG": // move
                        MoveMessage MOV = (MoveMessage) packet.getData();



                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                          Save Game
                    //--------------------------------------------------------------------------------------------------
                    case "SAV-MSG": // save
                        SaveGameMessage SAV = (SaveGameMessage) packet.getData();



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
