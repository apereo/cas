/**
 * Created by tschmidt on 2/23/17.
 */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
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
import {TabAdvancedComponent} from './tab-advanced/tab-advanced.component';
import {TabSamlComponent} from './tab-saml/tab-saml.component';
import {TabOauthComponent} from './tab-oauth/tab-oauth.component';
import {TabWsfedComponent} from './tab-wsfed/tab-wsfed.component';
import {TabContactsComponent} from './tab-contacts/tab-contacts.component';
import {TabExpirationComponent} from './tab-expiration/tab-expiration.component';
import {TabOIDCComponent} from './tab-oidc/tab-oidc.component';

const childRoutes: Routes = [
  {
    path: 'basics',
    component: TabBasicsComponent,
    outlet: 'form'
  },
  {
    path: 'saml',
    component: TabSamlComponent,
    outlet: 'form'
  },
  {
    path: 'oauth',
    component: TabOauthComponent,
    outlet: 'form'
  },
  {
    path: 'oidc',
    component: TabOIDCComponent,
    outlet: 'form'
  },
  {
    path: 'wsfed',
    component: TabWsfedComponent,
    outlet: 'form'
  },
  {
    path: 'contacts',
    component: TabContactsComponent,
    outlet: 'form'
  },
  {
    path: 'logout',
    component: TabLogoutComponent,
    outlet: 'form'
  },
  {
    path: 'accessstrategy',
    component: TabAccessstrategyComponent,
    outlet: 'form'
  },
  {
    path: 'expiration',
    component: TabExpirationComponent,
    outlet: 'form'
  },
  {
    path: 'multiauth',
    component: TabMulitauthComponent,
    outlet: 'form'
  },
  {
    path: 'proxy',
    component: TabProxyComponent,
    outlet: 'form'
  },
  {
    path: 'userattr',
    component: TabUsernameattrComponent,
    outlet: 'form'
  },
  {
    path: 'attrRelease',
    component: TabAttrreleaseComponent,
    outlet: 'form'
  },
  {
    path: 'properties',
    component: TabPropertiesComponent,
    outlet: 'form'
  },
  {
    path: 'advanced',
    component: TabAdvancedComponent,
    outlet: 'form'
  }
]
@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'duplicate/:id',
        component: FormComponent,
        resolve: {
          resp: FormResolve
        },
        children: childRoutes,
        data: {
          duplicate: true,
        }
      },
      {
        path: 'form/:id',
        component: FormComponent,
        resolve: {
          resp: FormResolve
        },
        children: childRoutes
      }
    ])
  ],
  exports: [ RouterModule ]
})

export class FormRoutingModule {}

