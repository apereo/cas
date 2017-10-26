/**
 * Created by tschmidt on 2/23/17.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import {ServicesComponent} from './services.component';
import {ServicesResolve} from './services.resolover';
import {ServiceViewService} from './service.service';
import {SharedModule} from '../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    HttpModule,
    SharedModule
  ],
  declarations: [
    ServicesComponent,
  ],
  providers: [
    ServicesResolve,
    ServiceViewService
  ]
})

export class ServicesModule {}

