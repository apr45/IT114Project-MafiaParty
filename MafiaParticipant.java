import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

public class MafiaParticipant{
    // input and output streams
    private static BufferedReader incomingStream = null;
    private static PrintWriter outgoingStream = null;
    private static String incomingText, outgoingText;

    // GUI client window
    private static JFrame gameWindow = new JFrame("Mafia Party");;

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
            // gets array of all players connected
            incomingText = incomingStream.readLine();
            String usernames = incomingText.replace("[", "").replace("]", "");
            String[] usernamesArray = usernames.split(", ");

            // informs client that all players connected
            System.out.println("\nAll players joined! Starting game..");

        // sets up GUI window for client side
            // window size and close operation
            gameWindow.setSize(500,500);
            gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // header
            JPanel header = new JPanel();
            JLabel title = new JLabel("List of Players:");
            header.add(title);
            gameWindow.add(header, BorderLayout.NORTH);

            // player list
                // sets layout of player list
                JPanel playerList = new JPanel();
                playerList.setLayout(new GridLayout(usernamesArray.length, 1, 5, 0));

                // resize alive icon image
                ImageIcon aliveIcon = new ImageIcon("alive.png");
                Image aliveImage = aliveIcon.getImage();
                Image resizedAliveImage = aliveImage.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
                aliveIcon = new ImageIcon(resizedAliveImage);

                // inserts label of each player into the player list panel
                for (int i = 0; i < usernamesArray.length; i++){
                    playerList.add(new JLabel(usernamesArray[i], aliveIcon, JLabel.LEFT));
                }
            
                // adds player list panel to game window
                gameWindow.add(playerList, BorderLayout.CENTER);
            
            // role
                JPanel clientRole = new JPanel();
                String role = incomingStream.readLine();
                clientRole.add(new JLabel("Your role is: " + role));
                gameWindow.add(clientRole, BorderLayout.SOUTH);

            // makes GUI window visible to client
            gameWindow.setVisible(true);

        // night and day states
            while (true){
                // recieves type of game state from server
                incomingText = incomingStream.readLine();

                // initiates game state
                if (incomingText.equals("NIGHTSTATE")){
                    clientNightState();
                } else if (incomingText.equals("DAYSTATE")) {
                    clientDayState();
                } else if (incomingText.equals("ENDSTATE")){
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
        System.out.println("\nNight has fallen. Civilians fall asleep as Mafia chooses their victim.");

        try {
            // block until recieves client role
            incomingText = incomingStream.readLine();
            
            // compares client role to initate proper 
                if (incomingText.equals("Mafia")){
                    // displays a list of players with "Alive" status
                    System.out.println("--List of Alive Players--");
                    incomingText = incomingStream.readLine();
                    System.out.println(incomingText);

                    // asks client to input a person to elimate
                    System.out.print("Choose player to elimate: ");
                    outgoingText = "NONE";
                    
                    while (ServerModerator.timer == 1){
                        if (System.in.available() > 0){
                            outgoingText = input.nextLine();
                            break;
                        }
                    }
                    outgoingStream.println(outgoingText);
               
                } else if (incomingText.equals("Civilian")){
                    // puts client in a waiting state until timer reaches 0
                    System.out.println("Waiting for Mafia to choose victim...");
                }
            
            // waits until recieves signal that night time has ended
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

        // initates global chat room
        clientMessages();
        groupMessages();

        incomingText = incomingStream.readLine();
        System.out.println(incomingText);
    }
    
    // display message from other clients
    public static void groupMessages() {
        try{
            while (true) {
                // waits until recieve a client message to display
                incomingText = incomingStream.readLine();

                if (incomingText.equals("EXIT")){
                    break;
                } else {
                    System.out.println(incomingText);
                }
            }

            // interrupts thread to stop taking input
            clientText.interrupt();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // recieves input from client
    public static void clientMessages(){
        // creates a new thread for client to send messages
        clientText = new Thread(() -> {
            try{
                // loops for client input until thread is interrupted
                while (true){
                    if (Thread.currentThread().isInterrupted()){
                        break;
                    }

                    outgoingText = input.nextLine();
                    outgoingStream.println(outgoingText);
                }
            } catch (Exception e){
            }
        });
        
        clientText.start();
    }
}