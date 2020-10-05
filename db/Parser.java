package db;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.LinkedList;

/**
 * Created by Joseph on 3/1/2017.
 */
public class Parser {
    // Various common constructs, simplifies parsing.
    private static final String REST  = "\\s*(.*)\\s*",
            COMMA = "\\s*,\\s*",
            AND   = "\\s+and\\s+";

    // Stage 1 syntax, contains the command name.
    private static final Pattern CREATE_CMD = Pattern.compile("create table " + REST),
            LOAD_CMD   = Pattern.compile("load " + REST),
            STORE_CMD  = Pattern.compile("store " + REST),
            DROP_CMD   = Pattern.compile("drop table " + REST),
            INSERT_CMD = Pattern.compile("insert into " + REST),
            PRINT_CMD  = Pattern.compile("print " + REST),
            SELECT_CMD = Pattern.compile("select " + REST),
            TABLES_CMD = Pattern.compile("tables");

    // Stage 2 syntax, contains the clauses of commands.
    private static final Pattern CREATE_NEW  = Pattern.compile("(\\S+)\\s+\\((\\S+\\s+\\S+\\s*" +
            "(?:,\\s*\\S+\\s+\\S+\\s*)*)\\)"),
            SELECT_CLS  = Pattern.compile("([^,]+?(?:,[^,]+?)*)\\s+from\\s+" +
                    "(\\S+\\s*(?:,\\s*\\S+\\s*)*)(?:\\s+where\\s+" +
                    "([\\w\\s+\\-*/'<>=!]+?(?:\\s+and\\s+" +
                    "[\\w\\s+\\-*/'<>=!]+?)*))?"),
            CREATE_SEL  = Pattern.compile("(\\S+)\\s+as select\\s+" +
                    SELECT_CLS.pattern()),
            INSERT_CLS  = Pattern.compile("(\\S+)\\s+values\\s+(.+?" +
                    "\\s*(?:,\\s*.+?\\s*)*)");

    public static void main(String[] args) {
        /* if (args.length != 1) {
            System.err.println("Expected a single query argument");
            return;
        } */
        String s = "";
        for (int i = 0; i < args.length; i++)
            s += args[i] + " ";
        eval(s);
    }

    static List<String> eval(String query) {
        Matcher m;
        if ((m = CREATE_CMD.matcher(query)).matches()) {
            return createTable(m.group(1));
        } else if ((m = LOAD_CMD.matcher(query)).matches()) {
            return loadTable(m.group(1));
        } else if ((m = STORE_CMD.matcher(query)).matches()) {
            return storeTable(m.group(1));
        } else if ((m = DROP_CMD.matcher(query)).matches()) {
            return dropTable(m.group(1));
        } else if ((m = INSERT_CMD.matcher(query)).matches()) {
            return insertRow(m.group(1));
        } else if ((m = PRINT_CMD.matcher(query)).matches()) {
            return printTable(m.group(1));
        } else if ((m = SELECT_CMD.matcher(query)).matches()) {
            return select(m.group(1));
        } else if (TABLES_CMD.matcher(query).matches()) {
            List<String> tables = new LinkedList<>();
            tables.add("tables");
            tables.add("dummy");
            return tables;
        }
        else {
            System.err.printf("Malformed query: %s\n", query);
        }
        return null;
    }

    /* Checks the method of creating a table. Returns a list of strings where 0-th item is
     * the command and 1st is the table name
     */
    private static List<String> createTable(String expr) {
        Matcher m;
        //A list of the arguments to create the table
        List<String> stringArgs = new LinkedList<>();
        if ((m = CREATE_NEW.matcher(expr)).matches()) {
            stringArgs.add("create new table");
            //Group 1 is the name of the new table
            stringArgs.add(m.group(1));
            //Group 2 is an array of strings of the columns
            String[] cols = m.group(2).split(COMMA);
            for (String col : cols) {
                if (col.matches("^.*[^a-zA-Z0-9 ].*$")) {
                    System.err.printf("Invalid column name: %s\n", col);
                    throw new RuntimeException();
                }
                stringArgs.add(col);
            }
            return stringArgs;
        } else if ((m = CREATE_SEL.matcher(expr)).matches()) {
            //TODO: Implement splitting of strings into individual data
            stringArgs.add("create selected table");
            //Group 1 is the name of the table
            stringArgs.add(m.group(1));
            //Group 2 is the string of column expressions
            stringArgs.add(m.group(2));
            //Group 3 is the string of table name/s to get from
            stringArgs.add(m.group(3));
            //Group 4 is the string of the conditional expressions
            stringArgs.add(m.group(4));
            return stringArgs;
        } else {
            System.err.printf("Malformed create: %s\n", expr);
            throw new RuntimeException();
        }
    }

    private static List<String> loadTable(String name) {
        List<String> stringArgs = new ArrayList<>();
        stringArgs.add("load");
        stringArgs.add(name);
        return stringArgs;
    }

    private static List<String> storeTable(String name) {
        List<String> stringArgs = new LinkedList<>();
        stringArgs.add("store");
        stringArgs.add(name);
        return stringArgs;
    }

    private static List<String> dropTable(String name) {
        List<String> stringArgs = new LinkedList<>();
        stringArgs.add("drop");
        stringArgs.add(name);
        return stringArgs;
    }

    /* Returns the list of values in the row to be added, as strings */
    private static List<String> insertRow(String expr) {
        Matcher m = INSERT_CLS.matcher(expr);
        //A list of the arguments to insert into table
        List<String> stringArgs = new LinkedList<>();
        stringArgs.add("insert");

        if (!m.matches()) {
            System.err.printf("Malformed insert: %s\n", expr);
            return null;
        }
        //Adds the name of the table
        stringArgs.add(m.group(1));

        //Adds a string of the values to be inserted
        String row = m.group(2).replaceAll(",", " , ");

        String[] rowValues = row.split(",");
        for (String val : rowValues) {
            stringArgs.add(val);
        }
        return stringArgs;
    }

    /* Returns a list of strings where 0-th element is the command and 1st element is the table name */
    private static List<String> printTable(String name) {
        //System.out.printf("You are trying to print the table named %s\n", name);
        List<String> stringArgs = new LinkedList<>();
        stringArgs.add("print");
        stringArgs.add(name);
        return stringArgs;
    }


    private static List<String> select(String expr) {
        Matcher m = SELECT_CLS.matcher(expr);
        if (!m.matches()) {
            System.err.printf("Malformed select: %s\n", expr);
            return null;
        }
        List<String> stringArgs = new LinkedList<>();
        stringArgs.add("select");
        stringArgs.add("None");

        //Group 1 is the string of column expressions
        stringArgs.add(m.group(1));

        //Group 2 is the string of table name/s to get from
        stringArgs.add(m.group(2));

        //Group 3 is the string of conditional expressions
        stringArgs.add(m.group(3));

        return stringArgs;
    }

}
