---
layout: default
title: CAS - Custom Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Custom Multifactor Authentication

To create your own custom multifactor authentication provider, you will need to design components that primarily register a customized 
authentication flow into the CAS webflow engine under a unique identifier. Later on, you will also need to consider strategies by which 
your custom multifactor authentication provider [can be triggered](Configuring-Multifactor-Authentication-Triggers.html).

<div class="alert alert-warning"><strong>Remember</strong><p>Designing a custom multifactor authentication provider
can be a challenging task, specially if you're not familiar with the CAS programming model, APIs and the underlying frameworks
and technologies that make this possible, such as Spring Webflow.</p></div>

## Provider ID

Each multifactor provider is assigned a unique identifier that is typically mapped or made equal to the underlying webflow. The unique 
identifier can be any arbitrary string of your choosing, provided it's kept distinct and sensible as it, depending on 
use case, may be used in other systems and by other applications to act as a trigger.

For the purposes of this guide, let's choose `mfa-custom` as our provider id.

## Register Webflow Configuration

The custom provider itself is its own standalone webflow that is then registered with the primary authentication flow.

```java
public class CustomAuthenticatorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {
    public static final String MFA_EVENT_ID = "mfa-custom";
      
    /*
        Define the appropriate constructor based on the parent class
        public CustomAuthenticatorWebflowConfigurer(...) {
        }
    */  

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(),
                MFA_EVENT_ID, yourCustomMfaFlowDefinitionRegistry);
    }
}
```
   
The `CustomAuthenticatorWebflowConfigurer` must be able to construct the webflow definition dynamically
using CAS-provided APIs. See the CAS codebase to review and learn from other implementations

## Design Provider

Multifactor authentication providers in CAS are represented in forms of `MultifactorAuthenticationProvider` instances.
The outline of the provider is briefly displayed below and much of its behavior is removed in favor of defaults.

```java
public class CustomMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = 4789727148634156909L;
}
```

## Register Provider

The custom webflow configuration needs to be registered with CAS. The outline of 
the configuration registration is sampled and summarized below:

```java
package org.example.cas;

@Configuration(value = "CustomAuthenticatorSubsystemConfiguration", proxyBeanMethods = false)
public class CustomAuthenticatorSubsystemConfiguration {
    ...
    @Bean
    public FlowDefinitionRegistry customFlowRegistry() {
        var builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
        builder.addFlowBuilder(new FlowModelFlowBuilder(
            new DefaultFlowModelHolder(new DynamicFlowModelBuilder())),
            "mfa-custom");
        return builder.build();
    }

    @Bean
    public MultifactorAuthenticationProvider customAuthenticationProvider() {
        var p = new CustomMultifactorAuthenticationProvider();
        p.setId("mfa-custom");
        return p;
    }

    @Bean
    public CasWebflowConfigurer customWebflowConfigurer() {
        return new CustomAuthenticatorWebflowConfigurer(...);
    } 

    @Bean
    public CasWebflowExecutionPlanConfigurer customWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(customWebflowConfigurer());
    }
    ...
}
```

Do not forget to register the configuration class with CAS. [See this guide](../configuration/Configuration-Management-Extensions.html) for better details.

## Triggers

The custom authentication webflow can be triggered using any of the [supported options](Configuring-Multifactor-Authentication-Triggers.html).
