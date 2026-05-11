import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class ClientHandler implements Runnable{
    // socket for client connection
    private Socket clientSocket;

    // client input and output streams
    private BufferedReader incomingStream = null;
    private PrintWriter outgoingStream = null;
    private String incomingText;

    // client information
    private String username;
    private String role;
    private String status;

    // clients information
    private ArrayList<String> usernames;
    private ArrayList<String> statuses;

    // constructor
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try{
            // input and output streams
            incomingStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outgoingStream = new PrintWriter(clientSocket.getOutputStream(), true);

            // prevent clients from using the same username
            while (true){
                incomingText = incomingStream.readLine();
                
                if (incomingText == null) {
                    // client disconnected
                    return;
                } else if (ServerModerator.doubleUsername(incomingText)){
                    // prompt client to enter a different username
                    outgoingStream.println("INVALID");
                } else {
                    // confirm username is valid
                    outgoingStream.println("VALID");
                    username = incomingText;
                    outgoingStream.println(username);
                    break;
                }
            }

            // set up client information and role
            role = ServerModerator.setup(incomingStream, outgoingStream);

            // enters the client into the waiting room
            ServerModerator.clientWaitingRoom();

            // signals client to begin game
                // reveals client the list of players connected
                usernames = ServerModerator.usernamesArrayList();
                outgoingStream.println(usernames);

                // reveals client their role
                outgoingStream.println(role);

                // resets player count
                ServerModerator.updatePlayerCount("subtract");

            // night and day states
            while(true){
                if (ServerModerator.gameState.equals("NIGHTSTATE")){
                    // informs client the current state and resends role
                    outgoingStream.println(ServerModerator.gameState);
                    outgoingStream.println(role);
                    
                    if (role.equals("Mafia")){
                        //reveals list of Civilians alive to Mafia
                        usernames = ServerModerator.alivePlayersList();
                        outgoingStream.println(usernames);

                        // sents Mafia's choice to server
                        incomingText = incomingStream.readLine();
                        ServerModerator.eliminatedPlayer(incomingText);

                        // puts Mafia into the waiting room after choosing player or timer run out
                        ServerModerator.playerCount = ServerModerator.MAX_PLAYERS;
                        ServerModerator.clientWaitingRoom();
                    } else if (role.equals("Civilian")){
                        // puts client with "Civilian" roles in the waiting room
                        ServerModerator.clientWaitingRoom();
                    }

                    // reveals list of all players and their statuses
                    usernames = ServerModerator.usernamesArrayList();
                    statuses = ServerModerator.statusArrayList();
                    outgoingStream.println(usernames);
                    outgoingStream.println(statuses);

                    // indicates the end of night state
                    ServerModerator.updatePlayerCount("subtract");
                    outgoingStream.println("Night time has ended.");
                } else if (ServerModerator.gameState.equals("DAYSTATE")){
                    // informs client the current state 
                    outgoingStream.println(ServerModerator.gameState);
                    status = ServerModerator.getStatus(username);
                    outgoingStream.println(status);

                    // handles recieving and transfering client messages
                    while (true){
                        // stores client input
                        incomingText = incomingStream.readLine();

                        // recieves signal to stop managing messages if timer reaches zero
                        if (incomingText.equals("END")){
                            // signals client to exit chat room
                            outgoingStream.println("EXIT");
                            break;
                        } else if (incomingText.equals("WINDOW_CLOSED")){
                            ServerModerator.clientDisconnected = true;
                            return;
                        } else{
                            // passes client input into a method to share message to other clients
                            ServerModerator.broadcast(username, incomingText);
                        }
                    }
                    
                    // informs client to vote a from a list of alive players
                    outgoingStream.println("Times up! Vote who you think is Mafia.");
                    usernames = ServerModerator.alivePlayersList();
                    outgoingStream.println(usernames);
                    
                    // recives player's vote and sends to server to add vote
                    incomingText = incomingStream.readLine();
                    ServerModerator.addVote(incomingText);

                    // puts client in waiting room until voting is over
                    ServerModerator.updatePlayerCount("add");
                    ServerModerator.clientWaitingRoom();
                        
                    // reveals list of all players and their statuses
                    usernames = ServerModerator.usernamesArrayList();
                    statuses = ServerModerator.statusArrayList();
                    outgoingStream.println(usernames);
                    outgoingStream.println(statuses);

                    // informs client that voting is over and initiates results
                    outgoingStream.println("Voting Over!");
                    ServerModerator.votingResults();
                    ServerModerator.updatePlayerCount("subtract");
                } else if (ServerModerator.gameState.equals("ENDSTATE")) {
                    // informs client the current state 
                    outgoingStream.println(ServerModerator.gameState);
                    
                    // recieves signal to close bridge connection
                    incomingStream.readLine();
                    ServerModerator.updatePlayerCount("subtract");
                    break;
                }
            }
        } catch (Exception e){
            e.getMessage();
        }
    }
}