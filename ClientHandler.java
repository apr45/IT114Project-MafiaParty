import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable{
    private Socket clientSocket;

    // constructor
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }


    @Override
    public void run() {
        try{
            // input and output streams
            BufferedReader incomingStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter outgoingStream = new PrintWriter(clientSocket.getOutputStream(), true);
            String incomingText = null;


            // prevent clients from using the same username
            while (true){
                incomingText = incomingStream.readLine();
                
                if (incomingText == null) {
                    return; // client disconnected
                } else if (ServerModerator.doubleUsername(incomingText)){
                    outgoingStream.println("INVALID"); // prompt client to enter a different username
                } else {
                    outgoingStream.println("VALID"); // confirm username is valid
                    break;
                }
            }

            String assignedRole = ServerModerator.setup(incomingText); // set up client's role and status
 
            outgoingStream.println("Hello " + incomingText + "!"); // greets client and confirm connection

            // enters the waiting room
            outgoingStream.println("You are connected. Waiting for other players to join...");
            ServerModerator.clientWaitingRoom();

            outgoingStream.println("All players joined! Starting game...");

        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}