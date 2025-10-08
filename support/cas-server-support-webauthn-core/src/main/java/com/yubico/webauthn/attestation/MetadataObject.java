package com.yubico.webauthn.attestation;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.webauthn.WebAuthnUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.yubico.internal.util.CertificateParser;
import com.yubico.internal.util.ExceptionUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import tools.jackson.core.JacksonException;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is {@link MetadataObject}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(of = "data", callSuper = false)
public class MetadataObject {
    private static final ObjectMapper OBJECT_MAPPER = null; //JacksonCodecs.json();

    private static final TypeReference<Map<String, String>> MAP_STRING_STRING_TYPE =
        new TypeReference<>() {
        };

    private static final TypeReference<List<String>> LIST_STRING_TYPE =
        new TypeReference<>() {
        };

    private static final TypeReference<List<JsonNode>> LIST_JSONNODE_TYPE =
        new TypeReference<>() {
        };

    private final JsonNode data;

    private final String identifier;

    private final long version;

    private final Map<String, String> vendorInfo;

    private final List<String> trustedCertificates;

    private final List<JsonNode> devices;

    @JsonCreator
    public MetadataObject(final JsonNode data) {
        this.data = data;
        vendorInfo = OBJECT_MAPPER.readValue(data.get("vendorInfo").traverse(ObjectReadContext.empty()), MAP_STRING_STRING_TYPE);
        trustedCertificates = OBJECT_MAPPER.readValue(data.get("trustedCertificates").traverse(ObjectReadContext.empty()), LIST_STRING_TYPE);
        devices = OBJECT_MAPPER.readValue(data.get("devices").traverse(ObjectReadContext.empty()), LIST_JSONNODE_TYPE);

        identifier = data.get("identifier").asString();
        version = data.get("version").asLong();
    }

    public static MetadataObject readDefault() {
        return readMetadata("/metadata.json");
    }

    public static MetadataObject readPreview() {
        return readMetadata("/preview-metadata.json");
    }

    public static MetadataObject readMetadata(final String path) {
        return FunctionUtils.doUnchecked(() -> {
            try (val is = MetadataObject.class.getResourceAsStream(path)) {
                return readMetadata(is);
            }
        });
    }

    public static MetadataObject readMetadata(final InputStream is) {
        try {
            return WebAuthnUtils.getObjectMapper().readValue(is, MetadataObject.class);
        } catch (final JacksonException e) {
            throw ExceptionUtil.wrapAndLog(LOGGER, "Failed to read default metadata", e);
        }
    }

    @JsonIgnore
    public List<X509Certificate> getParsedTrustedCertificates() throws CertificateException {
        val list = new ArrayList<X509Certificate>();
        for (val trustedCertificate : trustedCertificates) {
            val x509Certificate = CertificateParser.parsePem(trustedCertificate);
            list.add(x509Certificate);
        }
        return list;
    }

    public List<JsonNode> getDevices() {
        return MoreObjects.firstNonNull(devices, List.of());
    }
}
