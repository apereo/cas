package org.apereo.cas.palantir.schema;

import org.apereo.cas.CentralAuthenticationService;
import com.fasterxml.classmate.ResolvedType;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SubtypeResolver;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link ClassGraphSubtypeResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
class ClassGraphSubtypeResolver implements SubtypeResolver {
    private final ClassGraph classGraphConfig;

    private final Set<Class> subTypesProcessed = new HashSet<>();

    private ScanResult scanResult;

    ClassGraphSubtypeResolver(final List<String> excludeTypes) {
        classGraphConfig = new ClassGraph()
            .enableClassInfo()
            .enableInterClassDependencies()
            .rejectClasses(excludeTypes.toArray(ArrayUtils.EMPTY_STRING_ARRAY))
            .acceptPackages(CentralAuthenticationService.NAMESPACE);
    }

    private ScanResult getScanResult() {
        if (scanResult == null) {
            scanResult = classGraphConfig.scan();
        }
        return scanResult;
    }

    @Override
    public void resetAfterSchemaGenerationFinished() {
        if (scanResult != null) {
            scanResult.close();
            scanResult = null;
        }
    }

    @Override
    public List<ResolvedType> findSubtypes(final ResolvedType declaredType, final SchemaGenerationContext context) {
        if (declaredType.isInterface()) {
            val realType = declaredType.getErasedType();
            if (subTypesProcessed.contains(realType)) {
                return List.of();
            }
            subTypesProcessed.add(realType);
        }
        if (declaredType.isInstanceOf(Map.class) && declaredType.getTypeBindings().size() == 2) {
            val mapKeyType = declaredType.getTypeBindings().getBoundType(1);
            return resolveSubtypes(mapKeyType, context);
        }
        return resolveSubtypes(declaredType, context);
    }

    private List<ResolvedType> resolveSubtypes(final ResolvedType declaredType, final SchemaGenerationContext context) {
        if (!declaredType.getErasedType().equals(Object.class)) {
            val subtypes = declaredType.isInterface()
                ? getScanResult().getClassesImplementing(declaredType.getErasedType())
                : getScanResult().getSubclasses(declaredType.getErasedType());
            val typeContext = context.getTypeContext();

            return subtypes.loadClasses(true)
                .stream()
                .map(subclass -> typeContext.resolveSubtype(declaredType, subclass))
                .collect(Collectors.toList());
        }
        return null;
    }
}
