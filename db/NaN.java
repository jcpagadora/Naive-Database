package db;

/**
 * Created by gilbertlin on 3/4/17.
 */
public class NaN extends SpecialValue {
    private String repr = "NaN";

    private float NumRepr = Float.POSITIVE_INFINITY;

    @Override
    public String toString() {
        return repr;
    }

    public float getNumRepr() {
        return NumRepr;
    }
}
