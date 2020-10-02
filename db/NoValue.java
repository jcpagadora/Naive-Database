package db;

/**
 * Created by Joseph on 3/3/2017.
 * NoValue is its own class; integer representation is 0 with
 * string representation "NOVALUE"
 */
public class NoValue extends SpecialValue {

    private static final String repr = "NOVALUE";

    @Override
    public String toString() {
        return repr;
    }

}
