## Grow With Google Android Basics Nanodegree: CAPSTONE PROJECT
# Inventory App Part 1 & Part 2 - Book Inventory

Used **API 27: Android 8.1 (Oreo)**

## Screenshots
![Book Inventory App Screenshots](./screenshots_tiny.png)

## Walkthrough
_[Watch a short demo on YouTube](https://youtu.be/DxaIj-FBA9s)_

## Project Rubric
Create an inventory app using an SQLite database that contains activities for the user to:

* Add Inventory
* See Product Details
* Edit Product Details 
* See a list of all inventory from a Main Activity. 

**In the Main Activity/Fragment**, each list item displays the Product Name, Price, and Quantity. Each list item also contains a SaleButton that reduces the total quantity of that particular product by one (including logic so that no negative quantities are displayed).

**The Product Detail Layout** displays the Product Name, Price, Quantity, Supplier Name, and Supplier Phone Number that's stored in the database. The layout also contains buttons that increase and decrease the available quantity displayed with a check to ensure that no negative quantities display. The Product Detail Layout contains a button to delete the product record entirely and a button to order from the supplier that connects you to the phone app via an intent.

## Resources

1. **[Udacity Pets App](https://github.com/udacity/ud845-Pets)**
Based loosely off the Pets App we created in class.

2. **[Android swipe to delete listview](http://codesfor.in/android-swipe-to-delete-listview/)**
Adapted with the help of [TheBaileyBrew](https://github.com/TheBaileyBrew)
