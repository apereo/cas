package org.apereo.cas.configuration.metadata;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.PropertyOwner;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RelaxedPropertyNames;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.Deprecation;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.ReflectionUtils;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
 * private var list = new ArrayList<>()
 * }
 * The generator additionally adds hints to the metadata generated to indicate
 * required properties and modules.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class ConfigurationMetadataGenerator {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setDefaultPrettyPrinter(new DefaultPrettyPrinter())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .findAndRegisterModules();

    private static final Pattern PATTERN_GENERICS = Pattern.compile(".+\\<(.+)\\>");

    private static final Pattern NESTED_TYPE_PATTERN1 = Pattern.compile("java\\.util\\.\\w+<(org\\.apereo\\.cas\\..+)>");

    private static final Pattern NESTED_TYPE_PATTERN2 = Pattern.compile("java\\.util\\.(List|Set)<(.+Properties)>");

    private static final Pattern MAP_TYPE_STRING_KEY_OBJECT_PATTERN =
        Pattern.compile("java\\.util\\.Map<java\\.lang\\.String,\\s*(org\\.apereo\\.cas\\..+)>");

    private static final Pattern NESTED_CLASS_PATTERN = Pattern.compile("(.+)\\$(\\w+)");

    private final File inputSpringConfigurationMetadata;
    private final File outputSpringConfigurationMetadata;
    private final File projectDirectory;

    public ConfigurationMetadataGenerator(final File inputSpringConfigurationMetadata,
                                          final File outputSpringConfigurationMetadata) {
        this.inputSpringConfigurationMetadata = inputSpringConfigurationMetadata;
        this.outputSpringConfigurationMetadata = outputSpringConfigurationMetadata;
        this.projectDirectory = inputSpringConfigurationMetadata.getParentFile().getParentFile()
            .getParentFile().getParentFile().getParentFile().getParentFile();
    }

    /**
     * Main.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: ConfigurationMetadataGenerator <input-file> <output-file>");
        }
        val inputSpringConfigurationMetadata = new File(args[0]);
        val outputSpringConfigurationMetadata = new File(args[1]);
        LOGGER.info("Input configuration file: [{}], Output configuration file [{}]", inputSpringConfigurationMetadata, outputSpringConfigurationMetadata);
        val generator = new ConfigurationMetadataGenerator(inputSpringConfigurationMetadata, outputSpringConfigurationMetadata);
        generator.adjustConfigurationMetadata();
    }

    protected void adjustConfigurationMetadata() throws Exception {
        if (!inputSpringConfigurationMetadata.exists()) {
            throw new RuntimeException("Could not locate file " + inputSpringConfigurationMetadata.getCanonicalPath());
        }
        LOGGER.info("Project directory [{}]", projectDirectory);
        final Map<String, Object> jsonMap = MAPPER.readValue(inputSpringConfigurationMetadata, new TypeReference<>() {});
        final List<ConfigurationMetadataProperty> properties = MAPPER.convertValue(jsonMap.get("properties"), new TypeReference<>() {});
        Objects.requireNonNull(properties);
        final List<ConfigurationMetadataProperty> groups = MAPPER.convertValue(jsonMap.get("groups"), new TypeReference<>() {});
        Objects.requireNonNull(groups);
        
        processMappableProperties(properties, groups);
        processNestedTypes(properties, groups);

        val hints = processHints(properties, groups);
        processNestedEnumProperties(properties, groups);
        processDeprecatedProperties(properties);
        processTopLevelEnumTypes(properties);

        removeNestedConfigurationPropertyGroups(properties, groups);

        jsonMap.put("properties", properties.parallelStream().sorted(Comparator.comparing(ConfigurationMetadataProperty::getName)).collect(Collectors.toCollection(LinkedHashSet::new)));
        jsonMap.put("groups", groups.parallelStream().sorted(Comparator.comparing(ConfigurationMetadataProperty::getName)).collect(Collectors.toCollection(LinkedHashSet::new)));
        jsonMap.put("hints", hints.parallelStream().sorted(Comparator.comparing(ConfigurationMetadataHint::getName)).collect(Collectors.toCollection(LinkedHashSet::new)));
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(outputSpringConfigurationMetadata, jsonMap);
    }

    protected static Set<ConfigurationMetadataHint> processHints(final Collection<ConfigurationMetadataProperty> props,
                                                                 final Collection<ConfigurationMetadataProperty> groups) {

        var hints = new LinkedHashSet<ConfigurationMetadataHint>(0);
        val allValidProps = props.stream()
            .filter(p -> p.getDeprecation() == null
                || Deprecation.Level.ERROR != p.getDeprecation().getLevel()).toList();

        for (val entry : allValidProps) {
            val propName = StringUtils.substringAfterLast(entry.getName(), ".");
            val groupName = StringUtils.substringBeforeLast(entry.getName(), ".");
            groups
                .stream()
                .filter(g -> g.getName().equalsIgnoreCase(groupName))
                .findFirst()
                .ifPresent(grp -> {
                    try {
                        val matcher = PATTERN_GENERICS.matcher(grp.getType());
                        val className = matcher.find() ? matcher.group(1) : grp.getType();
                        val clazz = ClassUtils.getClass(className);

                        val hint = new ConfigurationMetadataHint();
                        hint.setName(entry.getName());

                        val annotation = Arrays.stream(clazz.getAnnotations())
                            .filter(a -> a.annotationType().equals(RequiresModule.class))
                            .findFirst()
                            .map(RequiresModule.class::cast)
                            .orElseThrow(() -> new RuntimeException(clazz.getCanonicalName() + " is missing @RequiresModule"));

                        val valueHint = new ValueHint();

                        val hintsMap = new TreeMap<>();
                        hintsMap.put("module", annotation.name());
                        hintsMap.put("automated", annotation.automated());
                        valueHint.setValue(toJson(hintsMap));
                        valueHint.setDescription(RequiresModule.class.getName());
                        hint.getValues().add(valueHint);

                        val grpHint = new ValueHint();
                        grpHint.setValue(toJson(Map.of("owner", clazz.getCanonicalName())));
                        grpHint.setDescription(PropertyOwner.class.getName());
                        hint.getValues().add(grpHint);

                        val names = RelaxedPropertyNames.forCamelCase(propName);
                        names.getValues().forEach(Unchecked.consumer(name -> {
                            val f = ReflectionUtils.findField(clazz, name);
                            if (f != null && f.isAnnotationPresent(RequiredProperty.class)) {
                                val propertyHint = new ValueHint();
                                propertyHint.setValue(toJson(Map.of("owner", clazz.getName())));
                                propertyHint.setDescription(RequiredProperty.class.getName());
                                hint.getValues().add(propertyHint);
                            }
                            if (f != null && f.isAnnotationPresent(DurationCapable.class)) {
                                val propertyHint = new ValueHint();
                                propertyHint.setDescription(DurationCapable.class.getName());
                                propertyHint.setValue(toJson(List.of(DurationCapable.class.getName())));
                                hint.getValues().add(propertyHint);
                            }

                            if (f != null && f.isAnnotationPresent(ExpressionLanguageCapable.class)) {
                                val propertyHint = new ValueHint();
                                propertyHint.setDescription(ExpressionLanguageCapable.class.getName());
                                propertyHint.setValue(toJson(List.of(ExpressionLanguageCapable.class.getName())));
                                hint.getValues().add(propertyHint);
                            }

                            if (f != null && f.isAnnotationPresent(RegularExpressionCapable.class)) {
                                val propertyHint = new ValueHint();
                                propertyHint.setDescription(RegularExpressionCapable.class.getName());
                                propertyHint.setValue(toJson(List.of(RegularExpressionCapable.class.getName())));
                                hint.getValues().add(propertyHint);
                            }
                        }));

                        if (!hint.getValues().isEmpty()) {
                            hints.add(hint);
                        }
                    } catch (final Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
        }
        return hints;
    }

    protected static void processDeprecatedProperties(final List<ConfigurationMetadataProperty> properties) {
        properties.stream()
            .filter(p -> p.getDeprecation() != null)
            .forEach(property -> property.getDeprecation().setLevel(Deprecation.Level.ERROR));
    }

    protected static String toJson(final Object value) throws Exception {
        return MAPPER.writeValueAsString(value);
    }

    protected static void removeNestedConfigurationPropertyGroups(final List<ConfigurationMetadataProperty> properties,
                                                                  final List<ConfigurationMetadataProperty> groups) {
        var it = properties.iterator();
        while (it.hasNext()) {
            var entry = it.next();
            try {
                val propName = StringUtils.substringAfterLast(entry.getName(), ".");
                val groupName = StringUtils.substringBeforeLast(entry.getName(), ".");
                val res = groups
                    .stream()
                    .filter(g -> g.getName().equalsIgnoreCase(groupName))
                    .findFirst();
                if (res.isPresent()) {
                    var grp = res.get();
                    val className = grp.getType();
                    val clazz = ClassUtils.getClass(className);

                    val names = RelaxedPropertyNames.forCamelCase(propName);
                    names.getValues().forEach(Unchecked.consumer(name -> {
                        val f = ReflectionUtils.findField(clazz, name);
                        if (f != null && f.isAnnotationPresent(NestedConfigurationProperty.class)) {
                            it.remove();
                        }
                    }));
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void processMappableProperties(final List<ConfigurationMetadataProperty> properties,
                                             final List<ConfigurationMetadataProperty> groups) {
        val collectedProps = new HashSet<ConfigurationMetadataProperty>(0);
        val collectedGroups = new HashSet<ConfigurationMetadataProperty>(0);

        properties.forEach(property -> {
            try {
                val matcher = MAP_TYPE_STRING_KEY_OBJECT_PATTERN.matcher(property.getType());
                if (matcher.matches()) {
                    val valueType = matcher.group(1);

                    val typePath = ConfigurationMetadataClassSourceLocator.buildTypeSourcePath(projectDirectory.getCanonicalPath(), valueType);
                    val typeFile = new File(typePath);

                    if (typeFile.exists()) {
                        val parser = new ConfigurationMetadataUnitParser(projectDirectory.getCanonicalPath());
                        property.setName(property.getName().concat(".[key]"));
                        property.setId(property.getName());
                        parser.parseCompilationUnit(collectedProps, collectedGroups, property, typePath,
                            valueType, false);
                    } else {
                        throw new RuntimeException(typePath + " does not exist");
                    }
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
        properties.addAll(collectedProps);
        groups.addAll(collectedGroups);
    }

    protected void processNestedEnumProperties(final List<ConfigurationMetadataProperty> properties,
                                               final List<ConfigurationMetadataProperty> groups) throws Exception {
        val propertiesToProcess = properties.stream()
            .filter(e -> {
                val matcher = NESTED_CLASS_PATTERN.matcher(e.getType());
                return matcher.matches();
            })
            .collect(Collectors.toSet());

        for (val prop : propertiesToProcess) {

            val matcher = NESTED_CLASS_PATTERN.matcher(prop.getType());
            if (!matcher.matches()) {
                throw new RuntimeException("Unable to find a match for " + prop.getType());
            }

            val parent = matcher.group(1);
            val innerType = matcher.group(2);
            var typePath = ConfigurationMetadataClassSourceLocator.buildTypeSourcePath(projectDirectory.getCanonicalPath(), parent);

            try {
                TypeDeclaration<?> primaryType = null;
                if (typePath.contains("$")) {
                    val innerClass = StringUtils.substringBetween(typePath, "$", ".");
                    typePath = StringUtils.remove(typePath, '$' + innerClass);
                    val cu = StaticJavaParser.parse(new File(typePath));
                    for (val type : cu.getTypes()) {
                        for (val member : type.getMembers()) {
                            if (member.isClassOrInterfaceDeclaration()) {
                                val name = member.asClassOrInterfaceDeclaration().getNameAsString();
                                if (name.equals(innerClass)) {
                                    primaryType = member.asClassOrInterfaceDeclaration();
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    val cu = StaticJavaParser.parse(new File(typePath));
                    primaryType = cu.getPrimaryType().orElseThrow();
                }

                Objects.requireNonNull(primaryType).getMembers()
                    .stream()
                    .peek(member -> {
                        if (member.isFieldDeclaration()) {
                            var fieldDecl = member.asFieldDeclaration();
                            var variable = fieldDecl.getVariable(0);

                            if (variable.getInitializer().isPresent()) {
                                var beginIndex = prop.getName().lastIndexOf('.');
                                var propShortName = beginIndex != -1 ? prop.getName().substring(beginIndex + 1) : prop.getName();
                                var names = RelaxedPropertyNames.forCamelCase(variable.getNameAsString()).getValues();
                                if (names.contains(propShortName)) {
                                    variable.getInitializer().ifPresent(expression -> {
                                        val value = switch (expression) {
                                            case final LiteralStringValueExpr expr -> expr.getValue();
                                            case final BooleanLiteralExpr expr -> expr.getValue();
                                            case final FieldAccessExpr expr -> expr.getNameAsString();
                                            default -> null;
                                        };
                                        prop.setDefaultValue(value);
                                    });
                                }
                            }
                        }
                    })
                    .filter(member -> {
                        if (member.isEnumDeclaration()) {
                            val enumMem = member.asEnumDeclaration();
                            return enumMem.getNameAsString().equals(innerType);
                        }
                        if (member.isClassOrInterfaceDeclaration()) {
                            val typeName = member.asClassOrInterfaceDeclaration();
                            return typeName.getNameAsString().equals(innerType);
                        }
                        return false;
                    })
                    .forEach(member -> {
                        if (member.isEnumDeclaration()) {
                            val enumMem = member.asEnumDeclaration();
                            val builder = ConfigurationMetadataPropertyCreator.collectJavadocsEnumFields(prop, enumMem);
                            prop.setDescription(builder.toString());
                        }
                        if (member.isClassOrInterfaceDeclaration()) {
                            val typeName = member.asClassOrInterfaceDeclaration();
                            typeName.getFields()
                                .stream()
                                .filter(field -> !field.isStatic())
                                .forEach(field -> {
                                    val resultProps = new HashSet<ConfigurationMetadataProperty>();
                                    val resultGroups = new HashSet<ConfigurationMetadataProperty>();

                                    val creator = new ConfigurationMetadataPropertyCreator(
                                        false, resultProps, resultGroups, parent);
                                    creator.createConfigurationProperty(field, prop.getName());

                                    groups.addAll(resultGroups);
                                    properties.addAll(resultProps);
                                });
                        }
                    });
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    protected void processTopLevelEnumTypes(final List<ConfigurationMetadataProperty> properties) throws Exception {
        for (val property : properties) {
            var typePath = ConfigurationMetadataClassSourceLocator.buildTypeSourcePath(projectDirectory.getCanonicalPath(), property.getType());
            var typeFile = new File(typePath);
            if (!typeFile.exists() && !property.getType().contains(".")) {
                val clazz = ConfigurationMetadataClassSourceLocator.findClassBySimpleNameInPackage(property.getType(), "org.apereo.cas");
                if (clazz.isPresent()) {
                    typePath = ConfigurationMetadataClassSourceLocator.buildTypeSourcePath(projectDirectory.getCanonicalPath(), clazz.get().getName());
                    typeFile = new File(typePath);
                }
            }

            if (typeFile.exists()) {
                val cu = StaticJavaParser.parse(new File(typePath));
                for (val type : cu.getTypes()) {
                    if (type.isEnumDeclaration()) {
                        val enumMem = type.asEnumDeclaration();
                        val builder = ConfigurationMetadataPropertyCreator.collectJavadocsEnumFields(property, enumMem);
                        property.setDescription(builder.toString());
                    }
                }
            }
        }
    }

    protected void processNestedTypes(final List<ConfigurationMetadataProperty> properties, final List<ConfigurationMetadataProperty> groups) {
        val collectedProps = new HashSet<ConfigurationMetadataProperty>(0);
        val collectedGroups = new HashSet<ConfigurationMetadataProperty>(0);
        LOGGER.trace("Processing nested configuration types...");
        properties
            .forEach(Unchecked.consumer(p -> {
                var indexBrackets = false;
                var typeName = StringUtils.EMPTY;

                if (NESTED_TYPE_PATTERN1.matcher(p.getType()).matches()) {
                    val matcher = NESTED_TYPE_PATTERN1.matcher(p.getType());
                    indexBrackets = matcher.matches();
                    typeName = matcher.group(1);
                } else if (NESTED_TYPE_PATTERN2.matcher(p.getType()).matches()) {
                    val matcher = NESTED_TYPE_PATTERN2.matcher(p.getType());
                    indexBrackets = matcher.matches();
                    typeName = matcher.group(2);
                    val result = ConfigurationMetadataClassSourceLocator.findClassBySimpleNameInPackage(typeName, "org.apereo.cas");
                    if (result.isPresent()) {
                        typeName = result.get().getName();
                    }

                }

                if (!typeName.isEmpty()) {
                    val typePath = ConfigurationMetadataClassSourceLocator.buildTypeSourcePath(projectDirectory.getCanonicalPath(), typeName);
                    LOGGER.debug("Matched Type [{}], Property [{}], Type: [{}], Path [{}]", typeName, p.getName(), p.getType(), typePath);
                    val parser = new ConfigurationMetadataUnitParser(projectDirectory.getCanonicalPath());
                    parser.parseCompilationUnit(collectedProps, collectedGroups, p, typePath, typeName, indexBrackets);
                }
            }));

        properties.addAll(collectedProps);
        groups.addAll(collectedGroups);
    }
}
