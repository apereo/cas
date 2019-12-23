package org.apereo.cas.ticket.registry;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RegExUtils;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.memcached.kryo.CasKryoPool;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketCatalogConfigurer;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenExpirationPolicy;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20CodeExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenExpirationPolicy;
import org.apereo.cas.util.CoreTicketUtils;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.OkAction;
import org.pac4j.oauth.profile.facebook.FacebookProfile;
import org.pac4j.oauth.profile.google2.Google2Profile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.sleuth.annotation.TagValueResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration("jdbcTicketRegistryConfiguration")
@AutoConfigureBefore(CasCoreTicketsConfiguration.class)
@Slf4j
public class JdbcTicketRegistryConfiguration {

  private static List<Class> extraClasses = Arrays.asList(
      CasProfile.class,
      FoundAction.class,
      OkAction.class,
      OAuth20Code.class,
      OAuth20CodeExpirationPolicy.class,
      OAuth20AccessToken.class,
      OAuth20AccessTokenExpirationPolicy.class,
      OAuth20RefreshToken.class,
      ServiceTicketImpl.class,
      OAuth20RefreshTokenExpirationPolicy.class,
      OAuth20AccessTokenExpirationPolicy.OAuthAccessTokenSovereignExpirationPolicy.class,
      OAuth20RefreshTokenExpirationPolicy.OAuthRefreshTokenStandaloneExpirationPolicy.class,
      StackTraceElement[].class,
      StackTraceElement.class,
      PrincipalBearingCredential.class,
      Google2Profile.class,
      FacebookProfile.class,
      Locale.class,
      ClientCredential.class
  );

  @Bean
  @ConditionalOnProperty(value = "cis.auth.noop.metrics", havingValue = "true")
  public CompositeMeterRegistry noOpMeterRegistry() {
    return new CompositeMeterRegistry(Clock.SYSTEM);
  }

  @Bean("ticketCatalog")
  public TicketCatalog jwtFriendlyTicketCatalog(final List<TicketCatalogConfigurer> configurers) {
    LOGGER.info("Configuring the  JwtFriendlyTicketCatalog");
    val plan = new JwtFriendlyTicketCatalog();
    configurers.forEach(c -> {
      val name = RegExUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
      LOGGER.trace("Configuring ticket metadata registration plan [{}]", name);
      c.configureTicketCatalog(plan);
    });
    return plan;
  }

  @Bean("ticketDatasourceTransactionManager")
  public PlatformTransactionManager dataSourceTransactionManager(@Qualifier("ticketDatasource") DataSource dataSource) {
    DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
    dataSourceTransactionManager.setDataSource(dataSource);
    return dataSourceTransactionManager;
  }

  @Bean("ticketDatasource")
  public DataSource dataSource(CasConfigurationProperties casProperties) {
    return JpaBeans.newDataSource(casProperties.getTicket().getRegistry().getJpa());
  }

  @Bean(name = {"jdbcTicketRegistry", "ticketRegistry"})
  @RefreshScope
  public JdbcTicketRegistry jdbcTicketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog,
      @Qualifier("ticketDatasource") final DataSource datasource,
      @Qualifier("ticketDatasourceTransactionManager") final PlatformTransactionManager transactionManager,
      final CasConfigurationProperties casProperties,
      final MeterRegistry meterRegistry) {
    val jpa = casProperties.getTicket().getRegistry().getJpa();
    val bean = new JdbcTicketRegistry(ticketCatalog,
        new JdbcTemplate(datasource),
        new TransactionTemplate(transactionManager),
        jpa,
        meterRegistry,
        new CasKryoPool(extraClasses));
    bean.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(jpa.getCrypto(), "jpa"));
    bean.initDatastore();
    return bean;
  }

  @Bean
  public JdbcTicketRegistryEndpoint jdbcTicketRegistryStatsEndpoint(final JdbcTicketRegistry ticketRegistry) {
    return new JdbcTicketRegistryEndpoint(ticketRegistry);
  }

  @Bean
  public TagValueResolver ticketTagValueResolver() {
    return parameter -> parameter instanceof Ticket
        ? ((Ticket) parameter).getPrefix()
        : limitToMaxSixCharacters(parameter.toString().split("-")[0]);
  }

  private static String limitToMaxSixCharacters(final String s) {
    return s.substring(0, Math.min(6, s.length()));
  }
}
