package camp.computer.util;

import java.util.ArrayList;
import java.util.List;

public class Tuple<T> {

    public List<T> values = new ArrayList<>();

    public Tuple(T... values) {

        for (int valueIndex = 0; valueIndex < values.length; valueIndex++) {
            if (!this.values.contains(values[valueIndex])) {
                this.values.add(values[valueIndex]);
            }
        }

    }

}
