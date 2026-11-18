# Custom Service Registry

Source: https://apereo.github.io/cas/development/services/Custom-Service-Management.html

Store registered services somewhere CAS does not support by implementing your own `ServiceRegistryDao` and registering it with CAS.

If you wish to design your own implementation of a service registry, you will need to inject your implementation into CAS as such:

```java
package org.apereo.cas.support;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyConfiguration implements ServiceRegistryExecutionPlanConfigurer {

  @Bean
  @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
  public ServiceRegistryDao serviceRegistry() {
      ...
  }
  
  @Override
  public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
    plan.registerServiceRegistry(serviceRegistry());
  }
}
```

At a minimum, your overlay will need to include the following modules:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-core-services"
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
