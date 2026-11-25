package org.apereo.cas.mcp;

import module java.base;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import tools.jackson.databind.json.JsonMapper;

/**
 * This is {@link CasMcpErrorHttpMessageConverter}. The Streamable HTTP transport answers requests it cannot accept, such as
 * a message without a session id, with an {@link McpError} as the response body. Written by the default JSON converter, that
 * body is the serialized exception, stack trace included. This converter, registered ahead of the default ones, writes it
 * as a JSON-RPC error response instead: the protocol version, a {@code null} id because the request could not be
 * associated with one, and the JSON-RPC error object of the exception alone.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class CasMcpErrorHttpMessageConverter extends AbstractHttpMessageConverter<McpError> {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    public CasMcpErrorHttpMessageConverter() {
        super(MediaType.APPLICATION_JSON);
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return McpError.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canRead(final Class<?> clazz, @Nullable final MediaType mediaType) {
        return false;
    }

    @Override
    protected McpError readInternal(final Class<? extends McpError> clazz, final HttpInputMessage inputMessage) {
        throw new HttpMessageNotReadableException("MCP errors are never read from requests", inputMessage);
    }

    @Override
    protected void writeInternal(final McpError error, final HttpOutputMessage outputMessage) throws IOException {
        val response = new LinkedHashMap<String, @Nullable Object>();
        response.put("jsonrpc", McpSchema.JSONRPC_VERSION);
        response.put("id", null);
        response.put("error", error.getJsonRpcError());
        outputMessage.getBody().write(MAPPER.writeValueAsBytes(response));
    }
}
