package web;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import src.ConstructImdbSearchStore;
import src.SearchImdb;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;

/**
 * Mimic bringing a server on a specified port.
 * Read the input for search.
 */
public final class ImdbSearchServer {

    private static final Logger logger = Logger.getLogger(ImdbSearchServer.class);
    private static final int PORT = 8080;

    /**
     * Bring up the server and wait for the search input.
     * @param port
     */
    public static void bringUpServer(int port) {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket connection = serverSocket.accept();

            OutputStream outputStream = connection.getOutputStream();
            InputStream inputStream = connection.getInputStream();

            Scanner scanner = new Scanner(inputStream, "UTF-8");
            PrintWriter serverOutput =
                    new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            serverOutput.println("Welcome to IMDB Search!!");

            serverOutput.println("Enter your search here: ");
            while (scanner.hasNextLine()) {

                String searchString = scanner.nextLine();
                searchString = searchString.toLowerCase().strip();
                long start = Instant.now().getNano();
                List<String> result = SearchImdb.searchMovies(searchString);
                long end = Instant.now().getNano();

                serverOutput.println("List of all the movies:");

                for(String movie: result){
                    serverOutput.println(movie);
                }

                serverOutput.println(String.format("Your search results were returned in %1.10f seconds",
                        (end - start)/1000000000.0));
                serverOutput.println();
                serverOutput.println("Enter your search here: ^C for exit");
            }

        } catch (IOException exception) {
            logger.error("Could not bring up the server.", exception);
            exception.printStackTrace();
        }
    }

    /**
     * The code flow starts here.
     * Construct the map and the server.
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        BasicConfigurator.configure();
        logger.debug("Construct the backend data store.");
        long start = Instant.now().toEpochMilli();
        ConstructImdbSearchStore.constructDataStore();
        long end = Instant.now().toEpochMilli();
        logger.debug(String.format("Constructed the backend store in %d seconds", (end - start)/1000));
        logger.debug("Bring up the server on port "+PORT);
        bringUpServer(PORT);
    }
}
