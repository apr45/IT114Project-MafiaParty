import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable{
    // socket for client connection
    private Socket clientSocket;

    // client input and output streams
    private BufferedReader incomingStream = null;
    public PrintWriter outgoingStream = null;

    // client information
    private String username;
    private String role;

    static String incomingText = null;

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
                    return; // client disconnected
                } else if (ServerModerator.doubleUsername(incomingText)){
                    outgoingStream.println("INVALID"); // prompt client to enter a different username
                } else {
                    outgoingStream.println("VALID"); // confirm username is valid
                    username = incomingText; // set client's username
                    break;
                }
            }

            role = ServerModerator.setup(incomingStream, outgoingStream); // set up client's role and status
 
            outgoingStream.println("Hello " + username + "!"); // greets client and confirm connection

            // enters the waiting room
            outgoingStream.println("You are connected. Waiting for other players to join...");
            ServerModerator.clientWaitingRoom();

            // game starts once all players have joined
            outgoingStream.println("All players joined! Starting game...");
            outgoingStream.println(role);
            ServerModerator.playersCount--;

            // night and day phases
            while(true){
                if (ServerModerator.gameState.equals("DAYPHASE")){
                    while (true){
                        incomingText = incomingStream.readLine();
                        ServerModerator.broadcast(username, incomingText);
                    }
                    //outgoingStream.println("Time to vote!");
                }
            }
               
            
            //outgoingStream.println("Daytime has ended.");

        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}