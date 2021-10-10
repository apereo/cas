package org.apereo.cas.configuration.metadata;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.PropertyOwner;
import org.apereo.cas.configuration.support.RelaxedPropertyNames;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import lombok.RequiredArgsConstructor;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
 * private var list = new ArrayList<>(0)
 * }
 * The generator additionally adds hints to the metadata generated to indicate
 * required properties and modules.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class ConfigurationMetadataGenerator {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setDefaultPrettyPrinter(new MinimalPrettyPrinter())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .findAndRegisterModules();

    private static final Pattern PATTERN_GENERICS = Pattern.compile(".+\\<(.+)\\>");

    private static final Pattern NESTED_TYPE_PATTERN = Pattern.compile("java\\.util\\.\\w+<(org\\.apereo\\.cas\\..+)>");

    private static final Pattern MAP_TYPE_STRING_KEY_OBJECT_PATTERN =
        Pattern.compile("java\\.util\\.Map<java\\.lang\\.String,\\s*(org\\.apereo\\.cas\\..+)>");

    private static final Pattern NESTED_CLASS_PATTERN = Pattern.compile("(.+)\\$(\\w+)");

    private final String buildDir;

    private final String sourcePath;

    /**
     * Main.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main(final String[] args) throws Exception {
        val buildDir = args[0];
        val projectDir = args[1];
        val generator = new ConfigurationMetadataGenerator(buildDir, projectDir);
        generator.adjustConfigurationMetadata();
    }

    private static Set<ConfigurationMetadataHint> processHints(final Collection<ConfigurationMetadataProperty> props,
                                                               final Collection<ConfigurationMetadataProperty> groups) {

        var hints = new LinkedHashSet<ConfigurationMetadataHint>(0);
        val allValidProps = props.stream()
            .filter(p -> p.getDeprecation() == null
                || !Deprecation.Level.ERROR.equals(p.getDeprecation().getLevel()))
            .collect(Collectors.toList());

        for (val entry : allValidProps) {
            try {
                val propName = StringUtils.substringAfterLast(entry.getName(), ".");
                val groupName = StringUtils.substringBeforeLast(entry.getName(), ".");
                val grp = groups
                    .stream()
                    .filter(g -> g.getName().equalsIgnoreCase(groupName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Cant locate group " + groupName));

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
                valueHint.setValue(toJson(Map.of("module", annotation.name(), "automated", annotation.automated())));
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
                }));

                if (!hint.getValues().isEmpty()) {
                    hints.add(hint);
                }
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return hints;
    }

    private static void processDeprecatedProperties(final Set<ConfigurationMetadataProperty> properties) {
        properties.stream()
            .filter(p -> p.getDeprecation() != null)
            .forEach(property -> property.getDeprecation().setLevel(Deprecation.Level.ERROR));
    }

    private static String toJson(final Object value) throws Exception {
        return MAPPER.writeValueAsString(value);
    }

    private static void removeNestedConfigurationPropertyGroups(final Set<ConfigurationMetadataProperty> properties,
                                                                final Set<ConfigurationMetadataProperty> groups) {
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

    /**
     * Execute.
     *
     * @throws Exception the exception
     */
    private void adjustConfigurationMetadata() throws Exception {
        val jsonFile = new File(buildDir, "classes/java/main/META-INF/spring-configuration-metadata.json");
        if (!jsonFile.exists()) {
            throw new RuntimeException("Could not locate file " + jsonFile.getCanonicalPath());
        }
        val values = new TypeReference<Map<String, Set<ConfigurationMetadataProperty>>>() {
        };
        final Map<String, Set> jsonMap = (Map) MAPPER.readValue(jsonFile, values);
        final Set<ConfigurationMetadataProperty> properties = jsonMap.get("properties");
        final Set<ConfigurationMetadataProperty> groups = jsonMap.get("groups");

        processMappableProperties(properties, groups);
        processNestedTypes(properties, groups);

        val hints = processHints(properties, groups);
        processNestedEnumProperties(properties, groups);
        processDeprecatedProperties(properties);

        removeNestedConfigurationPropertyGroups(properties, groups);

        jsonMap.put("properties", properties);
        jsonMap.put("groups", groups);
        jsonMap.put("hints", hints);
        MAPPER.writeValue(jsonFile, jsonMap);
        MAPPER.writeValue(new File(buildDir, jsonFile.getName()), jsonMap);
    }

    private void processNestedTypes(final Set<ConfigurationMetadataProperty> properties, final Set<ConfigurationMetadataProperty> groups) {
        val collectedProps = new HashSet<ConfigurationMetadataProperty>(0);
        val collectedGroups = new HashSet<ConfigurationMetadataProperty>(0);

        properties.stream()
            .filter(p -> NESTED_TYPE_PATTERN.matcher(p.getType()).matches())
            .forEach(Unchecked.consumer(p -> {
                val matcher = NESTED_TYPE_PATTERN.matcher(p.getType());
                val indexBrackets = matcher.matches();
                val typeName = matcher.group(1);
                val typePath = ConfigurationMetadataClassSourceLocator.buildTypeSourcePath(this.sourcePath, typeName);

                val parser = new ConfigurationMetadataUnitParser(this.sourcePath);
                parser.parseCompilationUnit(collectedProps, collectedGroups, p, typePath, typeName, indexBrackets);
            }));

        properties.addAll(collectedProps);
        groups.addAll(collectedGroups);
    }

    private void processMappableProperties(final Set<ConfigurationMetadataProperty> properties,
                                           final Set<ConfigurationMetadataProperty> groups) {
        val collectedProps = new HashSet<ConfigurationMetadataProperty>(0);
        val collectedGroups = new HashSet<ConfigurationMetadataProperty>(0);

        properties.forEach(property -> {
            val matcher = MAP_TYPE_STRING_KEY_OBJECT_PATTERN.matcher(property.getType());
            if (matcher.matches()) {
                val valueType = matcher.group(1);

                val typePath = ConfigurationMetadataClassSourceLocator.buildTypeSourcePath(this.sourcePath, valueType);
                val typeFile = new File(typePath);

                if (typeFile.exists()) {
                    val parser = new ConfigurationMetadataUnitParser(this.sourcePath);
                    property.setName(property.getName().concat(".[key]"));
                    property.setId(property.getName());
                    parser.parseCompilationUnit(collectedProps, collectedGroups, property, typePath,
                        valueType, false);
                } else {
                    throw new RuntimeException(typePath + " does not exist");
                }
            }
        });
        properties.addAll(collectedProps);
        groups.addAll(collectedGroups);
    }

    private void processNestedEnumProperties(final Set<ConfigurationMetadataProperty> properties,
                                             final Set<ConfigurationMetadataProperty> groups) {
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
            var typePath = ConfigurationMetadataClassSourceLocator.buildTypeSourcePath(this.sourcePath, parent);

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
                    primaryType = cu.getPrimaryType().get();
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
                                    variable.getInitializer().ifPresent(exp -> {
                                        var value = (Object) null;
                                        if (exp instanceof LiteralStringValueExpr) {
                                            value = ((LiteralStringValueExpr) exp).getValue();
                                        } else if (exp instanceof BooleanLiteralExpr) {
                                            value = ((BooleanLiteralExpr) exp).getValue();
                                        } else if (exp instanceof FieldAccessExpr) {
                                            value = ((FieldAccessExpr) exp).getNameAsString();
                                        }
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
}
