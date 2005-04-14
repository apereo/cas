package org.jasig.cas.web.support;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.event.advice.PageRequestHandlerInterceptorAdapter;
import org.springframework.context.support.StaticApplicationContext;

import junit.framework.TestCase;


public class AutowireHandlerInterceptorsSimpleUrlHandlerMappingTests extends
    TestCase {

    private StaticApplicationContext context;
    
    private AutowireHandlerInterceptorsSimpleUrlHandlerMapping mapping;
    
    private Map map = new HashMap();

    protected void setUp() throws Exception {
        this.context = new StaticApplicationContext();
        this.mapping = new AutowireHandlerInterceptorsSimpleUrlHandlerMapping();
        this.mapping.setUrlMap(this.map);
        
        this.map.put("/test", "testController");
        
        this.mapping.setApplicationContext(this.context);
    }
    
    public void testInitApplicationContextNoInterceptors() throws Exception {
        this.mapping.initApplicationContext();
    }
    
    public void testInitApplicationContextWithInterceptors() throws Exception {
        this.context.registerPrototype("test", PageRequestHandlerInterceptorAdapter.class);
        this.mapping.initApplicationContext();
    }
}
