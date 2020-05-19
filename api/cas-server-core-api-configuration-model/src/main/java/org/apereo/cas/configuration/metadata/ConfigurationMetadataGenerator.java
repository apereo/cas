package org.apereo.cas.configuration.metadata;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.Deprecation;
import org.springframework.boot.configurationmetadata.ValueHint;
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
 * private List<SomeClassProperties> list = new ArrayList<>(0)
 * }
 * The generator additionally adds hints to the metadata generated to indicate
 * required properties and modules.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class ConfigurationMetadataGenerator {

    private static final Pattern PATTERN_GENERICS = Pattern.compile(".+\\<(.+)\\>");

    private static final Pattern NESTED_TYPE_PATTERN = Pattern.compile("java\\.util\\.\\w+<(org\\.apereo\\.cas\\..+)>");

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
        if (args.length != 2) {
            throw new RuntimeException("Invalid build configuration. No command-line arguments specified");
        }
        val buildDir = args[0];
        val projectDir = args[1];
        new ConfigurationMetadataGenerator(buildDir, projectDir).execute();
    }

    private static Set<ConfigurationMetadataHint> processHints(final Collection<ConfigurationMetadataProperty> props,
                                                               final Collection<ConfigurationMetadataProperty> groups) {

        final Set<ConfigurationMetadataHint> hints = new LinkedHashSet<>(0);

        val nonDeprecatedErrors = props.stream()
            .filter(p -> p.getDeprecation() == null
                || !Deprecation.Level.ERROR.equals(p.getDeprecation().getLevel()))
            .collect(Collectors.toList());

        for (val entry : nonDeprecatedErrors) {
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

                if (clazz.isAnnotationPresent(RequiresModule.class)) {
                    val annotation = Arrays.stream(clazz.getAnnotations())
                        .filter(a -> a.annotationType().equals(RequiresModule.class))
                        .findFirst()
                        .map(RequiresModule.class::cast)
                        .get();

                    val valueHint = new ValueHint();
                    valueHint.setValue(List.of(RequiresModule.class.getName(), annotation.automated()));
                    valueHint.setDescription(annotation.name());
                    hint.getValues().add(valueHint);
                }

                val names = RelaxedPropertyNames.forCamelCase(propName);
                names.getValues().forEach(name -> {
                    val f = ReflectionUtils.findField(clazz, name);
                    if (f != null && f.isAnnotationPresent(RequiredProperty.class)) {
                        val annotation = Arrays.stream(f.getAnnotations())
                            .filter(a -> a.annotationType().equals(RequiredProperty.class))
                            .findFirst()
                            .map(RequiredProperty.class::cast)
                            .get();
                        val valueHint = new ValueHint();
                        valueHint.setValue(RequiredProperty.class.getName());
                        valueHint.setDescription(clazz.getName());
                        valueHint.setShortDescription(annotation.message());
                        hint.getValues().add(valueHint);
                    }
                });
                if (!hint.getValues().isEmpty()) {
                    hints.add(hint);
                }
            } catch (final Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(e.getMessage(), e);
                } else {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        return hints;
    }

    private static void processDeprecatedProperties(final Set<ConfigurationMetadataProperty> properties) {
        properties.stream()
            .filter(p -> p.getDeprecation() != null)
            .forEach(property -> property.getDeprecation().setLevel(Deprecation.Level.ERROR));
    }

    /**
     * Execute.
     *
     * @throws Exception the exception
     */
    public void execute() throws Exception {
        val jsonFile = new File(buildDir, "classes/java/main/META-INF/spring-configuration-metadata.json");
        if (!jsonFile.exists()) {
            throw new RuntimeException("Could not locate file " + jsonFile.getCanonicalPath());
        }
        val mapper = new ObjectMapper().findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

        final TypeReference<Map<String, Set<ConfigurationMetadataProperty>>> values = new TypeReference<>() {
        };
        final Map<String, Set> jsonMap = (Map) mapper.readValue(jsonFile, values);
        final Set<ConfigurationMetadataProperty> properties = jsonMap.get("properties");
        final Set<ConfigurationMetadataProperty> groups = jsonMap.get("groups");

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

        val hints = processHints(properties, groups);

        processNestedEnumProperties(properties, groups);
        processDeprecatedProperties(properties);

        jsonMap.put("properties", properties);
        jsonMap.put("groups", groups);
        jsonMap.put("hints", hints);
        mapper.writer(new DefaultPrettyPrinter()).writeValue(jsonFile, jsonMap);
    }

    private void processNestedEnumProperties(final Set<ConfigurationMetadataProperty> properties, final Set<ConfigurationMetadataProperty> groups) {
        val propertiesToProcess = properties.stream()
            .filter(e -> {
                val matcher = NESTED_CLASS_PATTERN.matcher(e.getType());
                return matcher.matches();
            })
            .collect(Collectors.toSet());

        for (val e : propertiesToProcess) {
            val matcher = NESTED_CLASS_PATTERN.matcher(e.getType());
            matcher.matches();

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
                            val builder = new StringBuilder(e.getDescription());
                            enumMem.getEntries()
                                .stream()
                                .filter(entry -> entry.getJavadoc().isPresent())
                                .forEach(entry -> builder.append(entry.getNameAsString())
                                    .append(':')
                                    .append(entry.getJavadoc().get().getDescription().toText())
                                    .append('.'));
                            e.setDescription(builder.toString());
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
                                    creator.createConfigurationProperty(field, e.getName());

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
