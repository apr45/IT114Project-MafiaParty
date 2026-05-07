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

    // main GUI components
    private static JFrame gameWindow = new JFrame("Mafia Party");
    private static JPanel playersPanel = new JPanel();
    private static ImageIcon aliveIcon;
    private static ImageIcon deadIcon;

    // chatroom GUI components
    private static JButton sendInput;
    private static JTextField textInput;
    private static JTextArea textArea;

    // tracks players information
    private static String[] usernameList;
    private static String[] statusList;

    // timer
    private static javax.swing.Timer timer;

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
            usernameList = arrayFormat(incomingText);

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
                playersPanel.setLayout(new GridLayout(usernameList.length, 1, 5, 0));

                // resize icon images
                aliveIcon = new ImageIcon("alive.png");
                Image image = aliveIcon.getImage();
                Image resizedImage = image.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
                aliveIcon = new ImageIcon(resizedImage);

                    // later use
                    deadIcon = new ImageIcon("dead.png");
                    image = deadIcon.getImage();
                    resizedImage = image.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
                    deadIcon = new ImageIcon(resizedImage);

                // inserts label of each player into the player list panel
                for (int i = 0; i < usernameList.length; i++){
                    playersPanel.add(new JLabel(usernameList[i], aliveIcon, JLabel.LEFT));
                }
            
                // adds player list panel to game window
                gameWindow.add(playersPanel, BorderLayout.CENTER);
            
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

        // endgame state
        System.out.println("\nGame over!");
        incomingText = incomingStream.readLine();
        System.out.println(incomingText);
        gameWindow.dispose();

        // closes socket connection
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

    // converts a string into an array
    private static String[] arrayFormat(String string){
        String stringCleanup = string.replace("[", "").replace("]", "");
        String[] stringArray = stringCleanup.split(", ");
        return stringArray;
    }

    private static void updatingMainWindow(){
        // updates panel to showcase players that alive or dead
        playersPanel.removeAll();
        for (int i = 0; i < usernameList.length; i++){
            if (statusList[i].equals("Dead")){
                playersPanel.add(new JLabel(usernameList[i], deadIcon, JLabel.LEFT));
            } else if (statusList[i].equals("Alive")){
                playersPanel.add(new JLabel(usernameList[i], aliveIcon, JLabel.LEFT));
                }
            }
                
        // refreshes players panel to showcase update
        playersPanel.revalidate();
        playersPanel.repaint();
    }

    // night state for the client side
    private static void clientNightState() throws IOException {
        // signals client the beginning of night time
        System.out.println("\nNight has fallen. Civilians fall asleep as Mafia chooses their victim.");

        try {
            // block until recieves client role
            incomingText = incomingStream.readLine();
            
            // compares client role to initate proper 
                if (incomingText.equals("Mafia")){
                    // gets array of all Civilians alive
                    incomingText = incomingStream.readLine();
                    usernameList = arrayFormat(incomingText);

                    // GUI pane for Mafia to choose a player to eliminate
                    JOptionPane choosingPlayerPane =new JOptionPane("Choose player to eliminate:", JOptionPane.QUESTION_MESSAGE,  JOptionPane.OK_CANCEL_OPTION);
                    choosingPlayerPane.setSelectionValues(usernameList);
                    choosingPlayerPane.setInitialSelectionValue(usernameList[0]); 
                    JDialog playerElimination = choosingPlayerPane.createDialog(gameWindow, "Player Elimination");

                    // sets a time limit for Mafia during player elimination
                    timer = new javax.swing.Timer(9000, e -> playerElimination.dispose());
                    timer.setRepeats(false);
                    timer.start();

                    // ensures GUI pane is available to Mafia during timer run
                    Object playerChoosen = "";
                    while (timer.isRunning()){
                        playerElimination.setVisible(true);

                        // closes pane and stops timer once Mafia chooses player
                        playerChoosen = choosingPlayerPane.getInputValue();
                        if (playerChoosen != null && playerChoosen != JOptionPane.UNINITIALIZED_VALUE){
                            timer.stop();
                            break;
                        }
                    }
                    
                    // converts choosen player object to string
                    String playerEliminated = (String) playerChoosen;
                    outgoingStream.println(playerEliminated);
                } else if (incomingText.equals("Civilian")){
                    // puts client in a waiting state until timer reaches 0
                    System.out.println("Waiting for Mafia to choose victim...");
                }
            
            // GUI window update
            incomingText = incomingStream.readLine();
            usernameList = arrayFormat(incomingText);
            incomingText = incomingStream.readLine();
            statusList = arrayFormat(incomingText);
            updatingMainWindow();

            // waits until recieves signal that night time has ended
            incomingText = incomingStream.readLine();
            System.out.println(incomingText);
        } catch(Exception e) {
            e.printStackTrace();
        }
    } 

    // day state for the client side
    private static void clientDayState() throws IOException{
        // signals players that daytime has come and preps for dissussion time
        System.out.println("\nDay has dawned. Discuss who is the Mafia.");

        //chat room GUI
            // sets up seperate window for discussion
            JFrame chatroom = new JFrame();
            chatroom.setSize(500,500);
            chatroom.setLocationRelativeTo(null);
            chatroom.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            JLabel chatHeader = new JLabel("Chat Room");
            chatroom.add(chatHeader, BorderLayout.NORTH);

            // sets up area to showcase client text
            textArea = new JTextArea(30,30);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(textArea);
            chatroom.add(scrollPane, BorderLayout.CENTER);

            // sets up field to text
            JPanel inputArea = new JPanel();
            JLabel inputLine = new JLabel("Input:");
            textInput = new JTextField("", 10);
            sendInput = new JButton("Sent");
            inputArea.add(inputLine);
            inputArea.add(textInput);
            inputArea.add(sendInput);
            chatroom.add(inputArea, BorderLayout.SOUTH);

            // sets visibility of chatroom window
            chatroom.setVisible(true);

        // sets timer and initates global chat room
        timer = new javax.swing.Timer(10000, e ->{ 
            chatroom.dispose();
            outgoingStream.println("END");
        });
        timer.setRepeats(false);
        timer.start();
        clientMessages();
        groupMessages();

        // signals client that discussion time is over
        incomingText = incomingStream.readLine();
        System.out.println(incomingText);

        // gets list of players alive
        incomingText = incomingStream.readLine();
        usernameList = arrayFormat(incomingText);

         // GUI pane for voting
        JOptionPane votingPane =new JOptionPane("Vote on a player:", JOptionPane.QUESTION_MESSAGE,  JOptionPane.OK_CANCEL_OPTION);
        votingPane.setSelectionValues(usernameList);
        votingPane.setInitialSelectionValue(usernameList[0]); 
        JDialog playerVoting = votingPane.createDialog(gameWindow, "Player Voting");

        // set timer for voting
        timer = new javax.swing.Timer(10000, e -> playerVoting.dispose());
        timer.setRepeats(false);
        timer.start();

        // ensures GUI pane is available to client during timer run
        Object playerChoosen = "";
        while (timer.isRunning()){
            playerVoting.setVisible(true);

            // closes pane and stops timer once client votes
            playerChoosen = votingPane.getInputValue();
            if (playerChoosen != null && playerChoosen != JOptionPane.UNINITIALIZED_VALUE){
                timer.stop();
                break;
            }
        }
                    
        // converts voted player object to string
        String votedPlayer = (String) playerChoosen;
        outgoingStream.println(votedPlayer);

        // GUI window update
        incomingText = incomingStream.readLine();
        usernameList = arrayFormat(incomingText);
        incomingText = incomingStream.readLine();
        statusList = arrayFormat(incomingText);
        updatingMainWindow();

        // displays results of voting
        incomingText = incomingStream.readLine();
        System.out.println(incomingText);
        incomingText = incomingStream.readLine();
        System.out.println(incomingText);
        incomingText = incomingStream.readLine();
        System.out.println(incomingText);
    }
    
    // display message from other clients
    private static void groupMessages() {
        try{
            // waits until recieve a client message to display or until timer runs out
            while (true) {
                incomingText = incomingStream.readLine();

                if (incomingText.equals("EXIT")){
                    break;
                } else {
                    textArea.append(incomingText + "\n");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // recieves input from client
    private static void clientMessages(){
        // sents text from text field to server if button clicked
        sendInput.addActionListener(e ->{
            outgoingText = textInput.getText();
            outgoingStream.println(outgoingText);
            textInput.setText("");
        });
    }
}