/**
 * Created by tschmidt on 2/23/17.
 */
import { NgModule } from '@angular/core'
import { RouterModule } from '@angular/router';
import {ServicesComponent} from './services/services.component';
import {ServicesResolve} from './services/services.resolover';
import {DomainsComponent} from './domains/domains.component';
import {SearchComponent} from './search/search.component';
import {InitComponent} from './init.component';

@NgModule({
  imports: [
    RouterModule.forRoot( [
      {
        path: 'domains',
        component: DomainsComponent,
      },
      {
        path: 'services/:domain',
        component: ServicesComponent,
        resolve: {
          resp: ServicesResolve
        }
      },
      {
        path: 'search/:query',
        component: SearchComponent
      },
      {
        path: 'manage.html',
        component: InitComponent
      },
    ]),
  ],
  exports: [ RouterModule ]
})

export class AppRoutingModule {}
