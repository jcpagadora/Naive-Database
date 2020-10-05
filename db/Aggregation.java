package db;

public class Aggregation {

    /* This class handles all aggregation functions for numerical columns types.
       For example, avg(.), sum(.), and count(.) are aggregation functions since they
       take the entire column and output just a single number.
     */

    /* Here are the currently implemented aggregation function types. */
    private static final String[] validAggs = new String[]{"avg", "count", "sum"};

    private String agg_name;

    private Column column;

    Aggregation(String name, Column col) {
        /*
        Constructor for an Aggregation object.
         */
        column = col;
        for (String agg : validAggs) {
            if (name == agg) {
                agg_name = name;
            }
        }
        throw new RuntimeException();
    }



}
