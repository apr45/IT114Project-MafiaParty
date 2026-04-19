import java.net.*;
import java.io.*;
import java.util.Scanner;

public class MafiaParticipant{
    // input and output streams
    static private BufferedReader incomingStream = null;
    static private PrintWriter outgoingStream = null;
    static private String incomingText;

    public static void main(String[] args) throws IOException{
        // connection variables
        Socket connection = null;
        final String LOOPBACKADDRESS = "localhost";
        final int PORT = 2005;

        // scanner for user input
        Scanner input = new Scanner(System.in);

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
                    } catch(Exception e) {
                        System.exit(0);
                    }
                    break;
                }
            }

        outgoingStream.println(username); // sends initial username input to server

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

        // waiting room
        incomingText = incomingStream.readLine();
        System.out.println("\n" + incomingText);

        incomingText = incomingStream.readLine();
        System.out.println(incomingText);

        // game starts
        incomingText = incomingStream.readLine();
        System.out.println("\n" + incomingText);
        
        incomingText = incomingStream.readLine();
        System.out.println("Your role is: " + incomingText);

        //dayPhase(incomingText);

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
/*      
        // variable to store role and choice
        String role, choice;

        while (true) {
            try {
                incomingText = incomingStream.readLine();
                role = incomingText;

                // checks if server initates game
                incomingText = incomingStream.readLine();
                if (incomingText.equals("NIGHTPHASE")) {
                    System.out.println("The game is starting...");
                    System.out.println("Your role is: " + role);
                    break;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        // day and night phases; code will later be surrounded by a while loop
        try{
            nightPhase(role);
            dayPhase(role);

            // checks for server to verify a win condition met
            incomingText = incomingStream.readLine();
            if (incomingText.equals("EXIT")){
                // code will be updated to break off loop
                incomingText = incomingStream.readLine();
                System.out.println(incomingText);
                incomingText = incomingStream.readLine();
                System.out.println(incomingText);
            }
        } catch (Exception e){
        } finally {
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
    }

    // night phase
    public static void nightPhase(String role) {
        try {
            outgoingStream.println(role);
            incomingText = incomingStream.readLine();
            System.out.println(incomingText);

            incomingText = incomingStream.readLine();
            System.out.println(incomingText);
            // if player is Mafia, get victim choice
            if (role.equals("Mafia")) {
                // code will be updated to input victim choice and send to server
                System.out.println("Victim choosen.");
            }

            incomingText = incomingStream.readLine();
            System.out.println(incomingText);
        } catch(Exception e) {
            e.printStackTrace();
        }
    } */


    // day phase
    public static void dayPhase(String role) {
        try{
            incomingText = incomingStream.readLine();
            System.out.println(incomingText);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}