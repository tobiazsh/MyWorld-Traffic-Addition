package at.tobiazsh.myworld.traffic_addition.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class FuzzySearch<T> {

    private final Collection<T> items;
    private final Function<T, String> textExtractor;
    private final int maxDistance;

    /**
     * Creates a new FuzzySearch instance.
     *
     * @param items The collection of items to search through.
     * @param textExtractor A function that extracts the text from an item.
     * @param maxDistance The maximum Levenshtein distance to consider a match.
     */
    public FuzzySearch(Collection<T> items, Function<T, String> textExtractor, int maxDistance) {
        this.items = items;
        this.textExtractor = textExtractor;
        this.maxDistance = maxDistance;
    }

    /**
     * Searches for items in the collection that match the given query using a fuzzy search algorithm.
     * The algorithm is able to handle typos and partial matches.
     *
     * @param query The search query.
     * @return A collection of items that match the query.
     */
    public Collection<T> search(String query) {
        Collection<T> matches = new ArrayList<>();

        if (query == null || query.isEmpty())
            return matches; // If query is empty, do nothing


        for (T item : items) {
            String text = textExtractor.apply(item);

            if (text == null) continue; // Skip null texts

            // Partial or exact match (case-insensitive)
            if (text.equalsIgnoreCase(query) || text.toLowerCase().contains(query.toLowerCase())) {
                matches.add(item);
                continue;
            }

            // Fuzzy exact match
            if (LevenshteinDistance.getDistance(query.toLowerCase(), text.toLowerCase()) <= maxDistance) {
                matches.add(item);
                continue;
            }

            // Fuzzy partial match
            int queryLen = query.length();
            for (int i = 0; i <= text.length() - queryLen; i++) {
                String substring = text.substring(i, i + queryLen);
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
