package at.tobiazsh.myworld.traffic_addition.algorithms;

import java.util.ArrayList;
import java.util.Collection;

public class FuzzySearch {

    Collection<String> items;

    public FuzzySearch(Collection<String> items) {
        this.items = items;
    }

    /**
     * Searches for items in the collection that match the given query using a fuzzy search algorithm.
     * The algorithm is able to handle typos and partial matches.
     *
     * @param query The search query.
     * @return A collection of items that match the query.
     */
    public Collection<String> search(String query) {
        Collection<String> matches = new ArrayList<>();

        for (String item : items) {

            // Partial match
            if (items.contains(query)) {
                matches.add(item);
                continue;
            }

            // Fuzzy partial match
            int queryLen = query.length();
            for (int i = 0; i <= item.length() - queryLen; i++) {
                String substring = item.substring(i, i + queryLen);
                int score = LevenshteinDistance.getDistance(query, substring);

                if (score <= 2) { // Allow a maximum of 2 character differences
                    matches.add(item);
                    break;
                }
            }
        }

        return matches;
    }
}
