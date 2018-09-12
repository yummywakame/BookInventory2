package com.yummywakame.bookinventory2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

        Locale locale = getCurrencyPreference();

        // Displays only the currency symbol in the Editor's Price field background
        if (showOnlyCurrencySymbol) {
            Currency currency = Currency.getInstance(locale);
            formattedPrice = currency.getSymbol(locale);

            return formattedPrice;
            // Displays XX.XX format in the Editor's Price field
        } else if (showOnlyPrice) {
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
     * To extract use String[] sortBy = HelperClass.getSortByPreference();
     * Then: sortBy[0] to retrieve the column and sortBy[2] to retrieve the sort direction;
     */
    public static String[] getSortByPreference() {
        String orderByColumn = getPreferenceStringValue(R.string.pref_order_by_key, R.string.pref_order_by_default);
        String orderByDirection = getPreferenceStringValue(R.string.pref_order_by_direction_key, R.string.pref_order_by_direction_default);
        return new String[]{orderByColumn, orderByDirection};
    }

    /**
     * A helper method that gets the user's Currency preference from Preferences and returns a
     * Usable locale.
     *
     * @return Locale as a Locale
     */
    private static Locale getCurrencyPreference() {
        String formatStrToLocale = getPreferenceStringValue(R.string.pref_currency_key, R.string.pref_currency_default);
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
    private static String getPreferenceStringValue(int key, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        return sharedPreferences.getString(
                MyApplication.getAppContext().getString(key),
                MyApplication.getAppContext().getString(defaultValue)
        );
    }
}