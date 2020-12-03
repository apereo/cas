package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.qr.web.flow.QRLoginWebflowConfigurer;
import org.apereo.cas.qr.web.flow.QRLoginGenerateCodeAction;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.execution.Action;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link QrConfiguration}.
 *
 * @author Ben Winston
 * @since 6.2.0
 */
@Configuration("QrConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class QrConfiguration {

	@Autowired
	private CasConfigurationProperties casProperties;

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Autowired
	@Qualifier("loginFlowRegistry")
	private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

	@Autowired
	@Qualifier("flowBuilderServices")
	private ObjectProvider<FlowBuilderServices> flowBuilderServices;

	@Autowired
	@Qualifier("defaultPrincipalResolver")
	private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

	@Autowired
	@Qualifier("servicesManager")
	private ObjectProvider<ServicesManager> servicesManager;

	@ConditionalOnMissingBean(name = "qrLoginWebflowConfigurer")
	@Bean
	@DependsOn("defaultWebflowConfigurer")
	public CasWebflowConfigurer qrLoginWebflowConfigurer() {
		return new QRLoginWebflowConfigurer(flowBuilderServices.getObject(),
			loginFlowDefinitionRegistry.getObject(),
			applicationContext, casProperties);
	}

	@Bean
	@ConditionalOnMissingBean(name = "qrLoginGenerateCodeAction")
	@RefreshScope
	public Action qrLoginGenerateCodeAction() {
		return new QRLoginGenerateCodeAction();
	}

	@Bean
	@ConditionalOnMissingBean(name = "qrLoginCasWebflowExecutionPlanConfigurer")
	public CasWebflowExecutionPlanConfigurer qrLoginCasWebflowExecutionPlanConfigurer() {
		return plan -> plan.registerWebflowConfigurer(qrLoginWebflowConfigurer());
	}

}
