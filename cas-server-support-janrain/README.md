# Janrain Engage Authentication support

## What is Janrain Engage?
> A turnkey social login and social sharing solution. Janrain Engage lets a website's visitors register or login with 
> their existing social network accounts from Facebook, Google, Twitter, Yahoo, LinkedIn or other networks, and share 
> content or their site activities with friends on multiple social networks. Additional functionality lets users 
> import their social network profile data and invite their friends to visit a website. [_wikipedia_](http://en.wikipedia.org/wiki/Janrain)

__Janrain Engage is a commercial service, but the free "Basic" level allows upto 2,500 unique users per year.  Please see [Janrain's website](http://www.janrain.com/) for details on account features and pricing.__


## What features are included with this module?
* User authentication to any social network or OpenID provider supported by Janrain Engage using [janrain4j](http://code.google.com/p/janrain4j/)
* Profile data from the social network login are exposed as CAS user attributes

## What is the difference between this module and the OAuth module?
This module is based on the client portion of the OAuth CAS module written by Jérôme Leleu (cas-server-support-oauth), so the configuration is very similar.  The main advantage of using Janrain Engage over configuring OAuth authentication directly are:

* Janrain Engage integrates with over [20 identity providers](http://documentation.janrain.com/providerguide) using multiple protocols including OAuth and OpenID. 
* The list of identity providers you allow users to select is held at Janrain and can be changed on the fly without modifying any CAS configuration.
* Profile data from the identity providers is provided in a single, consistent format.
* Multiple social network logins can be mapped to a local account using the [account mapping API](http://documentation.janrain.com/engage/api/account-mapping-integration-guide) 

## Attributes provided by this module
* ProviderName 
* PrimaryKey - for use with the [mapping API](http://documentation.janrain.com/engage/api/account-mapping-integration-guide)
* DisplayName
* FamilyName
* GivenName
* Email
* Birthday
* Gender
* PhoneNumber
* PreferredUsername
* PhotoURL
* Url
* StreetAddress
* Locality
* PostalCode
* Country
* FriendList 

See the [Janrain provider list](https://rpxnow.com/docs/providers) for details on which attributes are released by specific social networks and Engage pricing levels 

## Adding Janrain Engage support to CAS

1. ### Register a Janrain "social sign-in" application
Follow Janrain's [application](http://documentation.janrain.com/application-quick-start-guide) and [social sign-in](http://documentation.janrain.com/quick-start-guide) quickstart guides. **Important: You must add the domain of your cas server to the domain whitelist in the Janrain dashboard**

2. ### Add the Maven dependency
Add the following block to `$CAS_HOME/cas-server-webapp/pom.xml`:

           <dependency>
             <groupId>${project.groupId}</groupId>
             <artifactId>cas-server-support-janrain</artifactId>
             <version>${project.version}</version>
           </dependency>
          
           <module>cas-server-support-ldap</module>
3. ### Configure Janrain4j
Add this bean to `$CAS_HOME/cas-server-webapp/src/main/webapp/WEB-INF/deployerConfigContext.xml`:
        
           <bean class="com.googlecode.janrain4j.springframework.Janrain4jConfigurer"
             p:apiKey="JanrainAPIKey"
             p:applicationID="JanrainApplicationID"
             p:applicationDomain="https://example.rpxnow.com/"
             p:tokenUrl="https://cas.example.edu:8443/cas/login" />
Configure `p:apiKey`, `p:applicationID` and `p:applicationDomain` with the values provided in the Janrain account dashboard and `tokenUrl` is the URL for your CAS login page.  See the Janrain4j [documentation](http://janrain4j.googlecode.com/svn/docs/current/apidocs/com/googlecode/janrain4j/springframework/Janrain4jConfigurer.html) for all of the available configuration options.

4. ### Configure Authentication
To authenticate using Janrain Engage, add the `JanrainAuthenticationHandler` bean to the list of authentication handlers in `$CAS_HOME/cas-server-webapp/src/main/webapp/WEB-INF/deployerConfigContext.xml`:

	    <property name="authenticationHandlers">
         <list>
                   <bean class="org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler"
                                        p:httpClient-ref="httpClient" />
                   <bean class="org.jasig.cas.support.janrain.authentication.handler.support.JanrainAuthenticationHandler" />
               </list>
             </property>
           </bean>
         </list>
        </property>
You'll also need to add `JanrainCredentialsToPrincipalResolver` to the list of principal resolvers:

        <property name="credentialsToPrincipalResolvers">
            <list>
             <bean class="org.jasig.cas.support.janrain.authentication.principal.JanrainCredentialsToPrincipalResolver" />
             <bean class="org.jasig.cas.authentication.principal.HttpBasedServiceCredentialsToPrincipalResolver" />
            </list>
        </property>
        
5. ### Configure Attribute Population and Repository
To convert the profile data received from Janrain, configure the `authenticationMetaDataPopulators` property on the `authenticationManager` bean:

			<property name="authenticationMetaDataPopulators">
				<list>
					<bean
					 class="org.jasig.cas.support.janrain.authentication.JanrainAuthenticationMetaDataPopulator" />
				</list>
			</property>
You'll also need to configure the `attributeRepository` bean:
				
		<bean id="attributeRepository" class="org.jasig.services.persondir.support.StubPersonAttributeDao">
    		<property name="backingMap">
    			<map>
    				<entry key="DisplayName" value="DisplayName" />
    				<entry key="ProviderName" value="ProviderName" /> 
    				<entry key="PrimaryKey" value="PrimaryKey" />
    				<entry key="FamilyName" value="FamilyName" />
    				<entry key="GivenName" value="GivenName" />
    				<entry key="Email" value="Email" />
    				<entry key="Url" value="Url" />
    				<entry key="PhotoURL" value="PhotoURL" />
    				<entry key="PhoneNumber" value="PhoneNumber" />
    				<entry key="Gender" value="Gender" />
    				<entry key="Birthday" value="Birthday" />
    				<entry key="UTCoffset" value="UTSoffset" />
    				<entry key="StreetAddress" value="StreetAddress" />
    			    <entry key="Locality" value="Locality" />
                    <entry key="PostalCode" value="PostalCode" />
                    <entry key="Country" value="Country" />
    				<entry key="PreferredUsername" value="PreferredUsername" />
    				<entry key="FriendList" value="FriendList" />
    			</map>
    		</property>
    	</bean>
To release the attributes to CAS clients, you'll need to configure the [Service Manager](https://wiki.jasig.org/display/CASUM/Services+Management)	
6. ### Add `janrainAuthAction` to the CAS webflow
Add `janrainAuthAction` to `$CAS_HOME/cas-server-webapp/src/main/webapp/WEB-INF/login-webflow.xml`. It should be placed at the top of the file, just before the `ticketGratingTicketExistsCheck` decision-state:

        <action-state id="janrainAuthAction">
                <evaluate expression="janrainAuthAction" />
                <transition on="success" to="sendTicketGrantingTicket" />
                <transition on="error" to="ticketGrantingTicketExistsCheck" />
        </action-state>
To define the `janrainAuthAction` bean, add it to `$CAS_HOME/cas-server-webapp/src/main/webapp/WEB-INF/cas-servlet.xml`:

         <bean id="janrainAuthAction" class="org.jasig.cas.support.janrain.web.flow.JanrainAuthAction">
               <property name="centralAuthenticationService" ref="centralAuthenticationService" />
         </bean>
7. ### Modify the login page
First, add the janrain4j taglib to the top of `$CAS_HOME/cas-server-webapp/src/main/webapp/WEB-INF/view/jsp/default/ui/casLoginView.jsp`:

          <%@ taglib prefix="janrain" uri="http://janrain4j.googlecode.com/tags" %>
Next, you'll need to replace the username and password for with this tag:
      
          <janrain:signInEmbedded />	
    Here is a simple `casLoginView.jsp`:
    
          <%@ taglib prefix="janrain" uri="http://janrain4j.googlecode.com/tags" %>
    
            <%@ page contentType="text/html; charset=UTF-8" %>
            <jsp:directive.include file="includes/top.jsp" />
                    <janrain:signInEmbedded />
                    <p class="fl-panel fl-note fl-bevel-white fl-font-size-80">
                            <spring:message code="screen.welcome.security" />
                    </p>
            <jsp:directive.include file="includes/bottom.jsp" />
     