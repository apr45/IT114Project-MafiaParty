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
                    // informs client to the current state and resends role
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
                        ServerModerator.clientWaitingRoom();
                    }

                    ServerModerator.playersCount--;
                    outgoingStream.println("Night time has ended.");
                } else if (ServerModerator.gameState.equals("DAYSTATE")){
                    outgoingStream.println(ServerModerator.gameState);

                    while (true){
                        if (ServerModerator.timer == 1){
                            incomingText = incomingStream.readLine();
                            ServerModerator.broadcast(username, incomingText);
                        } else {
                            outgoingStream.println("EXIT");
                            break;
                        }
                    }
                } else if (ServerModerator.gameState.equals("ENDSTATE")) {

                }
            }

        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}