package org.apereo.cas.authentication.principal.merger;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;


/**
 * Merger that retains values from both maps. If a value exists for
 * a key in both maps the following is done:
 * <ul>
 *  <li>If both maps have a {@link List} they are merged into a single {@link List}</li>
 *  <li>If one map has a {@link List} and the other a single value the value is added to the {@link List}</li>
 *  <li>If both maps have a single value a {@link List} is created from the two.</li>
 * </ul>
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
@Getter
@Setter
public class MultivaluedAttributeMerger extends BaseAdditiveAttributeMerger {
    @Serial
    private static final long serialVersionUID = 3089991136823527485L;
    private boolean distinctValues = true;

    @Override
    protected Map<String, List<Object>> mergePersonAttributes(final Map<String, List<Object>> toModify,
                                                              final Map<String, List<Object>> toConsider) {
        for (val sourceEntry : toConsider.entrySet()) {
            var sourceKey = sourceEntry.getKey();

            var values = toModify.computeIfAbsent(sourceKey, _ -> new ArrayList<>());

            val sourceValue = sourceEntry.getValue();
            if (this.distinctValues) {
                val temp = new TreeSet<>((o1, o2) -> {
                    if (o1 instanceof String && o2 instanceof String && o1.toString().equalsIgnoreCase(o2.toString())) {
                        return 0;
                    }
                    if (o1 instanceof Comparable && o2 instanceof Comparable
                        && o1.getClass().isAssignableFrom(o2.getClass())) {
                        return ((Comparable<Object>) o1).compareTo(o2);
                    }
                    return -1;
                });
                temp.addAll(values);
                temp.addAll(sourceValue);
                toModify.put(sourceKey, new ArrayList<>(temp));
            } else {
                values.addAll(sourceValue);
            }
        }

        return toModify;
    }
}
