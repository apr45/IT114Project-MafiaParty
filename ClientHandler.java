import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable{
    // socket for client connection
    private Socket clientSocket;

    // client input and output streams
    private BufferedReader incomingStream = null;
    private PrintWriter outgoingStream = null;
    private static String incomingText;

    // client information
    private String username;
    private String role;

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

            // signals client to begin game and reveals their role
            outgoingStream.println(role);
            ServerModerator.playersCount--;

            // night and day states
            while(true){
                if (ServerModerator.gameState.equals("NIGHTSTATE")){
                    // informs client the current state and resends role
                    outgoingStream.println(ServerModerator.gameState);
                    outgoingStream.println(role);

                    if (role.equals("Mafia")){
                        ServerModerator.alivePlayersList();

                        incomingText = incomingStream.readLine();
                        ServerModerator.targetChoosen = true;

                        ServerModerator.eliminatedPlayer(incomingText);

                        ServerModerator.playersCount = ServerModerator.MAX_PLAYERS;
                        ServerModerator.clientWaitingRoom();
                    } else if (role.equals("Civilian")){
                        // puts client with "Civilian" roles in the waiting room
                        ServerModerator.clientWaitingRoom();
                    }

                    ServerModerator.playersCount--;
                    outgoingStream.println("Night time has ended.");
                } else if (ServerModerator.gameState.equals("DAYSTATE")){
                     // informs client the current state 
                    outgoingStream.println(ServerModerator.gameState);

                    while (true){
                            // stores client input
                            incomingText = incomingStream.readLine();

                        // checks if server timer reached 0
                        if (ServerModerator.timer == 1){
                            // passes client message into a method to share message to other clients
                            ServerModerator.broadcast(username, incomingText);
                        } else {
                            // signals client to exit chat room
                            outgoingStream.println("EXIT");
                            break;
                        }
                    }

                    outgoingStream.println("Times up! Vote who you think is Mafia.");
                } else if (ServerModerator.gameState.equals("ENDSTATE")) {
                    ServerModerator.playersCount++;
                    outgoingStream.println(ServerModerator.gameState);
                }
            }

        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}