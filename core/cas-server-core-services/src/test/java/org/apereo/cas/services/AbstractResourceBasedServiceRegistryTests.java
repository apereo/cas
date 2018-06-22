package org.apereo.cas.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apereo.cas.category.FileSystemCategory;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.core.io.ClassPathResource;

/**
 * This is {@link AbstractResourceBasedServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Category(FileSystemCategory.class)
public abstract class AbstractResourceBasedServiceRegistryTests extends AbstractServiceRegistryTests {
    public static final ClassPathResource RESOURCE = new ClassPathResource("services");

    protected ServiceRegistry dao;

    @Override
    @SneakyThrows
    public void tearDownServiceRegistry() {
        FileUtils.cleanDirectory(RESOURCE.getFile());
        super.tearDownServiceRegistry();
    }

    @Test
    public void verifyServiceWithInvalidFileName() {
        final AbstractRegisteredService r = buildService(RandomUtils.nextInt());
        r.setName("hell/o@world:*");
        this.thrown.expect(IllegalArgumentException.class);
        this.dao.save(r);
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.dao;
    }
}
