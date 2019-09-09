package src;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.util.*;

public final class SearchImdb {

    private static final Logger logger = Logger.getLogger(SearchImdb.class);
    private static PriorityQueue<Set<String>> priorityQueue = new PriorityQueue<>();

    private static List<String> searchMovies(String[] searchKeys){
        if(searchKeys.length==0) {
            return new ArrayList<>();
        }
        priorityQueue = new PriorityQueue<>((set1, set2) -> set1.size()-set2.size());

        for(String eachSearchKey: searchKeys){
            if(eachSearchKey.length()<2){
                return new ArrayList<>();
            }
            if(!ConstructImdbSearchStore.movieCast.containsKey(eachSearchKey)){
                return new ArrayList<>();
            }
            priorityQueue.add(ConstructImdbSearchStore.movieCast.get(eachSearchKey));
        }

        Set<String> resultMovies = priorityQueue.poll();
        while(!priorityQueue.isEmpty()){
            Set<String> nextSet = priorityQueue.poll();
            Set<String> tempSet = new HashSet<>();
            for(String eachMovie: resultMovies){
                if(nextSet.contains(eachMovie)){
                    tempSet.add(eachMovie);
                }
            }
            resultMovies = tempSet;
        }
        return new ArrayList<>(resultMovies);
    }

    public static List<String> searchMovies(String searchString){
        searchString = searchString.strip().toLowerCase();
        if(ConstructImdbSearchStore.movieCast.containsKey(searchString)) {
            logger.debug("Whole Search String matched");
            return new ArrayList<String>(ConstructImdbSearchStore.movieCast.get(searchString));
        }
        return searchMovies(searchString.split(" "));
    }
}
