package com.yummywakame.bookinventory2;

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

    // Hardcode the locale for now
    public static Locale locale = Locale.ITALY;

    /**
     * Helper method that formats the price
     *
     * @param price is the original double price
     * @return price    formatted with chosen currency in correct position
     * Displays eg: $25 instead of $25.00 and $35.99 instead of $39.998
     */
    public static String formatPrice(double price, boolean showOnlyCurrencySymbol, boolean showOnlyPrice) {
        // Get only the currency symbol to display in the EditText field
        String formattedPrice = "";

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
}
