package org.apereo.cas.documentation;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.metadata.CasConfigurationMetadataCatalog;
import org.apereo.cas.metadata.CasReferenceProperty;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.shell.commands.CasShellCommand;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.ReflectionUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Getter;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link CasDocumentationApplication}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuppressWarnings("removal")
public class CasDocumentationApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasDocumentationApplication.class);

    public static void main(final String[] args) throws Exception {
        var options = new Options();

        var dt = new Option("d", "data", true, "Data directory");
        dt.setRequired(true);
        options.addOption(dt);

        var ver = new Option("v", "version", true, "Project version");
        ver.setRequired(true);
        options.addOption(ver);

        var root = new Option("r", "root", true, "Project root directory");
        root.setRequired(true);
        options.addOption(root);

        var ft = new Option("f", "filter", true, "Property filter pattern");
        ft.setRequired(false);
        options.addOption(ft);

        var act = new Option("a", "actuators", true, "Generate data for actuator endpoints");
        act.setRequired(false);
        options.addOption(act);

        var tp = new Option("tp", "thirdparty", true, "Generate data for third party");
        tp.setRequired(false);
        options.addOption(tp);

        var sp = new Option("sp", "serviceproperties", true, "Generate data for registered services properties");
        sp.setRequired(false);
        options.addOption(sp);

        var feats = new Option("ft", "features", true, "Generate data for feature toggles and descriptors");
        feats.setRequired(false);
        options.addOption(feats);

        var csh = new Option("csh", "shell", true, "Generate data for CAS command-line shell commands and groups");
        csh.setRequired(false);
        options.addOption(csh);

        var aud = new Option("aud", "audit", true, "Generate data for CAS auditable events");
        aud.setRequired(false);
        options.addOption(aud);

        var dver = new Option("ver", "versions", true, "Generate data for CAS dependency versions");
        dver.setRequired(false);
        options.addOption(dver);

        var ui = new Option("ui", "userinterface", true, "Generate data for CAS user interface and templates");
        ui.setRequired(false);
        options.addOption(ui);

        new HelpFormatter().printHelp("CAS Documentation", options);
        var cmd = new DefaultParser().parse(options, args);

        var dataDirectory = cmd.getOptionValue("data");
        var projectVersion = cmd.getOptionValue("version");
        var projectRootDirectory = cmd.getOptionValue("root");
        var propertyFilter = cmd.getOptionValue("filter", ".+");

        var results = CasConfigurationMetadataCatalog.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
                .queryFilter(property -> RegexUtils.find(propertyFilter, property.getName()))
                .build());

        var groups = new HashMap<String, Set<CasReferenceProperty>>();
        results.properties()
            .stream()
            .filter(property -> StringUtils.isNotBlank(property.getModule()))
            .peek(property -> {
                var desc = cleanDescription(property);
                property.setDescription(desc);
            })
            .forEach(property -> {
                if (groups.containsKey(property.getModule())) {
                    groups.get(property.getModule()).add(property);
                } else {
                    var values = new TreeSet<CasReferenceProperty>();
                    values.add(property);
                    groups.put(property.getModule(), values);
                }
            });

        var dataPath = new File(dataDirectory, projectVersion);
        if (dataPath.exists()) {
            FileUtils.deleteQuietly(dataPath);
        }
        dataPath.mkdirs();
        groups.forEach((key, value) -> {
            var destination = new File(dataPath, key);
            destination.mkdirs();
            var configFile = new File(destination, "config.yml");
            CasConfigurationMetadataCatalog.export(configFile, value);
        });

        var thirdparty = cmd.getOptionValue("thirdparty", "true");
        if (StringUtils.equalsIgnoreCase("true", thirdparty)) {
            exportThirdPartyConfiguration(dataPath, propertyFilter);
        }

        var registeredServicesProps = cmd.getOptionValue("serviceproperties", "true");
        if (StringUtils.equalsIgnoreCase("true", registeredServicesProps)) {
            exportRegisteredServiceProperties(dataPath);
        }

        var uiProps = cmd.getOptionValue("userinterface", "true");
        if (StringUtils.equalsIgnoreCase("true", uiProps)) {
            exportTemplateViews(projectRootDirectory, dataPath);
            exportThemeProperties(projectRootDirectory, dataPath);
        }
        
        var actuators = cmd.getOptionValue("actuators", "true");
        if (StringUtils.equalsIgnoreCase("true", actuators)) {
            exportActuatorEndpoints(dataPath);
        }

        var features = cmd.getOptionValue("features", "true");
        if (StringUtils.equalsIgnoreCase("true", features)) {
            exportFeatureToggles(dataPath);
        }

        var shell = cmd.getOptionValue("shell", "true");
        if (StringUtils.equalsIgnoreCase("true", shell)) {
            exportCommandlineShell(dataPath);
        }

        var audit = cmd.getOptionValue("audit", "true");
        if (StringUtils.equalsIgnoreCase("true", audit)) {
            exportAuditableEvents(dataPath);
        }
        var dversions = cmd.getOptionValue("versions", "true");
        if (StringUtils.equalsIgnoreCase("true", dversions)) {
            exportDependencyVersions(projectRootDirectory, dataPath);
        }
    }

    private static void exportDependencyVersions(final String rootDir, final File dataPath) throws Exception {
        var file = new File(rootDir, "docs/cas-server-documentation-processor/build/dependencies.json");
        if (!file.exists()) {
            LOGGER.error("[{}] does not exist", file.getCanonicalPath());
            return;
        }

        var parentPath = new File(dataPath, "dependency-versions");
        if (parentPath.exists()) {
            FileUtils.deleteQuietly(parentPath);
        }
        parentPath.mkdirs();
        var depFile = new File(parentPath, "config.yml");

        var dependencies = CasConfigurationMetadataCatalog.getObjectMapper().readValue(
            FileUtils.readFileToString(file, StandardCharsets.UTF_8), List.class);
        LOGGER.info("Writing [{}] dependencies found in [{}]", dependencies.size(), file.getCanonicalPath());
        CasConfigurationMetadataCatalog.export(depFile, dependencies);
    }

    private static void exportAuditableEvents(final File dataPath) {
        var parentPath = new File(dataPath, "audits");
        var properties = new ArrayList<Map<?, ?>>();
        if (parentPath.exists()) {
            FileUtils.deleteQuietly(parentPath);
        }
        if (!parentPath.mkdirs()) {
            LOGGER.debug("Unable to create directory");
        }
        Arrays.stream(AuditableActions.class.getDeclaredFields())
            .filter(it -> Modifier.isStatic(it.getModifiers()) && Modifier.isFinal(it.getModifiers()))
            .forEach(it -> {
                var event = new LinkedHashMap();
                event.put("name", it.getName());
                LOGGER.debug("Adding audit [{}]", event);
                properties.add(event);
            });
        if (!properties.isEmpty()) {
            var configFile = new File(parentPath, "config.yml");
            CasConfigurationMetadataCatalog.export(configFile, properties);
        }
    }

    private static void exportCommandlineShell(final File dataPath) {
        var parentPath = new File(dataPath, "shell");
        if (parentPath.exists()) {
            FileUtils.deleteQuietly(parentPath);
        }
        if (!parentPath.mkdirs()) {
            LOGGER.debug("Unable to create directory");
        }
        var subTypes = ReflectionUtils.findClassesWithAnnotationsInPackage(List.of(ShellComponent.class), CasShellCommand.NAMESPACE);
        var properties = new ArrayList<Map<?, ?>>();

        subTypes.forEach(clazz -> {
            LOGGER.debug("Locating shell command group for [{}]", clazz.getSimpleName());
            var group = clazz.getAnnotation(ShellCommandGroup.class);
            if (group == null) {
                LOGGER.warn("Shell command group is missing for {}", clazz.getName());
            }

            var methods = new LinkedHashMap();
            for (var method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(ShellMethod.class)) {
                    var annotInstance = method.getAnnotation(ShellMethod.class);
                    var cmd = new ShellCommand();

                    cmd.parameters = new ArrayList<Map<String, String>>();
                    var parameterAnnotations = method.getParameterAnnotations();
                    for (var parameterAnnotation : parameterAnnotations) {
                        for (var annotation : parameterAnnotation) {
                            var ann = (ShellOption) annotation;
                            cmd.parameters.add(Map.of(
                                "name", String.join(",", ann.value()),
                                "help", String.valueOf(ann.help()),
                                "optOut", String.valueOf(ann.optOut()),
                                "defaultValue", ann.defaultValue()));
                        }
                    }

                    cmd.description = annotInstance.value();
                    cmd.name = String.join(",", annotInstance.key());
                    cmd.group = group == null ? "other" : group.value();

                    LOGGER.debug("Adding shell command [{}]", cmd.name);
                    methods.put(cmd.name, cmd);
                }
            }
            properties.add(methods);
        });

        if (!properties.isEmpty()) {
            var configFile = new File(parentPath, "config.yml");
            CasConfigurationMetadataCatalog.export(configFile, properties);
        }

    }

    @Getter
    private static final class ShellCommand {
        public String name;

        public String description;

        public List parameters;

        public String group;
    }

    private static String cleanDescription(final CasReferenceProperty property) {
        var description = property.getDescription();
        var patterns = new ArrayList<String>();
        patterns.add("\\{@link (.+?)\\}");
        patterns.add("\\{@value (\\{*.+?\\}*)\\}");
        patterns.add("\\{@code (\\{*.+?\\}*)\\}");

        for (var pattern : patterns) {
            var matcher = Pattern.compile(pattern).matcher(description);
            try {
                while (matcher.find()) {
                    description = description.replaceFirst(pattern,
                        "<code>" + Matcher.quoteReplacement(matcher.group(1)) + "</code>");
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        return description;
    }

    private static void exportFeatureToggles(final File dataPath) {
        var parentPath = new File(dataPath, "features");
        if (parentPath.exists()) {
            FileUtils.deleteQuietly(parentPath);
        }
        if (!parentPath.mkdirs()) {
            LOGGER.debug("Unable to create directory");
        }

        var subTypes = ReflectionUtils.findClassesWithAnnotationsInPackage(List.of(ConditionalOnFeatureEnabled.class), CentralAuthenticationService.NAMESPACE);
        var properties = new ArrayList<Map<?, ?>>();

        var allToggleProps = new HashSet<String>();
        subTypes.forEach(clazz -> {
            var features = Arrays.stream(clazz.getAnnotationsByType(ConditionalOnFeatureEnabled.class)).collect(Collectors.toList());
            var declaredClasses = clazz.getDeclaredClasses();
            for (var declaredClass : declaredClasses) {
                var innerFeatures = Arrays.stream(declaredClass.getAnnotationsByType(ConditionalOnFeatureEnabled.class)).toList();
                features.addAll(innerFeatures);
            }

            features.forEach(feature -> {
                for (var featureDefn : feature.feature()) {
                    var propName = featureDefn.toProperty(feature.module());
                    if (!allToggleProps.contains(propName)) {
                        allToggleProps.add(propName);

                        var map = new LinkedHashMap<>();
                        map.put("type", clazz.getName());
                        map.put("feature", feature.feature());
                        if (StringUtils.isNotBlank(feature.module())) {
                            map.put("module", feature.module());
                        }
                        map.put("enabledByDefault", feature.enabledByDefault());

                        map.put("property", propName);
                        properties.add(map);
                    }
                }
            });
        });

        if (!properties.isEmpty()) {
            var configFile = new File(parentPath, "config.yml");
            CasConfigurationMetadataCatalog.export(configFile, properties);
        }
    }

    private static Pair<String, String> getEndpoint(final Class clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        
        var endpoint = (Endpoint) clazz.getAnnotation(Endpoint.class);
        if (endpoint != null) {
            return Pair.of(endpoint.id(), endpoint.annotationType().getSimpleName());
        }
        LOGGER.debug("[{}] is not an Endpoint. Checking for WebEndpoint...", clazz.getName());
        var webEndpoint = (WebEndpoint) clazz.getAnnotation(WebEndpoint.class);
        if (webEndpoint != null) {
            return Pair.of(webEndpoint.id(), webEndpoint.annotationType().getSimpleName());
        }
        LOGGER.debug("[{}] is not an Endpoint. Checking for RestControllerEndpoint...", clazz.getName());
        var restEndpoint = (RestControllerEndpoint) clazz.getAnnotation(RestControllerEndpoint.class);
        if (restEndpoint != null) {
            return Pair.of(restEndpoint.id(), restEndpoint.annotationType().getSimpleName());
        }
        LOGGER.debug("[{}] is not an Endpoint. Checking for ControllerEndpoint...", clazz.getName());
        var ctrlEndpoint = (ControllerEndpoint) clazz.getAnnotation(ControllerEndpoint.class);
        if (ctrlEndpoint != null) {
            return Pair.of(ctrlEndpoint.id(), ctrlEndpoint.annotationType().getSimpleName());
        }
        LOGGER.warn("Unable to determine endpoint from [{}]", clazz.getName());
        return null;
    }

    private static void exportActuatorEndpoints(final File dataPath) {
        var parentPath = new File(dataPath, "actuators");
        if (parentPath.exists()) {
            FileUtils.deleteQuietly(parentPath);
        }
        if (!parentPath.mkdirs()) {
            LOGGER.debug("Unable to create directory");
        }

        LOGGER.info("Checking REST endpoints...");
        var subTypes = ReflectionUtils.findClassesWithAnnotationsInPackage(List.of(RestControllerEndpoint.class), "org");
        collectRestActuators(subTypes, parentPath, RestControllerEndpoint.class);

        
        var restActuators = ReflectionUtils.findSubclassesInPackage(BaseCasRestActuatorEndpoint.class, "org.apereo.cas");
        collectRestActuators(restActuators, parentPath, Endpoint.class);

        LOGGER.info("Checking endpoints...");
        subTypes = ReflectionUtils.findClassesWithAnnotationsInPackage(List.of(Endpoint.class), "org");
        subTypes.forEach(clazz -> {
            var properties = new ArrayList<Map<?, ?>>();
            var endpoint = getEndpoint(clazz);

            if (endpoint != null) {
                LOGGER.debug("Checking endpoints (READ) for [{}]", clazz.getName());
                var methods = findAnnotatedMethods(clazz, ReadOperation.class);
                methods.forEach(Unchecked.consumer(method -> {
                    var read = method.getAnnotation(ReadOperation.class);
                    var map = new LinkedHashMap<>();
                    map.put("method", RequestMethod.GET.name());
                    map.put("path", endpoint.getKey());
                    map.put("name", endpoint.getKey());
                    map.put("endpointType", endpoint.getValue());
                    collectActuatorEndpointMethodMetadata(method, map, endpoint.getKey());
                    if (read.produces().length > 0) {
                        map.put("produces", read.produces());
                    }
                    properties.add(map);
                }));

                LOGGER.debug("Checking endpoints (WRITE) for [{}]", clazz.getName());
                methods = findAnnotatedMethods(clazz, WriteOperation.class);
                methods.forEach(Unchecked.consumer(method -> {
                    var write = method.getAnnotation(WriteOperation.class);
                    var map = new LinkedHashMap<>();
                    map.put("method", RequestMethod.POST.name());
                    map.put("path", endpoint.getKey());
                    map.put("name", endpoint.getKey());
                    map.put("endpointType", Endpoint.class.getSimpleName());
                    collectActuatorEndpointMethodMetadata(method, map, endpoint.getKey());
                    if (write.produces().length > 0) {
                        map.put("produces", write.produces());
                    }
                    properties.add(map);
                }));

                LOGGER.debug("Checking endpoints (DELETE) for [{}]", clazz.getName());
                methods = findAnnotatedMethods(clazz, DeleteOperation.class);
                methods.forEach(Unchecked.consumer(method -> {
                    var delete = method.getAnnotation(DeleteOperation.class);
                    var map = new LinkedHashMap<>();
                    map.put("method", RequestMethod.DELETE.name());
                    map.put("path", endpoint.getKey());
                    map.put("name", endpoint.getKey());
                    map.put("endpointType", endpoint.getValue());
                    collectActuatorEndpointMethodMetadata(method, map, endpoint.getKey());
                    if (delete.produces().length > 0) {
                        map.put("produces", delete.produces());
                    }
                    properties.add(map);
                }));
            }
            if (!properties.isEmpty()) {
                var destination = new File(parentPath, endpoint.getKey());
                if (!destination.mkdirs()) {
                    LOGGER.debug("Unable to create directory [{}]", destination);
                }

                var configFile = new File(destination, "config.yml");
                CasConfigurationMetadataCatalog.export(configFile, properties);
            }
        });
    }

    private static void collectRestActuators(final Collection<? extends Class> subTypes, final File parentPath, final Class annotationClazz) {
        subTypes.forEach(clazz -> {
            var properties = new ArrayList<Map<?, ?>>();
            var endpoint = clazz.getAnnotation(annotationClazz);
            var endpointId = getEndpointId(endpoint, annotationClazz);
            
            var methods = findAnnotatedMethods(clazz, GetMapping.class);
            LOGGER.debug("Checking actuator endpoint (GET) for [{}]", clazz.getName());
            methods.forEach(Unchecked.consumer(method -> {
                var get = method.getAnnotation(GetMapping.class);
                var map = new LinkedHashMap<>();
                var paths = Arrays.stream(get.path())
                    .map(path -> StringUtils.isBlank(path)
                        ? endpointId
                        : endpointId + StringUtils.prependIfMissing(path, "/"))
                    .findFirst()
                    .orElse(null);
                map.put("method", RequestMethod.GET.name());
                map.put("path", Optional.ofNullable(paths).orElse(endpointId));
                map.put("name", endpointId);
                map.put("endpointType", annotationClazz.getSimpleName());

                collectActuatorEndpointMethodMetadata(method, map, endpointId);
                if (get.produces().length > 0) {
                    map.put("produces", get.produces());
                }
                if (get.consumes().length > 0) {
                    map.put("consumes", get.consumes());
                }
                if (get.params().length > 0) {
                    map.put("parameters", get.params());
                }
                if (get.headers().length > 0) {
                    map.put("headers", get.headers());
                }
                if (get.value().length > 0) {
                    map.put("value", get.value());
                }
                properties.add(map);
            }));

            LOGGER.debug("Checking actuator endpoint (DELETE) for [{}]", clazz.getName());
            methods = findAnnotatedMethods(clazz, DeleteMapping.class);
            methods.forEach(Unchecked.consumer(method -> {
                var delete = method.getAnnotation(DeleteMapping.class);
                var map = new LinkedHashMap<>();
                var paths = Arrays.stream(delete.path())
                    .map(path -> StringUtils.isBlank(path) ? endpointId : endpointId
                        + StringUtils.prependIfMissing(path, "/"))
                    .findFirst().orElse(null);
                map.put("method", RequestMethod.DELETE.name());
                map.put("path", Optional.ofNullable(paths).orElse(endpointId));
                map.put("name", endpointId);
                map.put("endpointType", annotationClazz.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map, endpointId);
                if (delete.produces().length > 0) {
                    map.put("produces", delete.produces());
                }
                if (delete.consumes().length > 0) {
                    map.put("consumes", delete.consumes());
                }
                if (delete.params().length > 0) {
                    map.put("parameters", delete.params());
                }
                if (delete.headers().length > 0) {
                    map.put("headers", delete.headers());
                }
                if (delete.value().length > 0) {
                    map.put("value", delete.value());
                }
                properties.add(map);
            }));

            LOGGER.debug("Checking actuator endpoint (POST) for [{}]", clazz.getName());
            methods = findAnnotatedMethods(clazz, PostMapping.class);
            methods.forEach(Unchecked.consumer(method -> {
                var post = method.getAnnotation(PostMapping.class);
                var map = new LinkedHashMap<>();
                var paths = Arrays.stream(post.path())
                    .map(path -> StringUtils.isBlank(path)
                        ? endpointId
                        : endpointId + StringUtils.prependIfMissing(path, "/"))
                    .findFirst().orElse(null);
                map.put("method", RequestMethod.POST.name());
                map.put("path", Optional.ofNullable(paths).orElse(endpointId));
                map.put("name", endpointId);
                map.put("endpointType", annotationClazz.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map, endpointId);
                if (post.produces().length > 0) {
                    map.put("produces", post.produces());
                }
                if (post.consumes().length > 0) {
                    map.put("consumes", post.consumes());
                }
                if (post.params().length > 0) {
                    map.put("parameters", post.params());
                }
                if (post.headers().length > 0) {
                    map.put("headers", post.headers());
                }
                if (post.value().length > 0) {
                    map.put("value", post.value());
                }
                properties.add(map);
            }));

            LOGGER.debug("Checking actuator endpoint (PATCH) for [{}]", clazz.getName());
            methods = findAnnotatedMethods(clazz, PatchMapping.class);
            methods.forEach(Unchecked.consumer(method -> {
                var patch = method.getAnnotation(PatchMapping.class);
                var map = new LinkedHashMap<>();
                var paths = Arrays.stream(patch.path())
                    .map(path -> StringUtils.isBlank(path)
                        ? endpointId
                        : endpointId + StringUtils.prependIfMissing(path, "/"))
                    .findFirst().orElse(null);
                map.put("method", RequestMethod.PATCH.name());
                map.put("path", Optional.ofNullable(paths).orElse(endpointId));
                map.put("name", endpointId);
                map.put("endpointType", annotationClazz.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map, endpointId);
                if (patch.produces().length > 0) {
                    map.put("produces", patch.produces());
                }
                if (patch.consumes().length > 0) {
                    map.put("consumes", patch.consumes());
                }
                if (patch.params().length > 0) {
                    map.put("parameters", patch.params());
                }
                if (patch.headers().length > 0) {
                    map.put("headers", patch.headers());
                }
                if (patch.value().length > 0) {
                    map.put("value", patch.value());
                }
                properties.add(map);
            }));

            LOGGER.debug("Checking actuator endpoint (PUT) for [{}]", clazz.getName());
            methods = findAnnotatedMethods(clazz, PutMapping.class);
            methods.forEach(Unchecked.consumer(method -> {
                var put = method.getAnnotation(PutMapping.class);
                var map = new LinkedHashMap<>();
                var paths = Arrays.stream(put.path())
                    .map(path -> StringUtils.isBlank(path)
                        ? endpointId
                        : endpointId + StringUtils.prependIfMissing(path, "/"))
                    .findFirst().orElse(null);
                map.put("method", RequestMethod.PUT.name());
                map.put("path", Optional.ofNullable(paths).orElse(endpointId));
                map.put("name", endpointId);
                map.put("endpointType", annotationClazz.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map, endpointId);
                if (put.produces().length > 0) {
                    map.put("produces", put.produces());
                }
                if (put.consumes().length > 0) {
                    map.put("consumes", put.consumes());
                }
                if (put.params().length > 0) {
                    map.put("parameters", put.params());
                }
                if (put.headers().length > 0) {
                    map.put("headers", put.headers());
                }
                if (put.value().length > 0) {
                    map.put("value", put.value());
                }
                properties.add(map);
            }));

            if (!properties.isEmpty()) {
                var destination = new File(parentPath, endpointId);
                if (!destination.mkdirs()) {
                    LOGGER.debug("Unable to create directory [{}]", destination);
                }

                var configFile = new File(destination, "config.yml");
                CasConfigurationMetadataCatalog.export(configFile, properties);
            }
        });
    }

    private static String getEndpointId(final Annotation endpoint, final Class annotationClazz) {
        if (annotationClazz.equals(RestControllerEndpoint.class)) {
            return ((RestControllerEndpoint) endpoint).id();
        }
        return ((Endpoint) endpoint).id();
    }

    private static void collectActuatorEndpointMethodMetadata(final Method method,
                                                              final Map<Object, Object> map,
                                                              final String endpointId) throws Exception {
        var actuatorProperties = new Properties();
        try (var input = new ClassPathResource("actuators.properties").getInputStream()) {
            actuatorProperties.load(input);
        }

        var clazz = method.getDeclaringClass();

        var signature = method.toGenericString();
        signature = signature.substring(signature.lastIndexOf(method.getDeclaringClass().getSimpleName()));
        signature = RegExUtils.removePattern(signature, "throws.+");
        map.put("signature", signature);
        map.put("owner", clazz.getName());

        var returnType = method.getReturnType().getSimpleName();
        if (!StringUtils.equalsAnyIgnoreCase(returnType, "void")) {
            map.put("returnType", returnType);
        }
        map.put("casEndpoint", isCasEndpoint(clazz));

        if (clazz.getAnnotation(Deprecated.class) != null) {
            map.put("deprecated", true);
        }
        if (method.getAnnotation(Deprecated.class) != null) {
            map.put("deprecated", true);
        }

        var paramNames = ArrayUtils.EMPTY_STRING_ARRAY;
        try {
            paramNames = new StandardReflectionParameterNameDiscoverer().getParameterNames(method);
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage());
        }

        for (var i = 0; i < method.getParameters().length; i++) {
            var param = method.getParameters()[i];
            var selector = param.getAnnotation(Selector.class) != null;
            selector = selector || param.getAnnotation(PathVariable.class) != null;
            if (selector) {
                map.put("selector", selector);
                var path = (String) map.get("path");

                if (path.indexOf('{') == -1) {
                    var paramName = StringUtils.EMPTY;
                    if (param.getAnnotation(PathVariable.class) != null) {
                        paramName = param.getAnnotation(PathVariable.class).name();
                    }
                    if (StringUtils.isBlank(paramName) && paramNames.length > 0) {
                        paramName = paramNames[i];
                    }

                    path = StringUtils.appendIfMissing(path, "/")
                        .concat(String.format("{%s}", paramName));
                    map.put("path", path);
                }
            }
        }

        var parameters = new ArrayList<Map<?, ?>>();
        if (isCasEndpoint(clazz)) {
            var operation = Objects.requireNonNull(method.getAnnotation(Operation.class),
                () -> "Unable to locate @Operation annotation for " + method.toGenericString()
                      + " in declaring class " + clazz.getName());
            if (!map.containsKey("deprecated") && operation.deprecated()) {
                map.put("deprecated", true);
            }
            map.put("summary", StringUtils.appendIfMissing(operation.summary(), "."));
            var paramCount = Arrays.stream(method.getParameterTypes())
                .filter(type -> !type.equals(HttpServletRequest.class) && !type.equals(HttpServletResponse.class)).count();
            
            if (operation.parameters().length == 0 && paramCount > 0) {
                for (var i = 0; i < method.getParameterTypes().length; i++) {
                    var parameter = method.getParameters()[i];

                    var pathAnn = parameter.getAnnotation(PathVariable.class);
                    if (pathAnn != null) {
                        var paramData = new LinkedHashMap<String, Object>();
                        paramData.put("name", "path-variable" + RandomUtils.randomNumeric(4));
                        paramData.put("description", "Path variable selector");
                        paramData.put("required", pathAnn.required());
                        paramData.put("defaultValue", pathAnn.value());
                        paramData.put("selector", true);
                        parameters.add(paramData);
                    }
                    var requestParamAnn = parameter.getAnnotation(RequestParam.class);
                    if (requestParamAnn != null) {
                        var paramData = new LinkedHashMap<String, Object>();
                        var name = StringUtils.defaultIfBlank(requestParamAnn.name(), requestParamAnn.value());
                        name = StringUtils.defaultIfBlank(name, parameter.getName());
                        paramData.put("name", name);
                        paramData.put("description", "Request query parameter");
                        paramData.put("required", requestParamAnn.required());
                        paramData.put("defaultValue", requestParamAnn.defaultValue());
                        paramData.put("query", true);
                        parameters.add(paramData);
                    }
                    var selectorAnn = parameter.getAnnotation(Selector.class);
                    if (selectorAnn != null) {
                        var paramData = new LinkedHashMap<String, Object>();
                        paramData.put("name", RandomUtils.randomNumeric(4));
                        paramData.put("description", "Path variable selector");
                        paramData.put("required", true);
                        paramData.put("selector", true);
                        parameters.add(paramData);
                    }
                }

                if (parameters.isEmpty()) {
                    throw new RuntimeException("Unable to locate @Parameter annotation for " + method.toGenericString()
                                               + " in declaring class " + clazz.getName());
                }
            }

            for (var i = 0; i < operation.parameters().length; i++) {
                var parameter = operation.parameters()[i];
                var paramData = new LinkedHashMap<String, Object>();
                paramData.put("name", parameter.name());
                if (StringUtils.isNotBlank(parameter.description())) {
                    paramData.put("description", parameter.description());
                } else {
                    throw new RuntimeException("No description found for parameter %s in %s".formatted(parameter.name(), clazz.getName()));
                }
                paramData.put("required", parameter.required());

                if (Objects.requireNonNull(parameter.in()) == ParameterIn.PATH) {
                    paramData.put("selector", true);
                } else {
                    paramData.put("query", true);
                }
                if (parameter.schema() != null && StringUtils.isNotBlank(parameter.schema().type())) {
                    paramData.put("type", parameter.schema().type());
                }
                parameters.add(paramData);
            }
        } else {
            var name = String.format("actuator.endpoint.%s.description", endpointId);
            var summary = actuatorProperties.getProperty(name);
            if (StringUtils.isBlank(summary)) {
                throw new RuntimeException("Unable to locate undocumented endpoint summary for: " + endpointId + " found in " + clazz.getName());
            }
            map.put("summary", StringUtils.appendIfMissing(summary, "."));
        }

        if (!parameters.isEmpty()) {
            map.put("parameters", parameters);
        }

    }

    private static boolean isCasEndpoint(final Class<?> clazz) {
        return clazz.getPackageName().startsWith(CentralAuthenticationService.NAMESPACE);
    }

    private static void exportThemeProperties(final String projectRootDirectory, final File dataPath) throws Exception {
        var themeProps = new File(dataPath, "theme-properties");
        if (themeProps.exists()) {
            FileUtils.deleteQuietly(themeProps);
        }
        themeProps.mkdirs();
        var uiFile = new File(themeProps, "config.yml");
        var properties = new ArrayList<Map<?, ?>>();

        var root = new File(projectRootDirectory, "support/cas-server-support-thymeleaf");
        var file = new File(root, "src/main/resources/cas-theme-default.properties");
        var lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
        var it = lines.iterator();
        var comments = new StringBuilder();
        var pattern = Pattern.compile("#*\\s*(cas.+)=(\\S+)*");

        while (it.hasNext()) {
            var ln = it.next();
            var matcher = pattern.matcher(ln);
            if (matcher.find()) {
                var prop = matcher.group(1);
                var value = StringUtils.defaultString(matcher.group(2));
                var comm = comments.toString().stripLeading().trim();

                var map = new LinkedHashMap<String, Object>();
                map.put("name", prop);
                map.put("value", value);
                map.put("description", comm);
                properties.add(map);

                comments = new StringBuilder();
            } else {
                ln = ln.replace("# ", " ");
                comments.append(ln);
            }
            it.remove();
        }
        CasConfigurationMetadataCatalog.export(uiFile, properties);
    }

    private static void exportTemplateViews(final String projectRootDirectory, final File dataPath) {
        var serviceProps = new File(dataPath, "userinterface-templates");
        if (serviceProps.exists()) {
            FileUtils.deleteQuietly(serviceProps);
        }
        serviceProps.mkdirs();
        var uiFile = new File(serviceProps, "config.yml");
        var properties = new ArrayList<Map<?, ?>>();

        var root = new File(projectRootDirectory, "support/cas-server-support-thymeleaf");
        var parent = new File(root, "src/main/resources/templates");

        var files = FileUtils.listFiles(parent, new String[]{"html", "mustache"}, true);
        files
            .stream()
            .sorted()
            .forEach(file -> {
                var map = new LinkedHashMap<String, Object>();
                var path = StringUtils.remove(file.getAbsolutePath(), root.getAbsolutePath());
                map.put("name", path);
                properties.add(map);
            });
        CasConfigurationMetadataCatalog.export(uiFile, properties);
    }

    private static void exportRegisteredServiceProperties(final File dataPath) {
        var serviceProps = new File(dataPath, "registered-service-properties");
        if (serviceProps.exists()) {
            FileUtils.deleteQuietly(serviceProps);
        }
        serviceProps.mkdirs();
        var servicePropsFile = new File(serviceProps, "config.yml");
        var properties = new ArrayList<Map<?, ?>>();
        for (var property : RegisteredServiceProperty.RegisteredServiceProperties.values()) {
            var map = new LinkedHashMap<String, Object>();
            map.put("name", property.getPropertyName());
            map.put("defaultValue", property.getDefaultValue());
            map.put("type", property.getType().name());
            map.put("group", property.getGroup().name());
            map.put("description", property.getDescription());
            properties.add(map);
        }
        CasConfigurationMetadataCatalog.export(servicePropsFile, properties);
    }

    private static void exportThirdPartyConfiguration(final File dataPath, final String propertyFilter) {
        var results = CasConfigurationMetadataCatalog.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.THIRD_PARTY)
                .queryFilter(property -> RegexUtils.find(propertyFilter, property.getName()))
                .build());
        results.properties().forEach(property -> {
            var desc = cleanDescription(property);
            property.setDescription(desc);
        });

        var destination = new File(dataPath, "third-party");
        if (destination.exists()) {
            FileUtils.deleteQuietly(destination);
        }
        destination.mkdirs();
        var configFile = new File(destination, "config.yml");
        CasConfigurationMetadataCatalog.export(configFile, results.properties());
    }

    private static List<Method> findAnnotatedMethods(final Class<?> clazz,
                                                     final Class<? extends Annotation> annotationClass) {
        try {
            var methods = clazz.getMethods();
            return Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(annotationClass))
                .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            LOGGER.info("Failed to locate annotated methods: {}", throwable.getMessage());
        }
        return new ArrayList<>();
    }
}
