package db;

import java.util.*;
import java.lang.Object;

/**
 * Created by Joseph on 2/21/2017.
 */
public class Table {

    // Table is implemented as a list of columns
    private List<Column> table;

    // The names of the columns of the table cached in a set; used for joins operation
    private Set<String> columnNames;

    /**
     *  Constructor method for table to create new table; sets table name,
     *  creates table, and stores a list of its column names
     */
    Table(List<Column> columns) {
        table = columns;
        columnNames = new LinkedHashSet<>();

        // Adds the names of every column to the list of column names
        for (Column c : table) {
            columnNames.add(c.columnName);
        }
    }

    /**
     * Gets column data as a list of strings and inputs it into new columns, which are
     * used to create a new table
     */
    static Table getColumnInfo(List<String> columnInfo) {
        List<Column> columns = new ArrayList<>();
        int colInfoLength = columnInfo.size();
        //Iterates through columnInfo; if correctly formatted, even-indexed items are column
        //names, and odd-indexed items are column types
        for (int i = 0; i < colInfoLength; i = i + 2) {
            String columnName = columnInfo.get(i);
            String columnType = columnInfo.get(i + 1);
            Column c = new Column(columnName, columnType);
            columns.add(c);
        }
        return new Table(columns);
    }

    /* Gets data from colExprs to create a list of column expressions */
    private List<ColumnExpression> getColExpressions(String[] colExprs) {
        // Creates a list of column expressions
        List<ColumnExpression> columnExpressions = new ArrayList<>();
        for (String expr : colExprs) {
            expr = expr.trim();
            String[] exprSplit = expr.split(" as ");
            // Gets binary expression and trims whitespace
            String binaryExpr = exprSplit[0].trim();
            // Gets the column alias of the expression and trims whitespace
            String colAlias;
            if (exprSplit.length > 1) {
                colAlias = exprSplit[1].trim();
            } else {
                // No alias given
                colAlias = null;
            }
            ColumnExpression newColExpr = new ColumnExpression(binaryExpr, colAlias);
            columnExpressions.add(newColExpr);
        }
        return columnExpressions;
    }

    /**
     * Selects the given column from the table; evaluates column expressions
     */
    Table selectColumns(String[] colExprs) {
        //Creates a list of column expressions
        List<ColumnExpression> columnExpressions = getColExpressions(colExprs);

        //The list of selected columns to be placed in the table
        List<Column> selectedCols = new ArrayList<>();

        for (ColumnExpression colExpr : columnExpressions) {
            String colName1 = colExpr.getArg1();
            //Operand2 since it could be empty, a literal, or a column name
            String operand2 = colExpr.getArg2();
            //Gets a copy of column with name <colName1>
            Column column1 = getColumn(colName1).copy();
            String col1Type = column1.columnType;

            //The column type of the new column, if column types are equal, default is col1Type
            String newColType = col1Type;

            int rows = column1.size();

            //If unary expression, simply copy column
            if (operand2.equals("")) {
                selectedCols.add(column1);
            } else if (columnNames.contains(operand2)) {
                //Evaluate binary expression where operand2 is a column name
                //Gets appropriate type from arguments
                Column column2 = getColumn(operand2).copy();
                String col2Type = column2.columnType;

                //The only case where unequal types are allowed is with ints and floats
                if (!col1Type.equals(col2Type)) {
                    if ((col1Type.equals("int") && col2Type.equals("float")) ||
                            col1Type.equals("float") && col2Type.equals("int")) {
                        newColType = "float";
                    }
                }
                //Creates a new column to be added to selectedCols
                Column newCol = new Column(colExpr.getAlias(), newColType);

                for (int i = 0; i < rows; i++) {
                    Object arg1 = column1.get(i);
                    Object arg2 = column2.get(i);
                    newCol.add(colExpr.evalExpression(arg1, arg2));
                }
                selectedCols.add(newCol);
            }
            else {
                //Evaluate binary expression where operand2 is a literal value
                if (col1Type.equals("int")) {
                    //If both operands are integers then the return type is an int
                    newColType = "int";
                } else {
                    newColType = "float";
                }
                Object arg2 = Float.parseFloat(operand2);

                Column newCol = new Column(colExpr.getAlias(), newColType);
                for (int i = 0; i < rows; i++) {
                    Object arg1 = column1.get(i);
                    newCol.add(colExpr.evalExpression(arg1, arg2));
                }
                selectedCols.add(newCol);
            }
        }
        return new Table(selectedCols);
    }

    /* Aggregates a column in the table, according to the given aggregation function.
       An aggregation function takes in a numerical column, and aggregates all the numbers
       into one summarizing number, e.g., average, sum, count.
     */
    Table aggregate(String agg_func, String col_name) {
        Column col = getColumn(col_name);
        if (col.columnType == "string") {
            System.out.println("Columns of strings are invalid for aggregations.");
            throw new RuntimeException();
        }
        Aggregation agg = new Aggregation(agg_func, col);
        float result = agg.aggregate();
        Column agg_col = new Column(agg_func + "(" + col_name + ")", "float");
        agg_col.add(result);
        List<Column> cols = new LinkedList<>();
        cols.add(agg_col);
        return new Table(cols);
    }

