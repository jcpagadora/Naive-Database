package db;

/**
 * Created by Joseph on 2/27/2017.
 */
public class Conditional extends BinaryExpression {

    //String representations of comparison operations
    private static final String[] validComparators = new String[]{"==", "<", ">", "<=", ">=", "!="};

    /* Conditional constructor */
    Conditional(String cond) {
        super(cond, validComparators);
    }

    //Returns the conditional used to compare
    @Override
    protected String evalOperator(String cond, String[] validComparators) {
        //Checks if cond contains one and only one of the comparators
        String compareOp = super.evalOperator(cond, validComparators);
        //Removes possible repetitions
        compareOp = compareOp.replace("<<", "<");
        compareOp = compareOp.replace(">>", ">");
        if (compareOp.length() > 2) {
            throw new RuntimeException();
        }
        return compareOp;
    }

    /* Comparison method of conditional */
    boolean compare(Object arg1, Object arg2) {
        //Checks if either argument is NOVALUE
        if (arg1 instanceof NoValue || arg2 instanceof NoValue) {
            return false;
        }
        String arg1String = arg1.toString();
        String arg2String = arg2.toString();
        //Removes surrounding quotes if present
        arg2String = arg2String.replace("'", "").replace("\"", "");
        try {
            //Since ints are floats we compare using floats
            float item1 = parseFloat(arg1String);
            float item2 = parseFloat(arg2String);
            switch (operator) {
                case "==":
                    return item1 == item2;
                case "<=":
                    return item1 <= item2;
                case "<":
                    return item1 < item2;
                case ">=":
                    return item1 >= item2;
                case ">":
                    return item1 > item2;
                default:
                    return item1 != item2;
            }
        } catch (NumberFormatException e) {
            //Otherwise, the args are string, and we use the string comparators
            switch (operator) {
                case "==":
                    return arg1String.equals(arg2String);
                case "<=":
                    return arg1String.compareTo(arg2String) <= 0;
                case "<":
                    return arg1String.compareTo(arg2String) < 0;
                case ">=":
                    return arg1String.compareTo(arg2String) >= 0;
                case ">":
                    return arg1String.compareTo(arg2String) > 0;
                default:
                    return !arg1String.equals(arg2String);
            }
        }
    }

    /* Parses string into float, including case of NaN */
    private float parseFloat(String str) {
        if (str.equals("NaN")) {
            return Float.POSITIVE_INFINITY;
        }
        return Float.parseFloat(str);
    }

}
