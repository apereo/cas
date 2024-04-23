package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.Serial;
import java.sql.Array;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Represents a simple person with its attributes.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
@Getter
@EqualsAndHashCode(of = "name")
@ToString
public class SimplePersonAttributes implements PersonAttributes {
    @Serial
    private static final long serialVersionUID = 2576711533477055700L;

    private final Map<String, List<Object>> attributes;

    private final String name;

    public SimplePersonAttributes(final String name, final Map<String, List<Object>> attributes) {
        this.attributes = buildImmutableAttributeMap(attributes);
        this.name = name;
    }

    public SimplePersonAttributes(final Map<String, List<Object>> attributes) {
        this.attributes = buildImmutableAttributeMap(attributes);
        this.name = attributes.containsKey("username")
            ? attributes.get("username").getFirst().toString()
            : UUID.randomUUID().toString();
    }

    public SimplePersonAttributes() {
        this(Map.of());
    }

    public SimplePersonAttributes(final String name) {
        this(name, Map.of());
    }

    /**
     * Build simple person from username attribute.
     *
     * @param attribute  the attribute
     * @param attributes the attributes
     * @return the simple person attributes
     */
    public static SimplePersonAttributes fromAttribute(final String attribute, final Map<String, List<Object>> attributes) {
        if (attributes.containsKey(attribute)) {
            return new SimplePersonAttributes(attributes.get(attribute).getFirst().toString(), attributes);
        }
        return new SimplePersonAttributes(attributes);
    }

    /**
     * Canonicalize simple person attributes.
     *
     * @param mode the mode
     * @return the simple person attributes
     */
    public SimplePersonAttributes canonicalize(final CaseCanonicalizationMode mode) {
        val newName = mode.canonicalize(getName());
        return new SimplePersonAttributes(newName, getAttributes());
    }

    protected Map<String, List<Object>> buildImmutableAttributeMap(final Map<String, List<Object>> attributes) {
        val valueBuilder = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        val arrayPattern = Pattern.compile("\\{(.*)\\}");
        for (val attrEntry : attributes.entrySet()) {
            val key = attrEntry.getKey();
            var value = attrEntry.getValue().stream().filter(Objects::nonNull).toList();
            if (!value.isEmpty()) {
                val result = value.getFirst();
                if (result instanceof Array) {
                    LOGGER.trace("Column [{}] is classified as a SQL array", key);
                    val values = result.toString();
                    LOGGER.trace("Converting SQL array values [{}] using pattern [{}]", values, arrayPattern.pattern());
                    val matcher = arrayPattern.matcher(values);
                    if (matcher.matches()) {
                        val groups = matcher.group(1).split(",");
                        value = List.of((Object[]) groups);
                        LOGGER.trace("Converted SQL array values [{}]", values);
                    }
                }
            }
            LOGGER.trace("Collecting attribute [{}] with value(s) [{}]", key, value);
            valueBuilder.put(key, value);
        }
        return valueBuilder;
    }

    @Override
    public Object getAttributeValue(final String name) {
        val values = this.attributes.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.getFirst();
    }

    @Override
    public List<Object> getAttributeValues(final String name) {
        return this.attributes.get(name);
    }
}
