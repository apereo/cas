package org.apereo.cas.configuration.metadata;

import org.apereo.cas.util.model.TriStateBoolean;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This is {@link ConfigurationMetadataFieldVisitor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ConfigurationMetadataFieldVisitor extends VoidVisitorAdapter<ConfigurationMetadataProperty> {
    private static final Pattern EXCLUDED_TYPES;

    static {
        EXCLUDED_TYPES = Pattern.compile(
            String.class.getSimpleName() + '|'
                + Integer.class.getSimpleName() + '|'
                + Double.class.getSimpleName() + '|'
                + Long.class.getSimpleName() + '|'
                + Float.class.getSimpleName() + '|'
                + Boolean.class.getSimpleName() + '|'
                + TriStateBoolean.class.getSimpleName() + '|'
                + Resource.class.getSimpleName() + '|'
                + Map.class.getSimpleName() + '|'
                + List.class.getSimpleName() + '|'
                + Set.class.getSimpleName());
    }

    private final Set<ConfigurationMetadataProperty> properties;

    private final Set<ConfigurationMetadataProperty> groups;

    private final boolean indexNameWithBrackets;

    private final String parentClass;

    private final String sourcePath;

    @Getter
    private ConfigurationMetadataProperty result;

    private static boolean shouldTypeBeExcluded(final ClassOrInterfaceType type) {
        return EXCLUDED_TYPES.matcher(type.getNameAsString()).matches();
    }

    @Override
    public void visit(final FieldDeclaration field, final ConfigurationMetadataProperty property) {
        if (field.getVariables().isEmpty()) {
            throw new IllegalArgumentException("Field " + field + " has no variable definitions");
        }
        val variable = field.getVariable(0);
        if (field.getModifiers().contains(Modifier.staticModifier())) {
            LOGGER.debug("Field [{}] is static and will be ignored for metadata generation", variable.getNameAsString());
            return;
        }
        if (field.getJavadoc().isEmpty()) {
            LOGGER.error("Field [{}] has no Javadoc defined", field);
        }

        val creator = new ConfigurationMetadataPropertyCreator(indexNameWithBrackets, properties, groups, parentClass);
        result = creator.createConfigurationProperty(field, property.getName());
        processNestedClassOrInterfaceTypeIfNeeded(field, result);
    }

    private void processNestedClassOrInterfaceTypeIfNeeded(final FieldDeclaration n, final ConfigurationMetadataProperty prop) {
        if (n.getElementType() instanceof ClassOrInterfaceType) {
            val type = (ClassOrInterfaceType) n.getElementType();
            if (!shouldTypeBeExcluded(type)) {
                val instance = ConfigurationMetadataClassSourceLocator.getInstance();
                val clz = instance.locatePropertiesClassForType(type);
                if (clz != null && !clz.isMemberClass()) {
                    val typePath = ConfigurationMetadataClassSourceLocator.buildTypeSourcePath(this.sourcePath, clz.getName());
                    val parser = new ConfigurationMetadataUnitParser(this.sourcePath);
                    parser.parseCompilationUnit(properties, groups, prop, typePath, clz.getName(), false);
                }
            }
        }
    }

}
