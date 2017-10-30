/**
 * Created by tschmidt on 2/23/17.
 */
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';

import {FormRoutingModule} from './form-routing.module';
import {FormComponent} from './form.component';
import {FormResolve} from './form.resolover';
import {TabBasicsComponent} from './tab-basics/tab-basics.component';
import {TabLogoutComponent} from './tab-logout/tab-logout.component';
import {TabAccessstrategyComponent} from './tab-accessstrategy/tab-accessstrategy.component';
import {TabMulitauthComponent} from './tab-mulitauth/tab-mulitauth.component';
import {TabProxyComponent} from './tab-proxy/tab-proxy.component';
import {TabUsernameattrComponent} from './tab-usernameattr/tab-usernameattr.component';
import {TabAttrreleaseComponent} from './tab-attrrelease/tab-attrrelease.component';
import {TabPropertiesComponent} from './tab-properties/tab-properties.component';
import {FormService} from './form.service';
import {AccessStrategyComponent} from './access-strategy/access-strategy.component';
import {AttributeReleaseComponent} from './attribute-release/attribute-release.component';
import {EnabledComponent} from './enabled/enabled.component';
import {EvalorderComponent} from './evalorder/evalorder.component';
import {LinkrefsComponent} from './linkrefs/linkrefs.component';
import {LogoComponent} from './logo/logo.component';
import {LogoutComponent} from './logout/logout.component';
import {LogouttypeevalComponent} from './logouttypeeval/logouttypeeval.component';
import {MultiauthpaneComponent} from './multiauthpane/multiauthpane.component';
import {OauthclientComponent} from './oauthclient/oauthclient.component';
import {PropertiespaneComponent} from './propertiespane/propertiespane.component';
import {ProxyComponent} from './proxy/proxy.component';
import {PubkeyComponent} from './pubkey/pubkey.component';
import {RejectedattributesComponent} from './rejectedattributes/rejectedattributes.component';
import {ReqhandlersComponent} from './reqhandlers/reqhandlers.component';
import {SamlclientComponent} from './samlclient/samlclient.component';
import {ServicedescComponent} from './servicedesc/servicedesc.component';
import {ServiceidComponent} from './serviceid/serviceid.component';
import {ServicenameComponent} from './servicename/servicename.component';
import {ServicetypeComponent} from './servicetype/servicetype.component';
import {ThemeidComponent} from './themeid/themeid.component';
import {UidattrsComponent} from './uidattrs/uidattrs.component';
import {Data} from './data';
import {SharedModule} from '../shared/shared.module';
import {TabAdvancedComponent} from './tab-advanced/tab-advanced.component';
import { AttributeReleaseChecksComponent } from './attribute-release-checks/attribute-release-checks.component';
import { AttributeReleaseFiltersComponent } from './attribute-release-filters/attribute-release-filters.component';
import { AttributeReleasePoliciesComponent } from './attribute-release-policies/attribute-release-policies.component';
import { AttributeReleasePrincipalRepoComponent } from './attribute-release-principal-repo/attribute-release-principal-repo.component';
import { WsfedclientComponent } from './wsfedclient/wsfedclient.component';
import { WsfedattrrelpoliciesComponent } from './wsfedattrrelpolocies/wsfedattrrelpolicies.component';
import { SamlservicespaneComponent } from './samlservicespane/samlservicespane.component';
import { RemoteComponent } from './access-strategy/remote/remote.component';
import { TimeComponent } from './access-strategy/time/time.component';
import { GrouperComponent } from './access-strategy/grouper/grouper.component';
import { OidcclientComponent } from './oidcclient/oidcclient.component';
import { MappedattributesComponent } from './mappedattributes/mappedattributes.component';
import { TabOauthComponent } from './tab-oauth/tab-oauth.component';
import { TabSamlComponent } from './tab-saml/tab-saml.component';
import { TabWsfedComponent } from './tab-wsfed/tab-wsfed.component';
import {TabBaseComponent} from './tab-base';
import { AttributeReleaseConsentComponent } from './attribute-release-consent/attribute-release-consent.component';
import { SurrogateComponent } from './access-strategy/surrogate/surrogate.component';
import { ContactsComponent } from './contacts/contacts.component';
import { TabContactsComponent } from './tab-contacts/tab-contacts.component';
import { ExpirationComponent } from './expiration/expiration.component';
import { TabExpirationComponent } from './tab-expiration/tab-expiration.component';
import { MetadataComponent } from './samlclient/metadata/metadata.component';
import { SecurityComponent } from './samlclient/security/security.component';
import { OptionalComponent } from './samlclient/optional/optional.component';
import { NameidComponent } from './samlclient/nameid/nameid.component';
import { MappedComponent } from './attribute-release-filters/mapped/mapped.component';
import {TabOIDCComponent} from './tab-oidc/tab-oidc.component';


@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    HttpModule,
    SharedModule,
    FormRoutingModule
  ],
  declarations: [
    FormComponent,
    TabBasicsComponent,
    TabLogoutComponent,
    TabAccessstrategyComponent,
    TabMulitauthComponent,
    TabProxyComponent,
    TabUsernameattrComponent,
    TabAttrreleaseComponent,
    TabPropertiesComponent,
    TabAdvancedComponent,
    TabSamlComponent,
    TabOauthComponent,
    TabOIDCComponent,
    TabWsfedComponent,
    TabBaseComponent,
    FormComponent,
    AccessStrategyComponent,
    AttributeReleaseComponent,
    EnabledComponent,
    EvalorderComponent,
    LinkrefsComponent,
    LogoComponent,
    LogoutComponent,
    LogouttypeevalComponent,
    MultiauthpaneComponent,
    OauthclientComponent,
    PropertiespaneComponent,
    ProxyComponent,
    PubkeyComponent,
    RejectedattributesComponent,
    ReqhandlersComponent,
    SamlclientComponent,
    ServicedescComponent,
    ServiceidComponent,
    ServicenameComponent,
    ServicetypeComponent,
    ThemeidComponent,
    UidattrsComponent,
    AttributeReleaseChecksComponent,
    AttributeReleaseFiltersComponent,
    AttributeReleasePoliciesComponent,
    AttributeReleasePrincipalRepoComponent,
    WsfedclientComponent,
    WsfedattrrelpoliciesComponent,
    SamlservicespaneComponent,
    RemoteComponent,
    TimeComponent,
    GrouperComponent,
    OidcclientComponent,
    MappedattributesComponent,
    TabOauthComponent,
    TabSamlComponent,
    TabWsfedComponent,
    AttributeReleaseConsentComponent,
    SurrogateComponent,
    ContactsComponent,
    TabContactsComponent,
    ExpirationComponent,
    TabExpirationComponent,
    MetadataComponent,
    SecurityComponent,
    OptionalComponent,
    NameidComponent,
    MappedComponent,
  ],
  providers: [
    FormResolve,
    FormService,
    Data
  ]
})

export class FormModule {}
