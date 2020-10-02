package db;

/**
 * Created by Joseph on 3/4/2017.
 */
public class ColumnExpression extends BinaryExpression {

    //String representations of valid operators
    private static final String[] validOperators = new String[]{"+", "-", "*", "/"};

    private String alias;

    /* Constructor for column expression */
    ColumnExpression(String colExpression, String alias) {
        super(colExpression, validOperators);
        this.alias = alias;
    }

    /* Selects the alias */
    String getAlias() {
        return alias;
    }

    /* Evaluates the operator in the column expression */
    @Override
    protected String evalOperator(String colExpression, String[] validOperators) {
        //Checks if colExpression contains one and only one of the valid operators
        String operator = super.evalOperator(colExpression, validOperators);
        if (operator.length() > 1) {
            throw new RuntimeException();
        }
        return operator;
    }

    /* Evaluates the binary expression */
    Object evalExpression(Object arg1, Object arg2) {
        //Checks if expression is unary and trivially returns arg1
        if (arg2.equals("")) {
            return arg1;
        }
        //Checks if either operand is NaN
        if (arg1 instanceof NaN || arg2 instanceof NaN) {
            return new NaN();
        }
        //Checks if both operands are NOVALUE
        if (arg1 instanceof NoValue && arg2 instanceof NoValue) {
            return new NoValue();
        }

        //Turns each argument into a string
        String arg1String = arg1.toString();
        String arg2String = arg2.toString();
        //Removes any surrounding quotes
        arg2String = arg2String.replace("'", "").replace("\"", "");

        //Checks if both argument strings are formatted as floats
        String arg1TestNum = arg1String.replaceAll("[*0-9]", "").replace(" ", "");
        String arg2TestNum = arg2String.replaceAll("[*0-9]", "").replace(" ", "");
        //Removes any signs
        arg1TestNum = arg1TestNum.replace("-", "").replace("+", "");
        arg2TestNum = arg2TestNum.replace("-", "").replace("+", "");

        //Checks if dealing with NoValue
        if(arg1String.equals("NOVALUE")) {
            arg1TestNum = "";
        }
        if (arg2String.equals("NOVALUE")) {
            arg2TestNum = "";
        }

        //If removing all numbers gets empty string, then item is integer
        if (arg1TestNum.equals("") && arg2TestNum.equals("")) {
            int item1 = parseInt(arg1String);
            int item2 = parseInt(arg2String);
            return evalArithmetic(item1, item2);
        }
        //If just one decimal point is left, then it is a float
        else if (arg1TestNum.equals(".") || arg2TestNum.equals(".")) {
            float item1 = parseFloat(arg1String);
            float item2 = parseFloat(arg2String);
            return evalArithmetic(item1, item2);
        } else {
            //Otherwise, the args are string, and we use the string comparators
            switch (operator) {
                case "+":
                    //Treats NOVALUE as empty string
                    if (arg1String.equals("NOVALUE")) {
                        arg1String = "";
                    }
                    if (arg2String.equals("NOVALUE")) {
                        arg2String = "";
                    }
                    return arg1String + arg2String;
                default:
                    throw new RuntimeException(operator + " cannot be applied to string type");
            }
        }
    }

    /*Evaluates arithmetic expressions with floats */
    private Object evalArithmetic(float item1, float item2) {
        switch (operator) {
            case "+":
                return item1 + item2;
            case "-":
                return item1 - item2;
            case "*":
                return item1 * item2;
            case "/":
                if (item2 == 0) {
                    return new NaN();
                }
                return item1 / item2;
        }
        throw new RuntimeException();
    }

    /*Evaluates arithmetic expressions with integers */
    private Object evalArithmetic(int item1, int item2) {
        switch (operator) {
            case "+":
                return item1 + item2;
            case "-":
                return item1 - item2;
            case "*":
                return item1 * item2;
            case "/":
                if (item2 == 0) {
                    return new NaN();
                }
                return item1 / item2;
        }
        throw new RuntimeException();
    }

    /* Parses string into float, including case where string is no value */
    float parseFloat(String str) {
        if (str.equals("NOVALUE")) {
            return 0;
        }
        return Float.parseFloat(str);
    }

    /* Parses string into int, including case where string is no value */
    int parseInt(String str) {
        if (str.equals("NOVALUE")) {
            return 0;
        }
        return Integer.parseInt(str);
    }

}
