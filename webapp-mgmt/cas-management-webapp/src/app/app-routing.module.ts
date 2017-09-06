/**
 * Created by tschmidt on 2/23/17.
 */
import { NgModule } from '@angular/core'
import { RouterModule } from '@angular/router';
import {ServicesComponent} from "./services/services.component";
import {ServicesResolve} from "./services/services.resolover";

@NgModule({
  imports: [
    RouterModule.forRoot( [
      {
        path: 'services',
        component: ServicesComponent,
        resolve: {
          resp: ServicesResolve
        }
      },
      {
        path: 'manage.html',
        redirectTo: 'services',
        pathMatch: 'full'
      },
    ]),
  ],
  exports: [ RouterModule ]
})

export class AppRoutingModule {}
