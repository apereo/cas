package org.apereo.cas.configuration.metadata;

import org.apereo.cas.configuration.support.RelaxedPropertyNames;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

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
    private final boolean indexNameWithBrackets;
    private final Set<ConfigurationMetadataProperty> properties;
    private final Set<ConfigurationMetadataProperty> groups;
    private final String parentClass;

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

        val elementType = fieldDecl.getElementType().asString();
        if (elementType.equals(String.class.getSimpleName())
            || elementType.equals(Integer.class.getSimpleName())
            || elementType.equals(Long.class.getSimpleName())
            || elementType.equals(Double.class.getSimpleName())
            || elementType.equals(Float.class.getSimpleName())) {
            prop.setType("java.lang." + elementType);
        } else {
            prop.setType(elementType);
        }

        val initializer = variable.getInitializer();
        if (initializer.isPresent()) {
            val exp = initializer.get();
            if (exp instanceof LiteralStringValueExpr) {
                prop.setDefaultValue(((LiteralStringValueExpr) exp).getValue());
            } else if (exp instanceof BooleanLiteralExpr) {
                prop.setDefaultValue(((BooleanLiteralExpr) exp).getValue());
            }
        }
        properties.add(prop);

        val grp = new ConfigurationMetadataProperty();
        grp.setId(indexedGroup);
        grp.setName(indexedGroup);
        grp.setType(parentClass);
        groups.add(grp);

        return prop;
    }
}
