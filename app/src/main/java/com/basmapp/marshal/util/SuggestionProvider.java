package com.basmapp.marshal.util;

import android.app.SearchManager;
import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.provider.SearchRecentSuggestions;

import com.basmapp.marshal.R;

/**
 * Suggestions provider for recently searched for courses queries
 */
public class SuggestionProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "com.basmapp.marshal.SuggestionProvider";
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

    // Workaround to change autocomplete icon
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        class Wrapper extends CursorWrapper {
            private Wrapper(Cursor c) {
                super(c);
            }

            public String getString(int columnIndex) {
                if (columnIndex != -1
                        && columnIndex == getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1))
                    return String.valueOf(R.drawable.ic_restore_white_24dp);
                return super.getString(columnIndex);
            }
        }
        return new Wrapper(super.query(uri, projection, selection, selectionArgs, sortOrder));
    }
}
