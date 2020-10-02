package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;

public class Database {

    //The tables of the database kept in a map, where each table's associated key
    //is its name as a string
    private Map<String, Table> tables;

    /**
     * Public constructor for database
     */
    public Database() {
        tables = new HashMap<>();
    }

    public String transact(String query) {
        try {
            //The list returned from evaluating query using Parser class
            List<String> stringArgs = Parser.eval(query);
            //Gets the command
            String command = stringArgs.get(0);
            //Gets table name
            String tableName = stringArgs.get(1);
            //Checks that the table name starts with a letter
            char firstChar = tableName.charAt(0);
            if (!Character.isLetter(firstChar)) {
                return "ERROR: Invalid name; make sure names begin with a letter";
            }

            switch (command) {
                case "create new table":
                    List<String> colInfo = new LinkedList<>();
                    //Gets length of the list string args from query
                    int stringArgsLength = stringArgs.size();
                    //Starts at 2nd index of stringArgs in order to iterate
                    // through the column data
                    for (int i = 2; i < stringArgsLength; i++) {
                        String[] colSplit = stringArgs.get(i).split(" ");
                        //If formatted correctly, first and last items should be
                        // column name and type,
                        //ignore whitespace in the middle
                        colInfo.add(colSplit[0]);
                        colInfo.add(colSplit[colSplit.length - 1]);
                    }
                    return createNewTable(tableName, colInfo);

                case "create selected table":
                    //Uses select table to create this selected table
                    Table selectedCreateTable = selectedTable(stringArgs);
                    //Puts resulting table into database
                    tables.put(tableName, selectedCreateTable);
                    return "";

                case "select":
                    //Puts selected columns into a table
                    Table selectedTable = selectedTable(stringArgs);
                    return selectedTable.print();

                case "insert":
                    List<String> rowVals = new LinkedList<>();
                    //Gets length of the list string args from query
                    int lengthStringArgs = stringArgs.size();
                    for (int i = 2; i < lengthStringArgs; i++) {
                        rowVals.add(stringArgs.get(i).trim());
                    }
                    return insertRow(tableName, rowVals);

                case "store":
                    if (tables.containsKey(tableName)) {
                        return store(tableName);
                    } else {
                        return "ERROR: No table " + tableName + " found in database";
                    }

                case "load":
                    return load(tableName);

                case "print":
                    //Prints the table
                    return printTable(tableName);

                case "drop":
                    return dropTable(tableName);

                default:
                    return "ERROR: Malformed command";

            }
        } catch (RuntimeException e) {
            return "ERROR: There was an error in executing your command.";
        }
    }

    /**
     * Creates a new table; takes in column names and types, creates columns,
     * then puts the table into the tables map. The list columnInfo must be
     * ordered such that even-indexed items are the column names, and odd-indexed
     * items are the corresponding column types.
     */
    private String createNewTable(String tableName, List<String> columnInfo) {
        if (columnInfo.isEmpty()) {
            return "ERROR: table must have at least one column";
        }
        if (tables.containsKey(tableName)) {
            System.out.println("ERROR: Already a table " + tableName + " in database");
            throw new RuntimeException();
        }
        Table newTable = Table.getColumnInfo(columnInfo);
        tables.put(tableName, newTable);
        return "";
    }

