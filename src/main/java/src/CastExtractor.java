package src;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Extract the cast information and create sets for the same.
 */
public final class CastExtractor {

    private static final int THREADS = 6;
    private static Map<String, Set<String>> movieCast;
    private static final Logger logger = Logger.getLogger(CastExtractor.class);

    public static Map<String, Set<String>> extractCastFromMovies(Map<String, String> map) throws IOException {
        movieCast = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        for(Map.Entry<String, String> eachMovie: map.entrySet()){
            ExtractCast extractCast = new ExtractCast(eachMovie.getValue(), eachMovie.getKey());
            executorService.execute(extractCast);
        }
        executorService.shutdown();
        while(!executorService.isTerminated()){}

        return movieCast;
    }

    private static class ExtractCast implements Runnable {
        private final String url;
        private final String movie;

        ExtractCast(String url, String movie){
            this.url = url;
            this.movie = movie;
        }

        /**
         * Pith of the logic to construct the backend search data store.
         */
        public void run() {
            try {
                logger.debug("Processing movie "+movie);
                Connection connection = Jsoup.connect(url);
                Document htmlDocument = connection.get();
                Elements seeMoreElements = htmlDocument.select("div#titleCast");
                if(seeMoreElements.size()<0) {
                    logger.warn(String.format("No cast link seen for movie %s", movie));
                    return;
                }
                String movieCastURL = seeMoreElements.get(0)
                        .getElementsByClass("see-more")
                        .get(0).getElementsByTag("a")
                        .attr("abs:href").strip();
                connection = Jsoup.connect(movieCastURL);
                htmlDocument = connection.get();
                Elements castNames = htmlDocument.getElementsByClass("name");
                for (Element element : castNames) {
                    String castName = element.getElementsByTag("a").text().strip();
                    List<String> possibleKeys = generatePossibleSearchKeys(castName);
                    for(String eachKey: possibleKeys){
                        if(!movieCast.containsKey(eachKey)){
                            movieCast.put(eachKey, new HashSet<>());
                        }
                        movieCast.get(eachKey).add(movie);
                    }
                }
                castNames = htmlDocument.getElementsByClass("odd");
                for (Element element : castNames) {
                    Elements subElements = element.select("td:eq(1)");
                    if(subElements.size()==0){
                        continue;
                    }
                    String castName = subElements.get(0).getElementsByTag("a").text().strip();
                    List<String> possibleKeys = generatePossibleSearchKeys(castName);
                    for(String eachKey: possibleKeys){
                        if(!movieCast.containsKey(eachKey)){
                            movieCast.put(eachKey, new HashSet<>());
                        }
                        movieCast.get(eachKey).add(movie);
                    }
                }
                castNames = htmlDocument.getElementsByClass("even");
                for (Element element : castNames) {
                    Elements subElements = element.select("td:eq(1)");
                    if(subElements.size()==0){
                        continue;
                    }
                    String castName = subElements.get(0).getElementsByTag("a").text().strip();
                    List<String> possibleKeys = generatePossibleSearchKeys(castName);
                    for(String eachKey: possibleKeys){
                        if(!movieCast.containsKey(eachKey)){
                            movieCast.put(eachKey, new HashSet<>());
                        }
                        movieCast.get(eachKey).add(movie);
                    }
                }
                logger.debug("Processing movie completed for "+movie);
            }
            catch(IOException ioException){
                logger.error("An exception was seen while contructing the data store", ioException);
                ioException.printStackTrace();
            }
        }

        /**
         * Generates the possible combinations for the Cast name.
         * @param castName
         * @return List of the possible combinations of names.
         */
        private List<String> generatePossibleSearchKeys(String castName){
            List<String> listOfCasts = new ArrayList<>();
            listOfCasts.add(castName.toLowerCase());
            String[] listOfKeys = castName.split(" ");
            for(int j=0;j<listOfKeys.length;j++){
                if(listOfKeys[j].length()>2){
                    listOfCasts.add(listOfKeys[j].toLowerCase());
                }
                /*
                else{
                    if(listOfKeys[j].contains(".")){
                        if(j>0){
                            listOfCasts.add(listOfKeys[j-1]+" "+listOfKeys[j]);
                        }
                        if(j<listOfKeys.length-1){
                            listOfCasts.add(listOfKeys[j]+" "+listOfKeys[j+1]);
                        }
                    }
                }
                */
            }
            return listOfCasts;
        }
    }

    private CastExtractor() {}
}
