package org.apereo.cas.configuration.metadata;

import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapSearchEntryHandlersProperties;
import org.apereo.cas.util.model.Capacity;
import org.apereo.cas.util.model.TriStateBoolean;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.support.QueryType;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
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
                + PrincipalTransformationProperties.CaseConversion.class.getSimpleName() + '|'
                + QueryType.class.getSimpleName() + '|'
                + AbstractLdapProperties.LdapType.class.getSimpleName() + '|'
                + CaseCanonicalizationMode.class.getSimpleName() + '|'
                + TriStateBoolean.class.getSimpleName() + '|'
                + Capacity.class.getSimpleName() + '|'
                + PasswordPolicyProperties.PasswordPolicyHandlingOptions.class.getSimpleName() + '|'
                + LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes.class.getSimpleName() + '|'
                + Map.class.getSimpleName() + '|'
                + Resource.class.getSimpleName() + '|'
                + List.class.getSimpleName() + '|'
                + Set.class.getSimpleName());
    }

    private final Set<ConfigurationMetadataProperty> properties;

    private final Set<ConfigurationMetadataProperty> groups;

    private final boolean indexNameWithBrackets;

    private final String parentClass;

    private final String sourcePath;

    private static boolean shouldTypeBeExcluded(final ClassOrInterfaceType type) {
        return EXCLUDED_TYPES.matcher(type.getNameAsString()).matches();
    }

    @Override
    public void visit(final FieldDeclaration field, final ConfigurationMetadataProperty property) {
        if (field.getVariables().isEmpty()) {
            throw new IllegalArgumentException("Field " + field + " has no variable definitions");
        }
        val var = field.getVariable(0);
        if (field.getModifiers().contains(Modifier.staticModifier())) {
            LOGGER.debug("Field [{}] is static and will be ignored for metadata generation", var.getNameAsString());
            return;
        }
        if (field.getJavadoc().isEmpty()) {
            LOGGER.error("Field [{}] has no Javadoc defined", field);
        }
        val creator = new ConfigurationMetadataPropertyCreator(indexNameWithBrackets, properties, groups, parentClass);
        val prop = creator.createConfigurationProperty(field, property.getName());
        processNestedClassOrInterfaceTypeIfNeeded(field, prop);
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
