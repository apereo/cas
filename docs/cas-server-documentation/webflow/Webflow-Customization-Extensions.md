---
layout: default 
title: CAS - Web Flow Extensions 
category: Webflow Management
---

{% include variables.html %}

# Extending CAS Webflow

The objective of this guide is to better describe how CAS utilizes Spring Webflow to accommodate various authentication flows. Please
remember that this is **NOT** to teach one how Spring Webflow itself works internally. If you want to learn more about Spring Webflow and
understand the internals of actions, states, decisions and scopes please [see this guide](https://github.com/apereo/spring-webflow/).

CAS by default operates on the following core webflow configuration files:

| Flow        | Description                                                                                     |
|-------------|-------------------------------------------------------------------------------------------------|
| `login`     | Authentication flow for login attempts.                                                         |
| `logout`    | Authentication flow for logout attempts.                                                        |
| `pswdreset` | [Password management](../password_management/Password-Management.html) and password reset flow. |
| `account`   | [Account management](../registration/Account-Management-Overview.html) and profile flow..       |

The above flows present a minimal structure for what CAS needs at its core to handle login and logout flows. It is important to note that at
runtime many other actions and states are injected into either of these flows dynamically depending on the CAS configuration and presence of
feature modules. Also note that each feature module itself may dynamically present other opinionated subflow configuration files that are
automagically picked up at runtime.

## Modifying Webflow

In modest trivial cases, you may be able to [overlay and modify](../installation/WAR-Overlay-Installation.html) the core flow configuration
files to add or override the desired behavior. Again, think very carefully before introducing those changes into your deployment
environment. Avoid making ad-hoc changes to the webflow as much as possible and consider how the change you have in mind might be more
suitable as a direct contribution to the CAS project itself so you can just take advantage of its configuration and *NOT* its maintenance.

To learn how to introduce new actions and state into a Spring Webflow, please [see this guide](https://github.com/apereo/spring-webflow/).

<div class="alert alert-info">:information_source: <strong>Speak Up</strong><p>If you find something that is broken where the 
webflow auto-configuration strategy fails to deliver as advertised, discuss that with the project community 
and submit a patch that corrects the bug or adds the desired behavior as a modest enhancement. 
Avoid one-off changes and make the change where the change belongs.</p></div>

In more advanced cases where you may need to take a deep dive and alter core CAS behavior conditionally, you would need to take advantage of
the CAS APIs to deliver changes. Using the CAS APIs directly does present the following advantages at some cost:

- Changes are all scoped to Java (Groovy, Kotlin, Clojure, etc).
- You have the full power of Java to dynamically and conditionally augment the Spring Webflow.
- Your changes are all self-contained.
- Changes are now part of the CAS APIs and they will be compiled. Breaking changes on upgrades, if any, should be noticed immediately at
  build time.

### Java

This is the most traditional yet most powerful method of dynamically altering the webflow internals. You will be asked to write components
that auto-configure the webflow and inject themselves into the running CAS application context only to be executed at runtime.

At a minimum, your overlay will need to include the following modules:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-core-webflow" %}

#### Design

Design your dynamic webflow configuration agent that alters the webflow using the following form:

```java
public class SomethingWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public SomethingWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                      FlowDefinitionRegistry flowDefinitionRegistry,
                                      ApplicationContext applicationContext,
                                      CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() throws Exception {
        var flow = super.getLoginFlow();
        // Magic happens; Call 'super' to see 
        // what you have access to and alter the flow.
    }
}
```

The parent class, `AbstractCasWebflowConfigurer`, providers a lot of helper methods and utilities in a *DSL-like* fashion to hide the
complexity of Spring Webflow APIs to make customization easier.

#### Register

You will then need to register your newly-designed component into the CAS application runtime:

```java
package org.example.something;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SomethingConfiguration implements CasWebflowExecutionPlanConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name = "somethingWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer somethingWebflowConfigurer() {
        return new SomethingWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(somethingWebflowConfigurer());
    }
}
```

Configuration classes need to be registered with CAS via the strategy [outlined here](../configuration/Configuration-Management-Extensions.html).

<div class="alert alert-info">:information_source: <strong>To Build & Beyond</strong><p>Note that compiling configuration classes and any other
piece of Java code that is put into the CAS Overlay may require additional CAS modules and dependencies on the classpath. You will need
to study the CAS codebase and find the correct modules that contain the components you need, such 
as <code>CasWebflowConfigurer</code> and others.</p></div>

See [this guide](ttps://docs.spring.io/spring-boot/docs/current/reference/html/) for more info.

### Groovy

You may configure CAS to alter and auto-configure the webflow via a Groovy script. This is the less elaborate option where you have modest
access to CAS APIs that allow you alter the webflow. However, configuration and scaffolding of the overlay and required dependencies is
easier as all is provided by CAS at runtime.

<div class="alert alert-warning">:warning: <strong>Stop Coding</strong><p>Remember that APIs provided 
here, specifically executed as part of the Groovy script are considered implementations 
internal to CAS mostly. They may be added or removed with little hesitation which means 
changes may break your deployment and upgrades at runtime. Remember that unlike Java 
classes, scripts are not statically compiled when you build CAS and you only may observe 
failures when you do in fact turn on the server and deploy. Thus, choose this option 
with good reason and make sure you have thought changes through before stepping into code.</p></div>

#### Configuration

{% include_cached casproperties.html properties="cas.webflow" includes=".autoconfigure,.groovy" %}

#### Webflow Auto Configuration

A sample Groovy script follows that aims to locate the CAS login flow and a particular state pre-defined in the flow. If found, a custom
action is inserted into the state to execute as soon as CAS enters that state in the flow. While this is a rather modest example, note that
the script has the ability to add/remove actions, states, transitions, add/remove subflows, etc.

```groovy
import java.util.*

import org.apereo.cas.*
import org.apereo.cas.web.*
import org.apereo.cas.web.support.*
import org.apereo.cas.web.flow.*

import org.springframework.webflow.*
import org.springframework.webflow.engine.*
import org.springframework.webflow.execution.*

Object run(final Object... args) {
    def (webflow,springApplicationContext,logger) = args

    logger.info("Configuring webflow context...")

    def loginFlow = webflow.getLoginFlow()
    if (webflow.containsFlowState(loginFlow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM)) {
        logger.info("Found state that initializes the login form")

        def state = webflow.getState(loginFlow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class)
        logger.info("The state id is {}", state.id)

        state.getEntryActionList().add({ requestContext ->
            def flowScope = requestContext.flowScope
            def httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext)

            logger.info("Action executing as part of ${state.id}. Stuff happens...")
            return new Event(this, "success")
        })

        logger.info("Added action to ${state.id}'s entry action list")
    }

    return true
}
```

The parameters passed are as follows:

| Parameter                  | Description                                                        |
|----------------------------|--------------------------------------------------------------------|
| `webflow`                  | The object representing a facade on top of Spring Webflow APIs.    |
| `springApplicationContext` | The Spring application context.                                    |
| `logger`                   | Logger object for issuing log messages such as `logger.info(...)`. |

#### Webflow Actions

Webflow operations are typically handled via `Action` components that are implemented and registered with the CAS runtime as `Bean` definitions. While these 
definitions could be conditionally substituted with an alternative implementation, you also have the option to carry out the action operation via Groovy 
scripts. In this scenario, you take over the responsibility of action implementation yourself, relieving CAS from providing you with an implementation.

{% include_cached casproperties.html properties="cas.webflow.groovy.actions" %}

<div class="alert alert-info">:information_source: <strong>Note</strong>
<p>You will need to dig up the name of the original action <code>Bean</code> first before you can provide a Groovy substitute. This will require a careful
analysis of CAS codebase. Furthermore, please note that not all Spring Webflow actions may be substituted with a Groovy equivalent. Groovy support
in this area is a continuous development effort and will gradually improve throughout various CAS releases. Cross-check with the codebase to be sure.
</p></div>

The outline of the script may be as follows:

```groovy
import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*
import org.springframework.webflow.*
import org.springframework.webflow.action.*

def run(Object[] args) {
    def (requestContext,applicationContext,properties,logger) = args
    logger.info("Handling action...")
    return new EventFactorySupport().event(this, "success")
}
```

The outcome of the script should be a Spring Webflow `Event`. The parameters passed are as follows:

| Parameter            | Description                                                                                              |
|----------------------|----------------------------------------------------------------------------------------------------------|
| `requestContext`     | The object representing the Spring Webflow execution context that carries the HTTP request and response. |
| `applicationContext` | The Spring application context.                                                                          |
| `properties`         | Reference to CAS configuration properties.                                                               |
| `logger`             | Logger object for issuing log messages such as `logger.info(...)`.                                       |

