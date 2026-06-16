package org.apereo.cas.web.report;

import module java.base;
import com.sun.management.HotSpotDiagnosticMXBean;
import lombok.val;
import java.lang.management.ManagementFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link HeapDumpAnalysisEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@TestPropertySource(properties = "management.endpoint.heapDumpAnalysis.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class HeapDumpAnalysisEndpointTests extends AbstractCasEndpointTests {

    @Test
    void verifyOperation(@TempDir final Path directory) throws Throwable {
        val hprof = generateHeapDump(directory);
        try (val inputStream = Files.newInputStream(hprof)) {
            val file = new MockMultipartFile("file", "sample.hprof",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
            val result = mockMvc.perform(multipart("/actuator/heapDumpAnalysis")
                    .file(file)
                    .queryParam("top", "3")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();
            result.getAsyncResult(TimeUnit.MINUTES.toMillis(2));
            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.instanceCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.classesByInstanceCount.length()").value(allOf(greaterThan(0), lessThanOrEqualTo(3))))
                .andExpect(jsonPath("$.classesByInstanceCount[0].className").isString())
                .andExpect(jsonPath("$.classesByInstanceCount[0].instanceCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.classesByShallowSize.length()").value(allOf(greaterThan(0), lessThanOrEqualTo(3))))
                .andExpect(jsonPath("$.classesByShallowSize[0].className").isString())
                .andExpect(jsonPath("$.classesByShallowSize[0].shallowSizeBytes").value(greaterThanOrEqualTo(0)));
        }
    }

    private static Path generateHeapDump(final Path directory) throws Exception {
        val hprof = directory.resolve("sample.hprof");
        val heapDumpBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        assertNotNull(heapDumpBean);
        heapDumpBean.dumpHeap(hprof.toAbsolutePath().toString(), true);
        return hprof;
    }
}
