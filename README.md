# Running Mafia Party

To set up and run the Java application, follow these steps:

1. **Download Necessary Files**
   Make sure to download all required Java files from the repository, including:
   * `ServerModerator.java`
   * `ClientHandler.java`
   * `MafiaParticipant.java`
   
   Additionally, you will need a folder named `images` containing:
   * `dead.png`
   * `alive.png`
   
   Save these files in a designated folder on your computer.

2. **Open in VSCode**
   Launch Visual Studio Code (VSCode) and open the folder where you saved the Java files.

3. **Create a New Terminal**
   Within VSCode, create a new terminal instance.

4. **Compile Java Files**
   Use the command `javac <filename>.java` to compile each Java file individually. Ensure each file creates a `.class` file.

5. **Split the Terminal**
   After compiling, split the terminal into four separate instances:
   * **One terminal** to run the server.
   * **Three terminals** for client connections.

6. **Run the Server**
   In the terminal designated for the server, type `java ServerModerator` to start the server. If the terminal displays the message `"Waiting for connection..."`, the server is successfully running.

7. **Connect Clients**
   In the remaining terminals set up for clients, enter the command `java MafiaParticipant` to connect them to the server. The server will provide feedback indicating whether each client successfully connected.

8. **Start the Game**
   The game will commence automatically once three players connect to the server and enter valid usernames.

---
By following these steps, you will correctly set up and initiate the game environment.