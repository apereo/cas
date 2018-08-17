package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.NamedStubPersonAttributeDao;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


/**
 * A re-usable collection of utility methods for object instantiations and configurations used cross various
 * {@code @Bean} creation methods throughout CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

@UtilityClass
public class Beans {

    /**
     * New thread pool executor factory bean.
     *
     * @param config the config
     * @return the thread pool executor factory bean
     */
    public static ThreadPoolExecutorFactoryBean newThreadPoolExecutorFactoryBean(final ConnectionPoolingProperties config) {
        val bean = newThreadPoolExecutorFactoryBean(config.getMaxSize(), config.getMaxSize());
        bean.setCorePoolSize(config.getMinSize());
        return bean;
    }

    /**
     * New thread pool executor factory bean.
     *
     * @param keepAlive the keep alive
     * @param maxSize   the max size
     * @return the thread pool executor factory bean
     */
    public static ThreadPoolExecutorFactoryBean newThreadPoolExecutorFactoryBean(final long keepAlive,
                                                                                 final long maxSize) {
        val bean = new ThreadPoolExecutorFactoryBean();
        bean.setMaxPoolSize((int) maxSize);
        bean.setKeepAliveSeconds((int) keepAlive);
        return bean;
    }

    /**
     * New attribute repository person attribute dao.
     *
     * @param p the properties
     * @return the person attribute dao
     */
    @SneakyThrows
    public static IPersonAttributeDao newStubAttributeRepository(final PrincipalAttributesProperties p) {
        val dao = new NamedStubPersonAttributeDao();
        val pdirMap = new HashMap<String, List<Object>>();
        p.getStub().getAttributes().forEach((key, value) -> {
            val vals = org.springframework.util.StringUtils.commaDelimitedListToStringArray(value);
            pdirMap.put(key, Arrays.stream(vals).collect(Collectors.toList()));
        });
        dao.setBackingMap(pdirMap);
        return dao;
    }


    /**
     * New duration. If the provided length is duration,
     * it will be parsed accordingly, or if it's a numeric value
     * it will be pared as a duration assuming it's provided as seconds.
     *
     * @param length the length in seconds.
     * @return the duration
     */
    @SneakyThrows
    public static Duration newDuration(final String length) {
        if (NumberUtils.isCreatable(length)) {
            return Duration.ofSeconds(Long.parseLong(length));
        }
        return Duration.parse(length);
    }
}
