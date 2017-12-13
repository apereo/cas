package org.apereo.cas.configuration.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapSearchEntryHandlersProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.services.persondir.support.QueryType;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.jooq.lambda.Unchecked;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedNames;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.core.io.Resource;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is {@link ConfigurationMetadataGenerator}.
 * This class is invoked by the build during the finalization of the compile phase.
 * Its job is to scan the generated configuration metadata and produce metadata
 * for settings that the build process is unable to parse. Specifically,
 * this includes fields that are of collection type (indexed) where the inner type is an
 * externalized class.
 * <p>
 * Example:
 * {@code
 * private List<SomeClassProperties> list = new ArrayList<>()
 * }
 * The generator additionally adds hints to the metadata generated to indicate
 * required properties and modules.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConfigurationMetadataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMetadataGenerator.class);
    private static final Pattern PATTERN_GENERICS = Pattern.compile(".+\\<(.+)\\>");
    private static final Pattern NESTED_TYPE_PATTERN = Pattern.compile("java\\.util\\.\\w+<(org\\.apereo\\.cas\\..+)>");

    private final String buildDir;
    private final String sourcePath;
    private final Map<String, Class> cachedPropertiesClasses = new HashMap<>();

    public ConfigurationMetadataGenerator(final String buildDir, final String sourcePath) {
        this.buildDir = buildDir;
        this.sourcePath = sourcePath;
    }

    /**
     * Main.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main(final String[] args) throws Exception {
        final String buildDir = args[0];
        final String projectDir = args[1];
        new ConfigurationMetadataGenerator(buildDir, projectDir).execute();
    }

    /**
     * Execute.
     *
     * @throws Exception the exception
     */
    public void execute() throws Exception {
        final File jsonFile = new File(buildDir, "classes/java/main/META-INF/spring-configuration-metadata.json");
        final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final TypeReference<Map<String, Set<ConfigurationMetadataProperty>>> values = new TypeReference<Map<String, Set<ConfigurationMetadataProperty>>>() {
        };
        final Map<String, Set> jsonMap = mapper.readValue(jsonFile, values);
        final Set<ConfigurationMetadataProperty> properties = jsonMap.get("properties");
        final Set<ConfigurationMetadataProperty> groups = jsonMap.get("groups");

        final Set<ConfigurationMetadataProperty> collectedProps = new HashSet<>();
        final Set<ConfigurationMetadataProperty> collectedGroups = new HashSet<>();

        properties.stream()
            .filter(p -> NESTED_TYPE_PATTERN.matcher(p.getType()).matches())
            .forEach(Unchecked.consumer(p -> {
                final Matcher matcher = NESTED_TYPE_PATTERN.matcher(p.getType());
                final boolean indexBrackets = matcher.matches();
                final String typeName = matcher.group(1);
                final String typePath = buildTypeSourcePath(typeName);

                parseCompilationUnit(collectedProps, collectedGroups, p, typePath, typeName, indexBrackets);

            }));

        properties.addAll(collectedProps);
        groups.addAll(collectedGroups);

        final Set<ConfigurationMetadataHint> hints = processHints(properties, groups);

        jsonMap.put("properties", properties);
        jsonMap.put("groups", groups);
        jsonMap.put("hints", hints);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final PrettyPrinter pp = new DefaultPrettyPrinter();
        mapper.writer(pp).writeValue(jsonFile, jsonMap);
    }

    private String buildTypeSourcePath(final String type) {
        final String newName = type.replace(".", File.separator);
        return sourcePath + "/src/main/java/" + newName + ".java";
    }

    private void parseCompilationUnit(final Set<ConfigurationMetadataProperty> collectedProps,
                                      final Set<ConfigurationMetadataProperty> collectedGroups,
                                      final ConfigurationMetadataProperty p,
                                      final String typePath,
                                      final String typeName,
                                      final boolean indexNameWithBrackets) {

        try (InputStream is = new FileInputStream(typePath)) {
            final CompilationUnit cu = JavaParser.parse(is);
            new FieldVisitor(collectedProps, collectedGroups, indexNameWithBrackets, typeName).visit(cu, p);
            if (cu.getTypes().size() > 0) {
                final ClassOrInterfaceDeclaration decl = ClassOrInterfaceDeclaration.class.cast(cu.getType(0));
                for (int i = 0; i < decl.getExtendedTypes().size(); i++) {
                    final ClassOrInterfaceType parentType = decl.getExtendedTypes().get(i);
                    final Class parentClazz = locatePropertiesClassForType(parentType);
                    final String parentTypePath = buildTypeSourcePath(parentClazz.getName());

                    parseCompilationUnit(collectedProps, collectedGroups, p,
                        parentTypePath, parentClazz.getName(), indexNameWithBrackets);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private class FieldVisitor extends VoidVisitorAdapter<ConfigurationMetadataProperty> {
        private final Set<ConfigurationMetadataProperty> properties;
        private final Set<ConfigurationMetadataProperty> groups;

        private final String parentClass;
        private final boolean indexNameWithBrackets;

        FieldVisitor(final Set<ConfigurationMetadataProperty> properties, final Set<ConfigurationMetadataProperty> groups,
                     final boolean indexNameWithBrackets, final String clazz) {
            this.properties = properties;
            this.groups = groups;
            this.indexNameWithBrackets = indexNameWithBrackets;
            this.parentClass = clazz;
        }

        @Override
        public void visit(final FieldDeclaration field, final ConfigurationMetadataProperty property) {
            if (field.getVariables().isEmpty()) {
                throw new IllegalArgumentException("Field " + field + " has no variable definitions");
            }
            final VariableDeclarator var = field.getVariable(0);
            if (field.getModifiers().contains(Modifier.STATIC)) {
                LOGGER.debug("Field [{}] is static and will be ignored for metadata generation", var.getNameAsString());
                return;
            }

            if (field.getJavadoc().isPresent()) {
                final ConfigurationMetadataProperty prop = createConfigurationProperty(field, property);
                processNestedClassOrInterfaceTypeIfNeeded(field, prop);
            } else {
                LOGGER.error("Field " + field + " has no Javadoc defined");
            }
        }

        private ConfigurationMetadataProperty createConfigurationProperty(final FieldDeclaration n,
                                                                          final ConfigurationMetadataProperty arg) {
            final VariableDeclarator variable = n.getVariables().get(0);
            final String name = StreamSupport.stream(RelaxedNames.forCamelCase(variable.getNameAsString()).spliterator(), false)
                .map(Object::toString)
                .findFirst()
                .orElse(variable.getNameAsString());

            final String indexedGroup = arg.getName().concat(indexNameWithBrackets ? "[]" : StringUtils.EMPTY);
            final String indexedName = indexedGroup.concat(".").concat(name);

            final ConfigurationMetadataProperty prop = new ConfigurationMetadataProperty();
            final String description = n.getJavadoc().get().getDescription().toText();
            prop.setDescription(description);
            prop.setShortDescription(StringUtils.substringBefore(description, "."));
            prop.setName(indexedName);
            prop.setId(indexedName);

            final String elementType = n.getElementType().asString();
            if (elementType.equals(String.class.getSimpleName())
                || elementType.equals(Integer.class.getSimpleName())
                || elementType.equals(Long.class.getSimpleName())
                || elementType.equals(Double.class.getSimpleName())
                || elementType.equals(Float.class.getSimpleName())) {
                prop.setType("java.lang." + elementType);
            } else {
                prop.setType(elementType);
            }

            if (variable.getInitializer().isPresent()) {
                final Expression exp = variable.getInitializer().get();
                if (exp instanceof LiteralStringValueExpr) {
                    prop.setDefaultValue(((LiteralStringValueExpr) exp).getValue());
                } else if (exp instanceof BooleanLiteralExpr) {
                    prop.setDefaultValue(((BooleanLiteralExpr) exp).getValue());
                }
            }
            properties.add(prop);

            final ConfigurationMetadataProperty grp = new ConfigurationMetadataProperty();
            grp.setId(indexedGroup);
            grp.setName(indexedGroup);
            grp.setType(this.parentClass);
            groups.add(grp);

            return prop;
        }

        private void processNestedClassOrInterfaceTypeIfNeeded(final FieldDeclaration n, final ConfigurationMetadataProperty prop) {
            if (n.getElementType() instanceof ClassOrInterfaceType) {
                final ClassOrInterfaceType type = (ClassOrInterfaceType) n.getElementType();
                if (!shouldTypeBeExcluded(type)) {
                    final Class clz = locatePropertiesClassForType(type);
                    if (clz != null && !clz.isMemberClass()) {
                        final String typePath = buildTypeSourcePath(clz.getName());
                        parseCompilationUnit(properties, groups, prop, typePath, clz.getName(), false);
                    }
                }
            }
        }

        private boolean shouldTypeBeExcluded(final ClassOrInterfaceType type) {
            return type.getNameAsString().matches(
                String.class.getSimpleName() + "|"
                    + Integer.class.getSimpleName() + "|"
                    + Double.class.getSimpleName() + "|"
                    + Long.class.getSimpleName() + "|"
                    + Float.class.getSimpleName() + "|"
                    + Boolean.class.getSimpleName() + "|"
                    + PrincipalTransformationProperties.CaseConversion.class.getSimpleName() + "|"
                    + QueryType.class.getSimpleName() + "|"
                    + AbstractLdapProperties.LdapType.class.getSimpleName() + "|"
                    + CaseCanonicalizationMode.class.getSimpleName() + "|"
                    + PasswordPolicyProperties.PasswordPolicyHandlingOptions.class.getSimpleName() + "|"
                    + LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes.class.getSimpleName() + "|"
                    + Map.class.getSimpleName() + "|"
                    + Resource.class.getSimpleName() + "|"
                    + List.class.getSimpleName() + "|"
                    + Set.class.getSimpleName());
        }

    }

    private Class locatePropertiesClassForType(final ClassOrInterfaceType type) {
        if (cachedPropertiesClasses.containsKey(type.getNameAsString())) {
            return cachedPropertiesClasses.get(type.getNameAsString());
        }

        final Predicate<String> filterInputs = s -> s.contains(type.getNameAsString());
        final Predicate<String> filterResults = s -> s.endsWith(type.getNameAsString());
        final String packageName = ConfigurationMetadataGenerator.class.getPackage().getName();
        final Reflections reflections =
            new Reflections(new ConfigurationBuilder()
                .filterInputsBy(filterInputs)
                .setUrls(ClasspathHelper.forPackage(packageName))
                .setScanners(new TypeElementsScanner()
                        .includeFields(false)
                        .includeMethods(false)
                        .includeAnnotations(false)
                        .filterResultsBy(filterResults),
                    new SubTypesScanner(false)));
        final Class clz = reflections.getSubTypesOf(Serializable.class).stream()
            .filter(c -> c.getSimpleName().equalsIgnoreCase(type.getNameAsString()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cant locate class for " + type.getNameAsString()));
        cachedPropertiesClasses.put(type.getNameAsString(), clz);
        return clz;
    }

    private Set<ConfigurationMetadataHint> processHints(final Collection<ConfigurationMetadataProperty> props,
                                                        final Collection<ConfigurationMetadataProperty> groups) {

        final Set<ConfigurationMetadataHint> hints = new LinkedHashSet<>();

        for (final ConfigurationMetadataProperty entry : props) {
            try {
                final String propName = StringUtils.substringAfterLast(entry.getName(), ".");
                final String groupName = StringUtils.substringBeforeLast(entry.getName(), ".");
                final ConfigurationMetadataProperty grp = groups
                    .stream()
                    .filter(g -> g.getName().equalsIgnoreCase(groupName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Cant locate group " + groupName));

                final Matcher matcher = PATTERN_GENERICS.matcher(grp.getType());
                final String className = matcher.find() ? matcher.group(1) : grp.getType();
                final Class clazz = ClassUtils.getClass(className);


                final ConfigurationMetadataHint hint = new ConfigurationMetadataHint();
                hint.setName(entry.getName());

                if (clazz.isAnnotationPresent(RequiresModule.class)) {
                    final RequiresModule annotation = Arrays.stream(clazz.getAnnotations())
                        .filter(a -> a.annotationType().equals(RequiresModule.class))
                        .findFirst()
                        .map(RequiresModule.class::cast)
                        .get();
                    final ValueHint valueHint = new ValueHint();
                    valueHint.setValue(Stream.of(RequiresModule.class.getName(), annotation.automated()).collect(Collectors.toList()));
                    valueHint.setDescription(annotation.name());
                    hint.getValues().add(valueHint);
                }

                final boolean foundRequiredProperty = StreamSupport.stream(RelaxedNames.forCamelCase(propName).spliterator(), false)
                    .map(n -> ReflectionUtils.findField(clazz, n))
                    .anyMatch(f -> f != null && f.isAnnotationPresent(RequiredProperty.class));

                if (foundRequiredProperty) {
                    final ValueHint valueHint = new ValueHint();
                    valueHint.setValue(RequiredProperty.class.getName());
                    hint.getValues().add(valueHint);
                }

                if (!hint.getValues().isEmpty()) {
                    hints.add(hint);
                }
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return hints;
    }
}
