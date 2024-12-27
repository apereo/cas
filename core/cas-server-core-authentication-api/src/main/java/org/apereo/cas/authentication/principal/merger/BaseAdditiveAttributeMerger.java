package org.apereo.cas.authentication.principal.merger;

import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.val;
import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Merges the Sets of Persons additively calling the abstract {@link #mergePersonAttributes(Map, Map)} method on the
 * attributes of Persons that exist in both sets. The {@link #mergeAvailableQueryAttributes(Set, Set)} and {@link #mergePossibleUserAttributeNames(Set, Set)}
 * methods do a simple additive merge of the sets. These can be overridden by subclasses.
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
public abstract class BaseAdditiveAttributeMerger implements AttributeMerger {

    @Serial
    private static final long serialVersionUID = -7112667508448078170L;

    @Override
    public Set<String> mergeAvailableQueryAttributes(final Set<String> toModify, final Set<String> toConsider) {
        toModify.addAll(toConsider);
        return toModify;
    }

    @Override
    public Set<String> mergePossibleUserAttributeNames(final Set<String> toModify, final Set<String> toConsider) {
        toModify.addAll(toConsider);
        return toModify;
    }

    @Override
    public final Set<PersonAttributes> mergeResults(final Set<PersonAttributes> toModify, final Set<PersonAttributes> toConsider) {
        val toModifyPeople = new LinkedHashMap<String, PersonAttributes>();
        for (val toModifyPerson : toModify) {
            toModifyPeople.put(toModifyPerson.getName(), toModifyPerson);
        }
        for (val toConsiderPerson : toConsider) {
            val toConsiderName = toConsiderPerson.getName();
            val toModifyPerson = toModifyPeople.get(toConsiderName);

            if (toModifyPerson == null) {
                toModify.add(toConsiderPerson);
            } else {
                val toModifyAttributes = this.buildMutableAttributeMap(toModifyPerson.getAttributes());
                val mergedAttributes = this.mergePersonAttributes(toModifyAttributes, toConsiderPerson.getAttributes());
                val mergedPerson = new SimplePersonAttributes(toConsiderName, mergedAttributes);
                toModify.remove(mergedPerson);
                toModify.add(mergedPerson);
            }
        }

        return toModify;
    }

    protected Map<String, List<Object>> buildMutableAttributeMap(final Map<String, List<Object>> attributes) {
        val mutableValuesBuilder = this.createMutableAttributeMap(attributes.size());
        for (val attrEntry : attributes.entrySet()) {
            val key = attrEntry.getKey();
            var value = attrEntry.getValue();
            if (value != null) {
                value = new ArrayList<>(value);
            }
            mutableValuesBuilder.put(key, value);
        }
        return mutableValuesBuilder;
    }

    protected Map<String, List<Object>> createMutableAttributeMap(final int size) {
        return new LinkedHashMap<>(size > 0 ? size : 1);
    }

    protected abstract Map<String, List<Object>> mergePersonAttributes(Map<String, List<Object>> toModify, Map<String, List<Object>> toConsider);


    @Override
    public Map<String, List<Object>> mergeAttributes(final Map<String, List<Object>> toModify, final Map<String, List<Object>> toConsider) {
        return this.mergePersonAttributes(Objects.requireNonNull(toModify), Objects.requireNonNull(toConsider));
    }
}
