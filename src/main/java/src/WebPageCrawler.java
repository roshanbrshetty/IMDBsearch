package src;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Crawl the web page and extract the links to all the movies in the seeded page.
 */
public final class WebPageCrawler {

    private static final int THREADS = 3;
    private static final String SEED_URL =
            "https://www.imdb.com/search/title/?groups=top_1000&sort=user_rating&view=simple";
    private static final String HOME_URL = "https://www.imdb.com";
    private static final Logger logger = Logger.getLogger(WebPageCrawler.class);
    private static Map movieURLMap = new ConcurrentHashMap<String, String>();

    /**
     * Extract the 1000 movies from the SEED_URL and their corresponding URLs.
     * @throws IOException
     */
    private static void getListOfMovies() throws IOException {

        String currentURL = SEED_URL;
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        Connection connection;
        Document htmlDocument;
        while(true){
            connection = Jsoup.connect(currentURL);
            htmlDocument = connection.get();
            PopulateUrls populateUrls = new PopulateUrls(htmlDocument);
            executorService.execute(populateUrls);

            // Go to the next page and parse the next page. Keep looking until there exists a next button.
            // Last page would not contain the next button.
            Elements elements = htmlDocument.getElementsByClass("lister-page-next next-page");
            if(elements.size()>0) {
                currentURL = HOME_URL.concat(elements.get(0).getElementsByTag("a").attr("href"));
            } else{
                break;
            }
        }

        executorService.shutdown();
        while(!executorService.isTerminated()){}
        logger.debug("Extracted the 1000 movies successfully");
    }

    /**
     * Extract 50 movies from each document.
     */
    private static class PopulateUrls implements Runnable {
        private final Document htmlDocument;

        PopulateUrls(Document htmlDocument){
            this.htmlDocument = htmlDocument;
        }

        public void run() {
            Elements elements = htmlDocument.getElementsByClass("col-title");
            for(Element element: elements){
                Elements eachMovieElement = element.getElementsByTag("a");
                String movieName = eachMovieElement.text().stripTrailing();
                String movieURL = HOME_URL.concat(eachMovieElement.attr("href").strip());
                //logger.debug(String.format("The movie name %s and the url %s", movieName, movieURL));
                movieURLMap.put(movieName, movieURL);
            }
        }
    }

    public static Map<String, String> fetchMovieUrls() throws IOException{
        getListOfMovies();
        return movieURLMap;
    }

    private WebPageCrawler() {}
}
