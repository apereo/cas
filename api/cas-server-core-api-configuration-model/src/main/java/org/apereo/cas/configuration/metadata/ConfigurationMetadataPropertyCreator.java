package org.apereo.cas.configuration.metadata;

import org.apereo.cas.configuration.support.RelaxedPropertyNames;

import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * This is {@link ConfigurationMetadataPropertyCreator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ConfigurationMetadataPropertyCreator {
    private static final Map<String, String> PRIMITIVES = Map.of(
        String.class.getSimpleName(), String.class.getName(),
        Integer.class.getSimpleName(), Integer.class.getName(),
        Boolean.class.getSimpleName(), Boolean.class.getName(),
        Long.class.getSimpleName(), Long.class.getName(),
        Double.class.getSimpleName(), Double.class.getName(),
        Float.class.getSimpleName(), Float.class.getName());

    private final boolean indexNameWithBrackets;

    private final Set<ConfigurationMetadataProperty> properties;

    private final Set<ConfigurationMetadataProperty> groups;

    private final String parentClass;

    /**
     * Collect javadocs enum fields.
     *
     * @param prop the prop
     * @param em   the em
     * @return the string builder
     */
    public static StringBuilder collectJavadocsEnumFields(final ConfigurationMetadataProperty prop,
                                                          final EnumDeclaration em) {
        val builder = new StringBuilder(StringUtils.defaultString(prop.getDescription()));
        builder.append("\nAvailable values are as follows:\n");
        builder.append("<ul>");
        em.getEntries()
            .stream()
            .filter(entry -> entry.getJavadoc().isPresent())
            .forEach(entry -> {
                var text = entry.getJavadoc().get().getDescription().toText();
                text = StringUtils.appendIfMissing(text, ".");
                val member = String.format("<li>{@code %s}: %s</li>", entry.getNameAsString(), text);
                builder.append(member);
            });
        builder.append("</ul>");
        return builder;
    }

    /**
     * Create configuration property.
     *
     * @param fieldDecl the field decl
     * @param propName  the prop name
     * @return the configuration metadata property
     */
    public ConfigurationMetadataProperty createConfigurationProperty(final FieldDeclaration fieldDecl, final String propName) {
        val variable = fieldDecl.getVariables().get(0);
        val name = StreamSupport.stream(RelaxedPropertyNames.forCamelCase(variable.getNameAsString()).spliterator(), false)
            .map(Object::toString)
            .findFirst()
            .orElseGet(variable::getNameAsString);

        val indexedGroup = propName.concat(indexNameWithBrackets ? "[]" : StringUtils.EMPTY);
        val indexedName = indexedGroup.concat(".").concat(name);

        val prop = new ConfigurationMetadataProperty();
        if (fieldDecl.getJavadoc().isPresent()) {
            val description = fieldDecl.getJavadoc().get().getDescription().toText();
            prop.setDescription(description);
            prop.setShortDescription(StringUtils.substringBefore(description, "."));
        } else {
            LOGGER.error("No Javadoc found for field [{}]", indexedName);
        }
        prop.setName(indexedName);
        prop.setId(indexedName);
        
        var elementType = fieldDecl.getElementType();
        val elementTypeStr = elementType.asString();
        if (PRIMITIVES.containsKey(elementTypeStr)) {
            prop.setType(PRIMITIVES.get(elementTypeStr));
        } else if (elementTypeStr.startsWith("Map<") || elementTypeStr.startsWith("List<") || elementTypeStr.startsWith("Set<")) {
            prop.setType("java.util." + elementTypeStr);
            var typeName = elementTypeStr.substring(elementTypeStr.indexOf('<') + 1, elementTypeStr.indexOf('>'));
            var parent = fieldDecl.getParentNode().get();
            parent.findFirst(EnumDeclaration.class, em -> em.getNameAsString().contains(typeName))
                .ifPresent(em -> {
                    var builder = collectJavadocsEnumFields(prop, em);
                    prop.setDescription(builder.toString());
                });
        } else {
            prop.setType(elementTypeStr);
            var parent = fieldDecl.getParentNode().get();
            
            var enumDecl = parent.findFirst(EnumDeclaration.class, em -> em.getNameAsString().contains(elementTypeStr));
            if (enumDecl.isPresent()) {
                val em = enumDecl.get();
                var builder = collectJavadocsEnumFields(prop, em);
                prop.setDescription(builder.toString());
                em.getFullyQualifiedName().ifPresent(prop::setType);
            }
        }

        val initializer = variable.getInitializer();
        if (initializer.isPresent()) {
            val exp = initializer.get();
            if (exp instanceof LiteralStringValueExpr) {
                prop.setDefaultValue(((LiteralStringValueExpr) exp).getValue());
            } else if (exp instanceof BooleanLiteralExpr) {
                prop.setDefaultValue(((BooleanLiteralExpr) exp).getValue());
            } else if (exp instanceof FieldAccessExpr) {
                prop.setDefaultValue(((FieldAccessExpr) exp).getNameAsString());
            }
        }
        properties.add(prop);

        val grp = new ComparableConfigurationMetadataProperty();
        grp.setId(indexedGroup);
        grp.setName(indexedGroup);
        grp.setType(parentClass);
        groups.add(grp);

        return prop;
    }

    private static class ComparableConfigurationMetadataProperty extends ConfigurationMetadataProperty {
        private static final long serialVersionUID = -7924691650447203471L;

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ConfigurationMetadataProperty)) {
                return false;
            }
            var rhs = (ConfigurationMetadataProperty) obj;
            return new EqualsBuilder().append(getId(), rhs.getId()).isEquals();
        }
    }
}