    /* Gets indices of rows where conditions are not satisfied */
    void removeRowsConditions(List<Conditional> conditions) {
        //Iterates through the rows of the table, adds indices of rows that do
        //not satisfy the conditions
        List<Integer> indicesToRemove = new ArrayList<>();
        //Iterates through conditions
        for (Conditional cond : conditions) {
            //Gets a copy of the column; cond.getArg1() should return the name of
            // the column to be compared
            Column colToCompare = getColumn(cond.getArg1().trim()).copy();
            String arg2 = cond.getArg2();
            //Gets the number of rows
            int rows = colToCompare.size();

            //Checks whether the compare is unary or binary
            if (columnNames.contains(arg2)) {
                //Binary compare
                Column otherCol = getColumn(arg2.trim()).copy();
                //Iterates through the rows of this column and the other column
                for (int i = 0; i < rows; i++) {
                    Object objToCompare1 = colToCompare.get(i);
                    Object objToCompare2 = otherCol.get(i);
                    if (!cond.compare(objToCompare1, objToCompare2)) {
                        indicesToRemove.add(i);
                    }
                }
            } else {
                //Unary compare
                for (int i = 0; i < rows; i++) {
                    //Removes the corresponding rows from each column in the table
                    //Compares current item to the literal value given
                    if (!cond.compare(colToCompare.get(i), arg2)) {
                        indicesToRemove.add(i);
                    }
                }
            }
        }
        removeRows(indicesToRemove);
    }

    /* Removes the given rows of the table */
    private void removeRows(List<Integer> rowsToRemove) {
        int rows = table.get(0).size();
        for (int i = rows - 1; i >=0; i--) {
            if (rowsToRemove.contains(i)) {
                for (Column c : table) {
                    c.remove(i);
                }
            }
        }
    }

    /**
     * Makes a copy of the table
     */
    Table copy(){
        List<Column> copiedCols = new ArrayList<>();
        //Copies each column in the table
        for (Column col : table) {
            copiedCols.add(col.copy());
        }
        return new Table(copiedCols);
    }

    /**
     * Adds row to table; implements insert command
     */
    void addRow(List<String> row) {
        //Gets the number of columns
        int numCols = table.size();
        //Error if row and column sizes differ
        if (row.size() != numCols) {
            throw new RuntimeException();
        }
        //A deque of the items to add, at each iteration add an item to the front
        //of the deque, then add the items to the columns starting at the end
        ArrayDeque<Object> itemsToAdd = new ArrayDeque<>();
        //Checks if the types match up; gets class of the item in rows, then converts
        //it into a string in lowercase
        for (int i = 0; i < numCols; i++) {
            Column column = table.get(i);
            String itemString = row.get(i).trim();

            //Takes the string of the item to add and formats it correctly
            Object item = column.parseString(itemString);
            itemsToAdd.addFirst(item);
        }
        //If no error occurs, add all items to table
        for (Column c : table) {
            c.add(itemsToAdd.removeLast());
        }
    }

