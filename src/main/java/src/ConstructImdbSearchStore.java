package src;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Construct the back
 */
public final class ConstructImdbSearchStore {

    private static final Logger logger = Logger.getLogger(ConstructImdbSearchStore.class);
    private ConstructImdbSearchStore(){}

    private static Map<String, String> movieURLMap;
    public static Map<String, Set<String>> movieCast;

    public static void constructDataStore() {

        // 1. Construct the movie map.
        logger.debug("Fetch the movie urls");
        try {
            movieURLMap = WebPageCrawler.fetchMovieUrls();
        } catch (IOException exception) {
            logger.error("Could not fetch the urls for 1000 movies", exception);
        }

        logger.debug(String.format("Size of the map=%d",movieURLMap.size()));

        // 2. Construct the cast movie mapping.
        logger.debug("Construct the cast movie mapping");
        try {
            movieCast = CastExtractor.extractCastFromMovies(movieURLMap);
        } catch (IOException exception) {
            logger.error("Could not construct the in memory search store", exception);
        }
        logger.debug("Construction complete.");
    }
}
