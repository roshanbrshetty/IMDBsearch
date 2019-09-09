# IMDBsearch (Seacrh IMDB)

Project: Imdb Search

Email: roshanbrshetty@gmail.com

Language: Java 11

## Objective

Search for the actors among the list of 1000 top movies listed on IMDB. The search is constrained to cast name.

## Overview

The list of all 1000 movies are scanned for from a seed url. The seed url used here is [Seed URL](https://www.imdb.com/search/title/?groups=top_1000&sort=user_rating&view=simple)
The seed url only points to top 50 movies and it has a next button at the bottom which points to the list of next 50 movies. 

WebPageCrawler.java is responsible for extracting the movie names and the corresponding urls for the same. Jsoup is used for extracting the web content. The java file parses through every next button to extract all the 1000 movies.

A concurrent hash map is created for the same. movieURLMap maintains mapping between movie name and the url. The case of text is maintained as is.

CastExtractor.java extracts the casts from the movies.
The url is parsed for each of the movie and then look for [“See full cast”](https://www.imdb.com/title/tt6398184/fullcredits?ref_=tt_cl_sm#cast) in the url. This takes us to a page which has a list of the entire cast from director, producer, actors, makeup, costumes, etc. Sample of it is provided in the link above. The code does not differentiate based on the role of the cast. Thus the data we are dealing with here is much higher.

A map of movieCast is a mapping between cast name and a set of movies the cast was involved with.
The cast name is not taken as is but is preprossed. “Tom Hanks” is converted to keys “tom hanks” , “tom” and “hanks”. A small set of code is commented. “Samuel L. Jackson” is converted to “samuel”, “samuel l.”, “jackson”, “l. Jackson”, “samuel l. Jackson”. For now anything 2 and below  character length are ignored for simplicity. 

SearchImdb.java is where the search logic is performed. Given any key searches like →

“Tom Hanks”. This is converted to lowercase “tom hanks”. First the key itself is looked for as a whole to get a match. If yes then it returns the set corresponding to “tom hanks”.

If search is of the format
“Samuel TOM spielberg” →
Then the keys are taken separately
“samuel”      → set1  (length 20)
“tom”         → set2  (length 15)
“spielberg”   → set3  (length 50)

All the sets are placed in a priority queue based on the size of set.
The smallest set is picked first.

Set2 is picked first and the intersection is checked with set1. The resultant set is checked for intersection with set3.
The reason is to reduce time complexity. The search becomes O(min(len(set1), len(set2), len(set3)) * number of sets.

ImdbSearchServer.java is the main code with main module and brings up the server on port 8080 and stiches the code together.

*[ImdbSearchServer.java](https://github.com/roshanbrshetty/IMDBsearch/blob/master/src/main/java/web/ImdbSearchServer.java)*
is the main java file.

All the java files present under 
```
src/main/java
```
the jar file is under
```
target
```

## Dependency:

The code has a maven dependency.

## Run:

Download the jar file IMDBsearch/target/IMDBsearch-1.0-SNAPSHOT-jar-with-dependencies.jar  [Jar File](https://github.com/roshanbrshetty/IMDBsearch/blob/master/target/IMDBsearch-1.0-SNAPSHOT-jar-with-dependencies.jar)

From command prompt >

```
java -jar ^path^/IMDBsearch/target/IMDBsearch-1.0-SNAPSHOT-jar-with-dependencies.jar
```

This will take approximately 4 minutes to complete the internal data structures.

A server comes up on 8080 port

On another command prompt

```
> nc 127.0.0.1 8080

Welcome to IMDB Search!!
Enter your search here:

<Your text> <Enter>

List of all the movies:

….
….
…..

Enter your search here: ^C for exit
```
