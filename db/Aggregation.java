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

    private String type;

    /*
      Constructor for an Aggregation object.
     */
    Aggregation(String name, Column col) {
        column = col;
        type = col.columnType;
        for (String agg : validAggs) {
            if (name.equals(agg)) {
                agg_name = name;
                return;
            }
        }
        System.out.printf("Unrecognized aggregation function: %s\n", name);
        throw new RuntimeException();
    }

    /*
    Performs the aggregation function.
     */
    float aggregate() {
        // Many of the aggregation functions use the length of the column.
        int n = column.size();
        switch (agg_name) {
            case "avg":
                float sum = 0;
                if (type.equals("int")) {
                    for (int i = 0; i < n; i++) {
                        sum += (int) column.get(i);
                    }
                } else {
                    for (int i = 0; i < n; i++) {
                        sum += (float) column.get(i);
                    }
                }
                return sum / n;
            case "sum":
                float result = 0;
                if (type.equals("int")) {
                    for (int i = 0; i < n; i++) {
                        result += (int) column.get(i);
                    }
                } else {
                    for (int i = 0; i < n; i++) {
                        result += (float) column.get(i);
                    }
                }
                return result;
            case "count":
                return n;
            default:
                throw new RuntimeException();
        }
    }

}
