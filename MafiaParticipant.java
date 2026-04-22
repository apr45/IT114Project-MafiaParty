import java.net.*;
import java.io.*;
import java.util.Scanner;
//
public class MafiaParticipant{
    // input and output streams
    static private BufferedReader incomingStream = null;
    static private PrintWriter outgoingStream = null;
    static private String incomingText, outgoingText;

    static Thread clientText;

    // scanner for user input
    static private Scanner input = new Scanner(System.in);

    public static void main(String[] args) throws IOException{
        // connection variables
        Socket connection = null;
        final String LOOPBACKADDRESS = "localhost";
        final int PORT = 2005;

        // inital username input and server connection
            String username; // stores client's username

            System.out.print("Enter your name: "); // prompts client to enter username

            // ensures valid username before connecting to server
            while (true) {
                username = input.nextLine(); // gets client's username input

                // checks if username is in proper format
                if (usernameValidation(username)){
                    try {
                        connection = new Socket(LOOPBACKADDRESS, PORT); // creates socket connection to server

                        // input and output streams
                        outgoingStream = new PrintWriter(connection.getOutputStream(), true);
                        incomingStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    } catch(SocketException e) {
                        // signals client that server can not accept any more connections
                        System.out.println("Server reached max players. Not accepting new players.");
                        System.exit(0);
                    }
                    break;
                }
            }
        
        // sends initial username input to server
        outgoingStream.println(username);

        // username duplication check
        while (true) {
            // receives server's response on whether username is valid or not to use
            incomingText = incomingStream.readLine();
            if (incomingText.equals("INVALID")){
                System.out.print("Name already in use. Please enter a different name: "); // prompts client to enter a different username

                // ensures valid username before sending to server
                while (true){
                    username = input.nextLine();
                    if (usernameValidation(username)){
                        outgoingStream.println(username);
                        break;
                    }
                }
            } else if (incomingText.equals("VALID")){
                break;
            }
        }

        // greets client and confirms connection to server
        incomingText = incomingStream.readLine();
        System.out.println("\nHello " + incomingText + "!");
        System.out.println("You are connected. Waiting for other players to join...");

        // waits until all players connect to server before starting game
        incomingText = incomingStream.readLine();
        System.out.println("All players joined! Starting game..");
        System.out.println("Your role is: " + incomingText);

        // night and day states
            while (true){
                // recieves type of game state from server
                incomingText = incomingStream.readLine();

                // initiates game state
                if (incomingText.equals("NIGHTSTATE")){
                    clientNightState();
                } else if (incomingText.equals("DAYSTATE")) {
                    clientDayState();
                } else {
                    break;
                }
        }

        try{
                input.close();
                connection.close();
                if (incomingStream != null && outgoingStream != null){
                    incomingStream.close();
                    outgoingStream.close();
            }
            } catch(Exception e){

            }
    }

    // method to validate client's username input
    public static boolean usernameValidation(String username) {
        username = username.trim(); // remove leading and trailing whitespace

        // avoids empty usernames
        if (username.equals("")) {
            System.out.print("Name cannot be empty. Please enter a valid name: ");
            return false;
        } else {
            return true;
        }
    }

    // night state for the client side
    public static void clientNightState() throws IOException {
        // signals client the beginning of night time
        System.out.println("\nNight has fallen. Civilians fall asleep as Mafia choses their victim.");

        try {

            incomingText = incomingStream.readLine();
            
            if (incomingText.equals("Mafia")){
                System.out.println("--List of Alive Players--");

                incomingText = incomingStream.readLine();
                System.out.println(incomingText);

                System.out.print("Choose player to elimate: ");
                outgoingText = input.nextLine();
                outgoingStream.println(outgoingText);
               
            } else if (incomingText.equals("Civilian")){
                System.out.println("Waiting for Mafia to choose victim...");
            }

            incomingText = incomingStream.readLine();
            System.out.println(incomingText);
        } catch(Exception e) {
            e.printStackTrace();
        }
    } 


    // day state for the client side
    public static void clientDayState() throws IOException{
        // signals players that daytime has come and preps for dissussion time
        System.out.println("\nDay has dawned. Discuss who is the Mafia.");
        System.out.println("--Chat Room--");

        clientMessages();
        groupMessages();

        System.out.println("Times up! Vote who you think is Mafia.");
    }
    
    
    public static void groupMessages() {
        try{
            while (true) {
                incomingText = incomingStream.readLine();
                if (incomingText.equals("EXIT")){
                    break;
                } else 
                    System.out.println(incomingText);
            }
            clientText.interrupt();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void clientMessages(){
        clientText = new Thread(() -> {
            try{
                while(!Thread.currentThread().isInterrupted()){
                    outgoingText = input.nextLine();
                    outgoingStream.println(outgoingText);
                }
            } catch (Exception e){
                System.out.println("End of Discussion");
                Thread.currentThread().interrupt(); 
            }
        });
        
        clientText.start();
    }
}