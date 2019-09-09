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
        //min heap.
        priorityQueue = new PriorityQueue<>((set1, set2) -> set1.size()-set2.size());

        // Add the sets into the Priority Queue and pick the Set with the least size.
        for(String eachSearchKey: searchKeys){
            // If the search key contains only 1 character then return empty result.
            if(eachSearchKey.length()<2){
                return new ArrayList<>();
            }
            // Even if one of the string is not present in the map return empty.
            if(!ConstructImdbSearchStore.movieCast.containsKey(eachSearchKey)){
                return new ArrayList<>();
            }
            priorityQueue.add(ConstructImdbSearchStore.movieCast.get(eachSearchKey));
        }

        // find the intersection of the sets.
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

    /**
     * Check for the entire search string initially.
     * If no match is seen then split the string by space and check for individual keywords.
     */
    public static List<String> searchMovies(String searchString){
        searchString = searchString.strip().toLowerCase();
        if(ConstructImdbSearchStore.movieCast.containsKey(searchString)) {
            logger.debug("Whole Search String matched");
            return new ArrayList<String>(ConstructImdbSearchStore.movieCast.get(searchString));
        }
        // Check for the entire array of string.
        return searchMovies(searchString.split(" "));
    }
}
