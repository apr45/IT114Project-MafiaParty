import java.net.*;
import java.io.*;
import java.util.Scanner;

public class MafiaParticipant{
    // input and output streams
    static BufferedReader incomingStream = null;
    static PrintWriter outgoingStream = null;
    static String incomingText, outgoingText;

    public static void main(String[] args) {
        // connection variables
        Socket connection;
        String loopbackAddress = "localhost";
        int port = 2005;

        // scanner for user input
        Scanner input = new Scanner(System.in);

        // variable to store role and choice
        String role, choice;

        // get name and connect to server
        String username;
        System.out.print("Enter your name: ");
        while (true){
            username = input.nextLine();
            if (username.equals("")) {
                System.out.print("Name cannot be empty. Please enter a valid name:");
            } else {
                try {
                    connection = new Socket(loopbackAddress, port);
                    outgoingStream = new PrintWriter(connection.getOutputStream(), true);
                    incomingStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    break;
                } catch(Exception e) {
                    System.exit(0);
                }
            }
        }

        // waiting room
        outgoingStream.println(username);
        System.out.println("You are connected. Waiting for other players to join...");
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
    } 


    // day phase
    public static void dayPhase(String role) {
        try{
            outgoingStream.println(role);
            incomingText = incomingStream.readLine();
            System.out.println(incomingText);

            incomingText = incomingStream.readLine();
            System.out.println(incomingText);

            incomingText = incomingStream.readLine();
            System.out.println(incomingText);

            incomingText = incomingStream.readLine();
            System.out.println(incomingText);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}