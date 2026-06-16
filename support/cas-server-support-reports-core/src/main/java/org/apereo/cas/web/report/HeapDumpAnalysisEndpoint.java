package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.netbeans.lib.profiler.heap.HeapFactory;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * This is {@link HeapDumpAnalysisEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Endpoint(id = "heapDumpAnalysis", defaultAccess = Access.NONE)
public class HeapDumpAnalysisEndpoint extends BaseCasRestActuatorEndpoint {

    public HeapDumpAnalysisEndpoint(final CasConfigurationProperties casProperties,
                                    final ConfigurableApplicationContext applicationContext) {
        super(casProperties, applicationContext);
    }

    /**
     * Analyze response entity.
     *
     * @param top  the top
     * @param file the file
     * @return the response entity
     * @throws Exception the exception
     */
    @PostMapping(consumes = {
        MediaType.APPLICATION_OCTET_STREAM_VALUE,
        MediaType.MULTIPART_FORM_DATA_VALUE
    }, produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_YAML_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    })
    @Operation(summary = "Analyze the provided file")
    public ResponseEntity<HeapDumpAnalysis> analyze(
        @RequestParam(defaultValue = "100") final int top,
        @RequestPart("file") final MultipartFile file) throws Exception {
        val hprof = Files.createTempFile(UUID.randomUUID().toString(), ".hprof");
        try (val inputStream = file.getInputStream()) {
            Files.copy(inputStream, hprof, StandardCopyOption.REPLACE_EXISTING);
        }
        try {
            return ResponseEntity.ok(analyzeHeapDump(hprof, top));
        } finally {
            FileUtils.deleteQuietly(hprof.toFile());
        }
    }

    private static HeapDumpAnalysis analyzeHeapDump(final Path hprof, final int top) throws Exception {
        val heap = HeapFactory.createHeap(hprof.toFile());
        val allClasses = (List<JavaClass>) heap.getAllClasses();
        val topByInstanceCount = topClasses(allClasses, top, Comparator.comparingLong(JavaClass::getInstancesCount).reversed());
        val topByShallowSize = topClasses(allClasses, top, Comparator.comparingLong(JavaClass::getAllInstancesSize).reversed());
        val instanceCount = allClasses
            .stream()
            .mapToLong(JavaClass::getInstancesCount)
            .sum();
        return new HeapDumpAnalysis(allClasses.size(), instanceCount, topByInstanceCount, topByShallowSize);
    }

    private static List<ClassHistogramEntry> topClasses(
        final List<JavaClass> classes, final int topN, final Comparator<JavaClass> comparator) {
        return classes
            .stream()
            .filter(clazz -> clazz.getInstancesCount() > 0)
            .sorted(comparator)
            .limit(topN)
            .map(HeapDumpAnalysisEndpoint::toEntry)
            .toList();
    }

    private static ClassHistogramEntry toEntry(final JavaClass clazz) {
        val instanceCount = clazz.getInstancesCount();
        val shallowSizeBytes = clazz.getAllInstancesSize();
        val retainedSizeBytes = getRetainedSizeByClass(clazz);
        val averageShallow = instanceCount == 0 ? 0 : shallowSizeBytes / instanceCount;
        val averageRetained = instanceCount == 0 ? 0 : retainedSizeBytes / instanceCount;
        val ratio = shallowSizeBytes == 0 ? 0.0D : (double) retainedSizeBytes / shallowSizeBytes;
        return new ClassHistogramEntry(
            clazz.getName(),
            instanceCount,
            clazz.getAllInstancesSize(),
            retainedSizeBytes,
            averageShallow,
            averageRetained,
            ratio
        );
    }

    private static long getRetainedSizeByClass(final JavaClass clazz) {
        return FunctionUtils.doAndHandle(clazz::getRetainedSizeByClass, e -> -1L).get();
    }

    public record ClassHistogramEntry(
        String className,
        long instanceCount,
        long shallowSizeBytes,
        long retainedSizeBytes,
        long averageShallowSizeBytes,
        long averageRetainedSizeBytes,
        double retainedToShallowRatio) {
    }

    public record HeapDumpAnalysis(
        long classCount,
        long instanceCount,
        List<ClassHistogramEntry> classesByInstanceCount,
        List<ClassHistogramEntry> classesByShallowSize) {
    }
}
