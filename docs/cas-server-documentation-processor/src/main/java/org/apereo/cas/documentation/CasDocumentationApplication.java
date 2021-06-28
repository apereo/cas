package org.apereo.cas.documentation;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.metadata.CasConfigurationMetadataCatalog;
import org.apereo.cas.metadata.CasReferenceProperty;
import org.apereo.cas.metadata.ConfigurationMetadataCatalogQuery;
import org.apereo.cas.services.RegisteredServiceProperty;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link CasDocumentationApplication}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class CasDocumentationApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasDocumentationApplication.class);

    public static void main(final String[] args) throws Exception {
        var results = CasConfigurationMetadataCatalog.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
                .build());

        var groups = new HashMap<String, Set<CasReferenceProperty>>();
        results.properties()
            .stream()
            .filter(property -> StringUtils.isNotBlank(property.getModule()))
            .forEach(property -> {
                if (groups.containsKey(property.getModule())) {
                    groups.get(property.getModule()).add(property);
                } else {
                    var values = new TreeSet<CasReferenceProperty>();
                    values.add(property);
                    groups.put(property.getModule(), values);
                }
            });

        if (args.length != 3) {
            System.err.println("Error: data directory, project version and project root directory are unspecified");
            System.exit(1);
        }
        var dataDirectory = args[0];
        var projectVersion = args[1];
        var projectRootDirectory = args[2];

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

        exportThirdPartyConfiguration(dataPath);
        exportRegisteredServiceProperties(dataPath);
        exportTemplateViews(projectRootDirectory, dataPath);
        exportThemeProperties(projectRootDirectory, dataPath);
        exportActuatorEndpoints(dataPath);
    }

    private static void exportActuatorEndpoints(final File dataPath) {
        var parentPath = new File(dataPath, "actuators");
        if (parentPath.exists()) {
            FileUtils.deleteQuietly(parentPath);
        }
        parentPath.mkdirs();

        var urls = new ArrayList<>(ClasspathHelper.forPackage(CentralAuthenticationService.NAMESPACE));
        urls.addAll(ClasspathHelper.forPackage("org.springframework.boot"));
        urls.addAll(ClasspathHelper.forPackage("org.springframework.cloud"));
        urls.addAll(ClasspathHelper.forPackage("org.springframework.data"));
        var reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(urls)
            .setScanners(new TypeAnnotationsScanner()));

        var subTypes = reflections.getTypesAnnotatedWith(RestControllerEndpoint.class, true);
        subTypes.forEach(clazz -> {
            var properties = new ArrayList<Map<?, ?>>();
            var endpoint = clazz.getAnnotation(RestControllerEndpoint.class);

            var methods = findAnnotatedMethods(clazz, GetMapping.class);
            methods.forEach(method -> {
                var get = method.getAnnotation(GetMapping.class);
                var map = new LinkedHashMap<>();
                var paths = Arrays.stream(get.path())
                    .map(path -> StringUtils.isBlank(path) ? endpoint.id() : endpoint.id()
                        + StringUtils.prependIfMissing(path, "/")).collect(Collectors.toSet());
                map.put("method", RequestMethod.GET.name());
                map.put("path", paths.isEmpty() ? endpoint.id() : paths);
                map.put("name", endpoint.id());
                map.put("endpointType", RestControllerEndpoint.class.getSimpleName());

                collectActuatorEndpointMethodMetadata(method, map);
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
            });

            methods = findAnnotatedMethods(clazz, DeleteMapping.class);
            methods.forEach(method -> {
                var delete = method.getAnnotation(DeleteMapping.class);
                var map = new LinkedHashMap<>();
                var paths = Arrays.stream(delete.path())
                    .map(path -> StringUtils.isBlank(path) ? endpoint.id() : endpoint.id()
                        + StringUtils.prependIfMissing(path, "/")).collect(Collectors.toSet());
                map.put("method", RequestMethod.DELETE.name());
                map.put("path", paths.isEmpty() ? endpoint.id() : paths);
                map.put("name", endpoint.id());
                map.put("endpointType", RestControllerEndpoint.class.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map);
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
            });

            methods = findAnnotatedMethods(clazz, PostMapping.class);
            methods.forEach(method -> {
                var post = method.getAnnotation(PostMapping.class);
                var map = new LinkedHashMap<>();
                var paths = Arrays.stream(post.path())
                    .map(path -> StringUtils.isBlank(path) ? endpoint.id() : endpoint.id()
                        + StringUtils.prependIfMissing(path, "/")).collect(Collectors.toSet());
                map.put("method", RequestMethod.POST.name());
                map.put("path", paths.isEmpty() ? endpoint.id() : paths);
                map.put("name", endpoint.id());
                map.put("endpointType", RestControllerEndpoint.class.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map);
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
            });

            methods = findAnnotatedMethods(clazz, PatchMapping.class);
            methods.forEach(method -> {
                var patch = method.getAnnotation(PatchMapping.class);
                var map = new LinkedHashMap<>();
                var paths = Arrays.stream(patch.path())
                    .map(path -> StringUtils.isBlank(path) ? endpoint.id() : endpoint.id()
                        + StringUtils.prependIfMissing(path, "/")).collect(Collectors.toSet());
                map.put("method", RequestMethod.PATCH.name());
                map.put("path", paths.isEmpty() ? endpoint.id() : paths);
                map.put("name", endpoint.id());
                map.put("endpointType", RestControllerEndpoint.class.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map);
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
            });

            methods = findAnnotatedMethods(clazz, PutMapping.class);
            methods.forEach(method -> {
                var put = method.getAnnotation(PutMapping.class);
                var map = new LinkedHashMap<>();
                var paths = Arrays.stream(put.path())
                    .map(path -> StringUtils.isBlank(path) ? endpoint.id() : endpoint.id()
                        + StringUtils.prependIfMissing(path, "/")).collect(Collectors.toSet());
                map.put("method", RequestMethod.PUT.name());
                map.put("path", paths.isEmpty() ? endpoint.id() : paths);
                map.put("name", endpoint.id());
                map.put("endpointType", RestControllerEndpoint.class.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map);
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
            });

            if (!properties.isEmpty()) {
                var destination = new File(parentPath, endpoint.id());
                destination.mkdirs();

                var configFile = new File(destination, "config.yml");
                CasConfigurationMetadataCatalog.export(configFile, properties);
            }
        });

        subTypes = reflections.getTypesAnnotatedWith(Endpoint.class, true);
        subTypes.forEach(clazz -> {
            var properties = new ArrayList<Map<?, ?>>();
            var endpoint = clazz.getAnnotation(Endpoint.class);

            var methods = findAnnotatedMethods(clazz, ReadOperation.class);
            methods.forEach(method -> {
                var read = method.getAnnotation(ReadOperation.class);
                var map = new LinkedHashMap<>();
                map.put("method", RequestMethod.GET.name());
                map.put("path", endpoint.id());
                map.put("name", endpoint.id());
                map.put("endpointType", Endpoint.class.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map);
                if (read.produces().length > 0) {
                    map.put("produces", read.produces());
                }
                properties.add(map);
            });

            methods = findAnnotatedMethods(clazz, WriteOperation.class);
            methods.forEach(method -> {
                var write = method.getAnnotation(WriteOperation.class);
                var map = new LinkedHashMap<>();
                map.put("method", RequestMethod.POST.name());
                map.put("path", endpoint.id());
                map.put("name", endpoint.id());
                map.put("endpointType", Endpoint.class.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map);
                if (write.produces().length > 0) {
                    map.put("produces", write.produces());
                }
                properties.add(map);
            });

            methods = findAnnotatedMethods(clazz, DeleteOperation.class);
            methods.forEach(method -> {
                var delete = method.getAnnotation(DeleteOperation.class);
                var map = new LinkedHashMap<>();
                map.put("method", RequestMethod.DELETE.name());
                map.put("path", endpoint.id());
                map.put("name", endpoint.id());
                map.put("endpointType", Endpoint.class.getSimpleName());
                collectActuatorEndpointMethodMetadata(method, map);
                if (delete.produces().length > 0) {
                    map.put("produces", delete.produces());
                }
                properties.add(map);
            });

            if (!properties.isEmpty()) {
                var destination = new File(parentPath, endpoint.id());
                destination.mkdirs();

                var configFile = new File(destination, "config.yml");
                CasConfigurationMetadataCatalog.export(configFile, properties);
            }
        });
    }

    private static void collectActuatorEndpointMethodMetadata(final Method method, final Map<Object, Object> map) {
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

        var parameters = new ArrayList<Map<?, ?>>();

        if (method.getParameters().length == 0) {
            if (isCasEndpoint(clazz)) {
                var operation = method.getAnnotation(Operation.class);
                if (operation == null) {
                    throw new RuntimeException("Unable to locate @Operation annotation for " + method.toGenericString()
                        + " in declaring class " + clazz.getName());
                }
                map.put("summary", StringUtils.appendIfMissing(operation.summary(), "."));
            }
        } else {
            for (var i = 0; i < method.getParameters().length; i++) {
                var param = method.getParameters()[i];

                var paramData = new LinkedHashMap<String, Object>();

                var required = param.getAnnotation(Nullable.class) == null;
                if (!required) {
                    required = Optional.ofNullable(param.getAnnotation(PathVariable.class))
                        .map(PathVariable::required).orElse(false);
                }
                paramData.put("required", required);

                paramData.put("type", param.getType().getSimpleName());
                var selector = param.getAnnotation(Selector.class) != null;
                paramData.put("selector", selector || param.getAnnotation(PathVariable.class) != null);

                if (isCasEndpoint(clazz)) {
                    var operation = method.getAnnotation(Operation.class);
                    if (operation == null) {
                        throw new RuntimeException("Unable to locate @Operation annotation for " + method.toGenericString()
                            + " in declaring class " + clazz.getName());
                    }
                    if (!map.containsKey("deprecated") && operation.deprecated()) {
                        map.put("deprecated", true);
                    }
                    map.put("summary", StringUtils.appendIfMissing(operation.summary(), "."));

                    if (!param.getType().equals(HttpServletRequest.class)
                        && !param.getType().equals(HttpServletResponse.class)
                        && !param.getType().equals(MultiValueMap.class)) {

                        if (operation.parameters().length == 0) {
                            throw new RuntimeException("Unable to locate @Parameter annotation for " + method.toGenericString()
                                + " in declaring class " + clazz.getName());
                        }
                        var parameter = operation.parameters()[i];
                        paramData.put("name", parameter.name());
                        paramData.put("required", (boolean) paramData.get("required") || parameter.required());

                        if (selector) {
                            var path = map.get("path");
                            path = StringUtils.appendIfMissing(path.toString(), "/")
                                .concat(String.format("${%s}", parameter.name()));
                            map.put("path", path);
                        }
                    }
                } else {
                    if (selector) {
                        var path = map.get("path");
                        path = StringUtils.appendIfMissing(path.toString(), "/")
                            .concat(String.format("${%s}", param.getName()));
                        map.put("path", path);
                    }
                }

                parameters.add(paramData);
            }
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

        var files = FileUtils.listFiles(parent, new String[]{"html"}, true);
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

    private static void exportThirdPartyConfiguration(final File dataPath) {
        var results = CasConfigurationMetadataCatalog.query(
            ConfigurationMetadataCatalogQuery.builder()
                .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.THIRD_PARTY)
                .build());
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
        var annotatedMethods = new ArrayList<Method>();
        try {
            var methods = clazz.getMethods();
            for (var method : methods) {
                if (method.isAnnotationPresent(annotationClass)) {
                    annotatedMethods.add(method);
                }
            }
        } catch (final Throwable throwable) {
            LOGGER.info("Failed to locate annotated methods: {}", throwable.getMessage());
        }
        return annotatedMethods;
    }
}