    /**
     * Extracts column and table data from a select clause and puts it into a table; performs join
     * and evaluates column expressions
     */
    private Table selectedTable(List<String> stringArgs) {
        //Gets table names in a string
        String tableNameString = stringArgs.get(3).trim();
        //Splits by commas to get individual table names
        String[] tableNames = tableNameString.split(",");
        //Checks if table names are in database
        for (int i = 0; i < tableNames.length; i++) {
            tableNames[i] = tableNames[i].trim();
            String name = tableNames[i];
            if (!tables.containsKey(name)) {
                System.out.println("No table " + name + " found in database");
            }
        }

        String firstTableName = tableNames[0];
        //Uses copies of the tables to join
        Table selectedTable = tables.get(firstTableName).copy();
        for (int i = 1; i < tableNames.length; i++) {
            selectedTable = selectedTable.join(tables.get(tableNames[i]).copy());
        }
        //Gets the column expressions, removes whitespace
        String columnExpressionsString = stringArgs.get(2).trim();
        String[] colExpressionsArr = columnExpressionsString.split(",");

        //If specific columns are selected, applies the column expressions to the table and
        // stores the resulting table; otherwise select all
        if (colExpressionsArr.length > 1 || !colExpressionsArr[0].equals("*")) {
            selectedTable = selectedTable.selectColumns(colExpressionsArr);
        } else {
            selectedTable = selectedTable.copy();
        }

        //Gets the conditional expressions, separates by " and "
        String conditionalExprsString = stringArgs.get(4);
        if (conditionalExprsString == null) {
            return selectedTable;
        }
        conditionalExprsString = conditionalExprsString.trim();
        String[] condExprs = conditionalExprsString.split("and");

        //Adds each conditional to a list
        List<Conditional> conditionals = new ArrayList<>();
        for (String cond : condExprs) {
            conditionals.add(new Conditional(cond));
        }
        //Tests conditions on selected table and gets indices of rows to remove
        selectedTable.removeRowsConditions(conditionals);

        return selectedTable;
    }


    /**
     * Drops the table by removing its key from tables
     */
    private String dropTable(String tableName) {
        if (tables.containsKey(tableName)) {
            tables.remove(tableName);
            return "";
        }
        return "ERROR: No table " + tableName + " in database";
    }

    /**
     * Stores the contents of the table into the file <table name>.tbl
     */
    private String store(String tableName) {
        try {
            PrintWriter writer = new PrintWriter(tableName + ".tbl", "UTF-8");
            String printedTable = printTable(tableName);
            for (String s : printedTable.split("\n")) {
                writer.println(s);
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("ERROR: There was an error in storing this table");
            throw new RuntimeException();
        }
        return "";
    }

    /**
     * Loads the contents of <table name>.tbl file into memory as a table with
     * name <table name>. Takes string of
     */
    private String load(String tableName) {
        try (BufferedReader br = new BufferedReader(new FileReader(tableName + ".tbl"))) {

            //This string contains column information
            String line = br.readLine();
            String[] colInfoString = line.split(",");
            List<String> colInfoList = new ArrayList<>();
            for (String colInfo : colInfoString) {
                String[] colInfoSplit = colInfo.trim().split(" ");
                colInfoList.add(colInfoSplit[0]);
                colInfoList.add(colInfoSplit[colInfoSplit.length - 1]);
            }
            //Creates a new table in database with table name and column info given in colInfoList
            Table loadedTable = Table.getColumnInfo(colInfoList);

            //Read next lines, which are the rows
            line = br.readLine();
            while (line != null) {
                if (line.equals("") && colInfoList.size() > 2) {
                    throw new RuntimeException();
                }
                String[] rowInfoString = line.split(",");
                List<String> rowInfo = new ArrayList<>();
                Collections.addAll(rowInfo, rowInfoString);
                loadedTable.addRow(rowInfo);
                line = br.readLine();
            }
            tableName = tableName.trim();
            String[] path = tableName.split("/");
            //The table name in database should be the last in this path
            tableName = path[path.length - 1];
            tables.put(tableName, loadedTable);
            return "";
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: No table " + tableName + " found");
            throw new RuntimeException();
        } catch (IOException e) {
            System.out.println("Error: IO Exception");
            throw new RuntimeException();
        }
    }

    /* Prints the table */
    private String printTable(String tableName) {
        if (!tables.containsKey(tableName)) {
            return "ERROR: No table " + tableName + " found in database";
        }
        //Gets the table from database and obtains the list of columns
        Table t = tables.get(tableName);
        return t.print();
    }

    /* Inserts a row into the table */
    private String insertRow(String tableName, List<String> rowInfo) {
        if (tables.containsKey(tableName)) {
            Table table = tables.get(tableName);
            table.addRow(rowInfo);
            return "";
        }
        return "ERROR: No table " + tableName + " in database";
    }
}
