package db;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by Joseph on 2/27/2017.
 */

// Column class; has attributes column name and a list that holds the
// items in the columns, ie. the rows.
class Column {

    String columnName;

    String columnType;

    //A list of each item under the column; implemented with array list for efficiency
    //in select operations, since array lists have constant run-time get method
    private List items;

    Column(String columnName, String type) {
        if (columnName == null) {
            System.out.println("Must provide an alias in column expression");
            throw new RuntimeException();
        }
        this.columnName = columnName;
        switch (type) {
            case "int":
                items = new ArrayList<Integer>();
                break;
            case "float":
                items = new ArrayList<Float>();
                break;
            case "string":
                items = new ArrayList<String>();
                break;
            default:
                //A run-time exception for invalid type
                throw new RuntimeException();
        }
        columnType = type.toLowerCase();
    }

    /* Makes a copy of the column */
    Column copy() {
        Column copiedCol = new Column(columnName, columnType);
        copiedCol.items = new ArrayList(items);
        return copiedCol;
    }

    /* Formats the objects in the column to print out correctly */
    String format(Object item) {
        //Checks if the item is a special value
        if (item instanceof SpecialValue) {
            return item.toString();
        } else if (columnType.equals("string")) {
            return "'" + item.toString() + "'";
        }
        //Formats float values correctly
        else if (columnType.equals("float")) {
            return String.format("%.3f", item);
        } else if (columnType.equals("int")) {
            int itemInt = Integer.parseInt(item.toString());
            return String.valueOf(itemInt);
        }
        throw new RuntimeException("Error in printing out table");
    }

    /* Formats a given string to the correct type of object */
    Object parseString(String itemString) {
        //Checks to see if the string is a number and if it is formatted correctly
        //as a float or integer
        String testNumFormat = itemString.replaceAll("[*0-9]", "").replace(" ", "");
        //Removes any signs
        testNumFormat = testNumFormat.replace("-", "").replace("+", "");
        //Checks if the item string is empty; if so, it is NOVALUE
        if (itemString.equals("NOVALUE")) {
            return new NoValue();
        } else if (itemString.equals("NaN")) {
            //Checks if the item is of type NaN
            return new NaN();
        } else if (columnType.equals("float")) {
            //Removed all numerals and whitespace; if correctly formatted, one decimal point
            //should indicate a float
            if (testNumFormat.equals(".")) {
                return Float.parseFloat(itemString);
            } else {
                System.out.println("ERROR: Inserted an item of the wrong type");
                throw new NumberFormatException();
            }
        } else if (columnType.equals("int")) {
            //If the integer is formatted correctly, should be empty string
            if (testNumFormat.equals("")) {
                return Integer.parseInt(itemString);
            } else {
                System.out.println("ERROR: Inserted an item of the wrong type");
                throw new NumberFormatException();
            }
        } else {
            //Column type is string, checks if it is not a number
            if (!testNumFormat.equals(".") && !testNumFormat.equals("")) {
                itemString = itemString.replace("'", "").replace("\"", "");
                return itemString;
            } else {
                System.out.println("ERROR: Inserted an item of the wrong type");
                throw new RuntimeException();
            }
        }
    }

    /**
     * Creates a new column when given a column expression with
     * two columns and an alias; evaluates new column type
     */
    Column newColumnBinary(Column otherCol, String alias) {
        String newColType = columnType;
        String otherColType = otherCol.columnType;
        //The only case where unequal types are allowed is with ints and floats
        if (!columnType.equals(otherColType)) {
            if ((columnType.equals("int") && otherColType.equals("float")) ||
                    columnType.equals("float") && otherColType.equals("int")) {
                newColType = "float";
            }
        }
        //Creates a new column to be added to selectedCols
        return new Column(alias, newColType);
    }

    /* Adds an item to the column's items */
    void add(Object o) {
        items.add(o);
    }

    /* Removes the item at index i in the column */
    Object remove(int index) {
        return items.remove(index);
    }

    /* Gets the item at index i in the column */
    Object get(int index) {
        return items.get(index);
    }

    /* Gets the size of the column */
    int size() {
        return items.size();
    }
}
