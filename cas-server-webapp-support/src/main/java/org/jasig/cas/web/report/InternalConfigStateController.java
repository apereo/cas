/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.web.report;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jasig.cas.util.AbstractJacksonBackedJsonSerializer;
import org.jasig.cas.util.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Controller that exposes the CAS internal state and beans
 * as JSON. The report is available at <code>/status/config</code>.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Controller("internalConfigController")
@RequestMapping("/status/config")
public final class InternalConfigStateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalConfigStateController.class);

    private static final String VIEW_CONFIG = "monitoring/viewConfig";

    private static final String[] INCLUDE_PACKAGES = new String[] {"org.jasig"};

    @Autowired(required = true)
    private ApplicationContext applicationContext;

    @Autowired(required = true)
    @Qualifier("casProperties")
    private Properties casProperties;

    /**
     * Handle request.
     *
     * @param request the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(method = RequestMethod.GET)
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final Map<String, Object> list = getBeans(this.applicationContext);
        LOGGER.debug("Found [{}] beans to report", list.size());

        final JsonSerializer<Object> serializer = new BeanObjectJsonSerializer();
        final StringBuilder builder = new StringBuilder();
        builder.append('[');

        final Set<Map.Entry<String, Object>> entries = list.entrySet();
        final Iterator<Map.Entry<String, Object>> it = entries.iterator();

        while (it.hasNext()) {
            final Map.Entry<String, Object> entry = it.next();
            final Object obj = entry.getValue();

            final StringWriter writer = new StringWriter();
            writer.append('{');
            writer.append('\"' + entry.getKey() + "\":");
            serializer.toJson(writer, obj);
            writer.append('}');
            builder.append(writer);

            if (it.hasNext()) {
                builder.append(',');
            }
        }
        builder.append(']');
        final ModelAndView mv = new ModelAndView(VIEW_CONFIG);
        final String jsonData = StringEscapeUtils.escapeJson(builder.toString());

        mv.addObject("jsonData", jsonData);
        mv.addObject("properties", casProperties.entrySet());
        return mv;
    }

    private static final class BeanObjectJsonSerializer extends AbstractJacksonBackedJsonSerializer<Object> {
        private static final long serialVersionUID = 691461175315322624L;

        /**
         * Instantiates a new Bean object json serializer.
         */
        BeanObjectJsonSerializer() {
            super(new MinimalPrettyPrinter());
        }

        @Override
        protected ObjectMapper initializeObjectMapper() {
            final ObjectMapper mapper = super.initializeObjectMapper();

            final FilterProvider filters = new SimpleFilterProvider()
                    .addFilter("beanObjectFilter", new CasSimpleBeanObjectFilter());
            mapper.setFilters(filters);

            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
            mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.addMixIn(Object.class, CasSimpleBeanObjectFilter.class);
            mapper.disable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            return mapper;
        }

        @Override
        protected Class<Object> getTypeToSerialize() {
            return Object.class;
        }

        @JsonFilter("beanObjectFilter")
        private static class CasSimpleBeanObjectFilter extends SimpleBeanPropertyFilter {
            private static final Logger LOGGER = getLogger(CasSimpleBeanObjectFilter.class);

            private static final String[] EXCLUDE_FIELDS = new String[] {"logger"};

            @Override
            public void serializeAsField(final Object pojo, final JsonGenerator jgen,
                                         final SerializerProvider provider,
                                         final PropertyWriter writer) throws Exception {
                try {
                    if (!canSerializeField(pojo, writer)) {
                        return;
                    }
                    super.serializeAsField(pojo, jgen, provider, writer);
                } catch (final Exception e) {
                    LOGGER.debug(e.getMessage());
                }
            }

            /**
             * Can serialize field?
             *
             * @param pojo the pojo
             * @param writer the writer
             * @return the boolean
             */
            private boolean canSerializeField(final Object pojo, final PropertyWriter writer) {
                boolean foundPackage = false;
                final String packageName = pojo.getClass().getPackage().getName();
                for (int i = 0; !foundPackage && i < INCLUDE_PACKAGES.length; i++) {
                    foundPackage = (packageName.startsWith(INCLUDE_PACKAGES[i]));
                }
                if (!foundPackage) {
                    LOGGER.trace("Package [{}] is ignored", packageName);
                    return false;
                }


                boolean foundField = true;
                final String fieldName = writer.getFullName().getSimpleName();
                for (int i = 0; foundField && i < EXCLUDE_FIELDS.length; i++) {
                    foundField = !fieldName.equalsIgnoreCase(EXCLUDE_FIELDS[i]);
                }

                if (!foundField) {
                    LOGGER.trace("Field [{}] is excluded", fieldName);
                    return false;
                }
                return true;
            }
        }
    }

    /**
     * Gets beans in the application context.
     *
     * @param ctx the ctx
     * @return the beans
     */
    private static Map<String, Object> getBeans(final ApplicationContext ctx) {
        final String[] all = BeanFactoryUtils.beanNamesIncludingAncestors(ctx);

        final Map<String, Object> singletons = new HashMap<>(all.length);
        for (final String name : all) {
            try {
                final Object object = ctx.getBean(name);
                if (object != null) {

                    boolean foundPackage = false;
                    final String packageName = object.getClass().getPackage().getName();
                    for (int i = 0; !foundPackage && i < INCLUDE_PACKAGES.length; i++) {
                        foundPackage = (packageName.startsWith(INCLUDE_PACKAGES[i]));
                    }
                    if (foundPackage) {
                        singletons.put(name, object);
                    }

                }
            } catch (final BeanIsAbstractException e){
                LOGGER.debug("Skipping abstract bean definition. {}", e.getMessage());
            } catch (final Throwable e){
                LOGGER.trace(e.getMessage(), e);
            }
        }

        return singletons;
    }
}
