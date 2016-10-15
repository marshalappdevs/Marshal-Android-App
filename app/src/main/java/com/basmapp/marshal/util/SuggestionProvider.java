package com.basmapp.marshal.util;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;

/**
 * Suggestions provider for recently searched for courses queries
 */
public class SuggestionProvider extends SearchRecentSuggestionsProvider {

    private final static String AUTHORITY = "com.basmapp.marshal.SuggestionProvider";
    private final static int MODE = DATABASE_MODE_QUERIES;

    /**
     * Save query to history
     *
     * @param context
     * @param query
     */
    public static void save(Context context, String query) {
        suggestions(context).saveRecentQuery(query, null);
    }

    /**
     * Clear query history
     *
     * @param context
     */
    public static void clear(Context context) {
        suggestions(context).clearHistory();
    }

    private static SearchRecentSuggestions suggestions(Context context) {
        return new SearchRecentSuggestions(context, AUTHORITY, MODE);
    }

    /**
     * Create suggestions provider for searched courses queries
     */
    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