    /**
     * Performs join operation of two tables. If they share columns, append the rows
     * of the table where the values of the shared columns are the same. If the shared
     * columns don't share any values, then return an empty table. If they don't share
     * any columns, then return the Cartesian product of the tables.
     */
    Table join(Table otherTable) {
        Set<String> joinedColumnNames = new LinkedHashSet<>();
        //Holds sets of table1, table 2, & shared column names for utility
        List<String> table1Names = new ArrayList<>(columnNames);
        List<String> table2Names = new ArrayList<>(otherTable.columnNames);
        List<String> sharedColumnNames = new ArrayList<>();

        //Iterates through column names in this table and compares to column names in
        //other table; if shared, then add to joined column names and shared column names
        //and remove from table names
        for (Column col : table) {
            String colName = col.columnName;
            if (table2Names.contains(colName)) {
                joinedColumnNames.add(colName);
                sharedColumnNames.add(colName);
                table1Names.remove(colName);
                table2Names.remove(colName);
            }
        }

        //Adds the rest of the columns to joined column names
        joinedColumnNames.addAll(table1Names);
        joinedColumnNames.addAll(table2Names);

        //Iterates through joined table names to create the joined table
        List<Column> joinedTableColumns = new ArrayList<>();

        for (String name : joinedColumnNames) {
            Column colToAdd;
            if (columnNames.contains(name)) {
                colToAdd = new Column(name, getColumn(name).columnType);
            } else {
                colToAdd = new Column(name, otherTable.getColumn(name).columnType);
            }
            joinedTableColumns.add(colToAdd);
        }

        //Joined table to return
        Table joinedTable = new Table(joinedTableColumns);

        //If the tables don't share any columns, return their cartesian product
        if (sharedColumnNames.isEmpty()) {
            return cartesianProduct(otherTable, joinedTable);
        }

        //Gets length of items in this table
        int thisLength = table.get(0).size();
        //Gets length of items in other table
        int otherLength = otherTable.table.get(0).size();

        //Iterates through the rows in this table to check for same values for shared columns
        for (int i = 0; i < thisLength; i++) {

            List<String> rowToAdd = new ArrayList<>();

            //Iterates through the rows in the other table
            for (int j = 0; j < otherLength; j++) {
                //Whether to add a row or not
                boolean addRow = true;
                //Iterates through shared column names in this table
                for (String s : sharedColumnNames) {
                    Column sharedColThis = getColumn(s);
                    Object item1 = sharedColThis.get(i);
                    Column sharedColOther = otherTable.getColumn(s);
                    Object item2 = sharedColOther.get(j);
                    //If value is unequal, break from for-loop and move on to next row
                    if (!item1.equals(item2)) {
                        addRow = false;
                        break;
                    }
                }
                if (addRow) {
                    for (String nameToAdd : joinedColumnNames) {
                        if (table1Names.contains(nameToAdd)) {
                            //Adds the i-th item in this table's column as a string
                            rowToAdd.add(getColumn(nameToAdd).get(i).toString());
                        } else {
                            //Adds the j-th item in the other table's column as a string
                            rowToAdd.add(otherTable.getColumn(nameToAdd).get(j).toString());
                        }
                    }
                    joinedTable.addRow(rowToAdd);
                    rowToAdd = new ArrayList<>();
                }
            }
        }
        return joinedTable;
    }

    /**
     * Returns the cartesian product of tables 1 & 2. That is, for each row in table 1,
     * we append to it a row in table 2 for each row in table 2. Takes in the joined table
     * with new columns already created
     */
    private Table cartesianProduct(Table otherTable, Table joinedTable) {

        //Gets length of column items in this table and in other table
        Column firstCol1 = table.get(0);
        int thisLength = firstCol1.size();

        Column firstCol2 = otherTable.table.get(0);
        int otherLength = firstCol2.size();

        //Iterate through the rows in this table and through each row in other table,
        //adding each item to the row to be added to joined table
        for (int i = 0; i < thisLength; i++) {

            Column colItemsOther;
            for (int j = 0; j < otherLength; j++) {

                //Iterate through the columns in this table to add items to rowToAdd
                Column colItemsThis;
                List rowToAdd = new ArrayList<>();
                for (Column c1 : table) {
                    colItemsThis = c1;
                    rowToAdd.add(colItemsThis.get(i).toString());
                }
                //Iterate through the columns in other table to add items to rowToAdd
                for (Column c2 : otherTable.table) {
                    colItemsOther = c2;
                    rowToAdd.add(colItemsOther.get(j).toString());
                }

                //Iteration for one row complete; adds row to joined table
                joinedTable.addRow(rowToAdd);
            }
        }

        return joinedTable;
    }

    /**
     * Returns the column in the table with the given name
     */
    private Column getColumn(String name) {
        for (Column column : table) {
            if (column.columnName.equals(name)) {
                return column;
            }
        }
        throw new RuntimeException("No column " + name + " found.");
    }

    /* Stores the table as a string using a string builder */
    String print() {

            StringBuilder stringBuilder = new StringBuilder();
            //Prints the first row which is the list of column names and types
            for (int i = 0; i < table.size(); i++) {
                if (i > 0) {
                    stringBuilder.append(", ");
                }
                Column column = table.get(i);
                stringBuilder.append(column.columnName + " " + column.columnType);
            }
            stringBuilder.append("\n");
            //Iterates through the rows, defined by the size of the list of items of each column
            int rows = table.get(0).size();
            for (int j = 0; j < rows; j++) {

                //For each row, iterate through the columns
                for (int i = 0; i < table.size(); i++) {
                    if (i > 0) {
                        stringBuilder.append(",");
                    }
                    //Gets the items list of the column
                    Column col = table.get(i);
                    Object item = col.get(j);

                    //Formats the item as a string and adds it to the string builder
                    stringBuilder.append(col.format(item));
                }
                if (j != rows - 1) {
                    stringBuilder.append("\n");
                }
            }
            return stringBuilder.toString();
    }

    /* Prints the schema of the table using a string builder */
    String schema() {
        StringBuilder stringBuilder = new StringBuilder();
        int flag = 1;
        for (Column col : table) {
            if (flag == 0) {
                stringBuilder.append(", ");
            }
            flag = 0;
            stringBuilder.append(col.columnName + " " + col.columnType);
        }
        return stringBuilder.toString();
    }

}