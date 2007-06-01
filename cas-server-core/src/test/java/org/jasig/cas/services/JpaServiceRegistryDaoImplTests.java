/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import junit.framework.TestCase;

/**
 * 
 * @author battags
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class JpaServiceRegistryDaoImplTests extends TestCase {

    private JpaServiceRegistryDaoImpl dao;

    @Override
    protected void setUp() throws Exception {
        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUsername("sa");
        dataSource.setUrl("jdbc:hsqldb:mem:cas");
        dataSource.setSuppressClose(true);
        dataSource.setAutoCommit(true);
        
        final LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
        fb.setDataSource(dataSource);
        
        final HibernateJpaVendorAdapter adapter=  new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        adapter.setShowSql(true);
        fb.setJpaVendorAdapter(adapter);
        fb.afterPropertiesSet();
        
        final EntityManagerFactory factory = fb.getObject();
        
        this.dao = new JpaServiceRegistryDaoImpl();
        this.dao.setEntityManagerFactory(factory);
        this.dao.afterPropertiesSet();
        super.setUp();
    }
    
    public void testSave() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        
        final RegisteredService r2 = this.dao.save(r);
        
        assertEquals(r, r2);
    }
    
    /*
    public void testUpdate() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        
        this.dao.save(r);
        
        final List<RegisteredService> services = this.dao.load();
        
        final RegisteredService r2 = services.get(0);
        
        System.out.println(r2.getId());
        
        r.setId(r2.getId());
        r.setTheme("mytheme");
        
        this.dao.save(r);
        
        assertEquals(r, this.dao.findServiceById(r.getId()));
    }*/
}
