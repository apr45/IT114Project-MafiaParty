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

            outgoingStream.println("Hello " + incomingText + "!"); // greets client and confirm connection

            // set up client's role and status
            String assignedRole = ServerModerator.setup(incomingText); 
            outgoingStream.println(assignedRole); 
        } catch (IOException e){

        }
    }
}