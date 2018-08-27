/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yummywakame.bookinventory2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Books app.
 */
public final class BookContract {

    /**
     * The "Content authority" is a name for the entire content provider.
     * In BookContract.java, we set this up as a string constant whose value is the same as that
     * from the AndroidManifest:
     */
    public static final String CONTENT_AUTHORITY = "com.yummywakame.bookinventory2";
    /**
     * BASE_CONTENT_URI which will be shared by every URI associated with BookContract:
     * To make this a usable URI, we use the parse method which takes in a URI string
     * and returns a Uri.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * PATH_TableName stores the path for each of the tables which will be appended to the
     * base content URI.
     */
    public static final String PATH_BOOKS = "books";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private BookContract() {
    }

    /**
     * Inner class that defines constant values for the books database table.
     * Each entry in the table represents a single book.
     */
    public static final class BookEntry implements BaseColumns {

        /**
         * Complete CONTENT_URI
         * The Uri.withAppendedPath() method appends the BASE_CONTENT_URI
         * (which contains the scheme and the content authority) to the path segment.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of books.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single book.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /**
         * Name of database table for books
         */
        public final static String TABLE_NAME = "books";

        /**
         * Unique ID number for the book (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Title of the book.
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_TITLE = "book_title";

        /**
         * Author of the book.
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_AUTHOR = "book_author";

        /**
         * Supplier of the book.
         * <p>
         * The only possible values are {@link #SUPPLIER_SELECT}, {@link #SUPPLIER_1},
         * {@link #SUPPLIER_2}, {@link #SUPPLIER_3} or {@link #SUPPLIER_4}.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_SUPPLIER_ID = "supplier";

        /**
         * Weight of the book.
         * Type: INTEGER
         */
        public final static String COLUMN_BOOK_WEIGHT = "weight";

        /**
         * Possible values for the supplier of the book.
         */
        public static final int SUPPLIER_SELECT = 0;
        public static final int SUPPLIER_1 = 1;
        public static final int SUPPLIER_2 = 2;
        public static final int SUPPLIER_3 = 3;
        public static final int SUPPLIER_4 = 4;

        /**
         * Returns whether or not the given supplier is {@link #SUPPLIER_SELECT},  {@link #SUPPLIER_1},
         * {@link #SUPPLIER_2}, {@link #SUPPLIER_3} or {@link #SUPPLIER_4}.
         */
        public static boolean isValidSupplier(int supplier) {
            return supplier == SUPPLIER_SELECT || supplier == SUPPLIER_1 || supplier == SUPPLIER_2 || supplier == SUPPLIER_3 || supplier == SUPPLIER_4;
        }
    }
}