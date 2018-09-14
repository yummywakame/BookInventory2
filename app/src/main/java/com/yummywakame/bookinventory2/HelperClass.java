package com.yummywakame.bookinventory2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;


/**
 * BookInventory2
 * Created by Olivia Meiring on 2018/09/06.
 * Yummy Wakame
 * olivia@yummy-wakame.com
 */
public class HelperClass {

    /**
     * Helper method that formats the price
     *
     * @param price is the original double price
     * @return price    formatted with chosen currency in correct position
     * Displays eg: $35.99 instead of $39.998
     */
    public static String formatPrice(Context context, double price, boolean showOnlyCurrencySymbol, boolean showOnlyPrice) {
        // Get only the currency symbol to display in the EditText field
        String formattedPrice = "";

        Locale locale = getCurrencyPreference(context);

        // Displays only the currency symbol in the Editor's Price field background
        if (showOnlyCurrencySymbol && !showOnlyPrice) {
            Currency currency = Currency.getInstance(locale);
            formattedPrice = currency.getSymbol(locale);

            return formattedPrice;
            // Displays XX.XX format in the Editor's Price field
        } else if (showOnlyPrice && !showOnlyCurrencySymbol) {
            NumberFormat formatter = NumberFormat.getInstance();
            // Always display .X as .XX prices
            formatter.setMinimumFractionDigits(2);
            // Shorten .XXXX to .XX
            formatter.setMaximumFractionDigits(2);
            formattedPrice += formatter.format(price);

            return formattedPrice;

            // Displays the full-length price with currency symbol
        } else {
            // Get the correct currency symbol and position depending on chosen locale
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
            // Always display .X as .XX prices
            formatter.setMinimumFractionDigits(2);
            // Shorten .XXXX to .XX
            formatter.setMaximumFractionDigits(2);
            formattedPrice += formatter.format(price);

            return formattedPrice;
        }
    }

    /**
     * A helper method that gets the user's Sort By column and direction (ASC or DESC) preference
     * from Preferences
     *
     * @return String array sort by COLUMN and ascending or descending order
     * Example: new String[] { "BookEntry.COLUMN_BOOK_AUTHOR", "DESC" };
     *
     * To extract use String[] sortBy = HelperClass.getSortByPreference(context);
     * Then: sortBy[0] to retrieve the column and sortBy[2] to retrieve the sort direction;
     */
    public static String[] getSortByPreference(Context context) {
        String orderByColumn = getPreferenceStringValue(context, R.string.pref_order_by_key, R.string.pref_order_by_default);
        String orderByDirection = getPreferenceStringValue(context, R.string.pref_order_by_direction_key, R.string.pref_order_by_direction_default);
        return new String[]{orderByColumn, orderByDirection};
    }

    /**
     * A helper method that gets the user's Currency preference from Preferences and returns a
     * Usable locale.
     *
     * @return Locale as a Locale
     */
    private static Locale getCurrencyPreference(Context context) {
        String formatStrToLocale = getPreferenceStringValue(context, R.string.pref_currency_key, R.string.pref_currency_default);
        return formatStrToLocale(formatStrToLocale);
    }

    /**
     * A helper method that converts a preference String to a preference Locale
     *
     * @param savedLocale String value
     * @return converted Locale value
     */
    private static Locale formatStrToLocale(String savedLocale) {
        Locale locale;

        // Get Locale preference as String from Preferences and save it as a Locale value
        switch (savedLocale) {
            case "Locale.US":
                locale = Locale.US;
                break;
            case "Locale.ITALY":
                locale = Locale.ITALY;
                break;
            case "Locale.UK":
                locale = Locale.UK;
                break;
            case "Locale.CANADA":
                locale = Locale.CANADA;
                break;
            case "Locale.JAPAN":
                locale = Locale.JAPAN;
                break;
            case "Locale.CHINA":
                locale = Locale.CHINA;
                break;
            default:
                locale = Locale.US;
                break;
        }
        return locale;
    }

    /**
     * A helper method to extract any current preference String value
     *
     * @param key          preference's key
     * @param defaultValue preference's default value
     * @return preference  current value
     */
    private static String getPreferenceStringValue(Context context, int key, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(
                context.getString(key),
                context.getString(defaultValue)
        );
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    public static void showDeleteConfirmationDialog(final Context context, final Uri currentBookUri) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook(context, currentBookUri);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Call the supplier when the supplier phone buttons are clicked
     */
    public static void callSupplier(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        context.startActivity(intent);
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private static void deleteBook(Context context, Uri currentBookUri) {
        // Only perform the delete if this is an existing book.
        if (currentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = context.getContentResolver().delete(currentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(context, context.getString(R.string.toast_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(context, context.getString(R.string.toast_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        ((Activity) context).finish();
    }
}