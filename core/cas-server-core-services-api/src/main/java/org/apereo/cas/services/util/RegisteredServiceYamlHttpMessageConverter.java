package org.apereo.cas.services.util;

import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * This is {@link RegisteredServiceYamlHttpMessageConverter}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class RegisteredServiceYamlHttpMessageConverter<T> extends AbstractHttpMessageConverter<T> {
    public RegisteredServiceYamlHttpMessageConverter() {
        super(new MediaType("application", "vnd.cas.services+yaml"));
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz) || RegisteredService.class.isAssignableFrom(clazz);
    }

    @Override
    protected T readInternal(final Class<? extends T> clazz, final HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new NotImplementedException("read() operation is not implemented");
    }

    @Override
    protected void writeInternal(final T t, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try (val writer = new OutputStreamWriter(outputMessage.getBody(), StandardCharsets.UTF_8)) {
            val serializer = new RegisteredServiceYamlSerializer();
            if (t instanceof Collection) {
                Collection.class.cast(t)
                    .stream()
                    .filter(object -> object instanceof RegisteredService)
                    .forEach(service -> serializer.to(writer, RegisteredService.class.cast(service)));
            } else if (t instanceof RegisteredService) {
                serializer.to(writer, RegisteredService.class.cast(t));
            }
        }
    }
}
