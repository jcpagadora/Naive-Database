package db;

/**
 * Created by Joseph on 3/4/2017.
 */
public abstract class BinaryExpression {

    String operator;

    String[] operands;

    /* Constructor for binary expressions; catches run-time exception for malformed expressions */
    BinaryExpression(String expr, String[] validOperators) {
        try {
            operator = evalOperator(expr, validOperators);
            operands = getArguments(expr);
        } catch (RuntimeException e) {
            System.out.println("ERROR: Malformed binary expression");
        }
    }

    /* Evaluates the operator of the expression from valid operators and returns it as a string */
    protected String evalOperator(String expr, String[] validOperators) {
        String operator = "";
        for (String op : validOperators) {
            if (expr.contains(op)) {
                operator += op;
            }
        }
        //Return an adequate arbitrary string for placeholder operator:
        //This is in case unary expressions are permitted
        if (operator.isEmpty()) {
            return ";";
        }
        return operator;
    }

    /* Gets the arguments from the expression and puts them into an array */
    private String[] getArguments(String expr) {
        String exprReplace = expr.replace(operator, ",");
        String[] args = exprReplace.split(",");
        //Trims whitespace of the arguments
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].trim();
        }
        return args;
    }

    /* Selector method to get first argument */
    String getArg1() {
        return operands[0];
    }

    /* Selector method to get second argument */
    String getArg2() {
        try {
            return operands[1];
        } catch (IndexOutOfBoundsException e) {
            //Returns null if no second argument, in case unary expressions are permitted
            return "";
        }
    }
}
