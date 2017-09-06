import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';

import {AppComponent} from './app.component';
import {Messages} from "./messages";
import {AppRoutingModule} from "./app-routing.module";
import {ServicesModule} from "./services/services.module";
import {HeaderComponent} from "./header/header.component";
import {SharedModule} from "./shared/shared.module";
import {FormModule} from "./form/form.module";
import { DeleteComponent } from './delete/delete.component';
import {DomainsModule} from "./domains/domains.module";


@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpModule,
    DomainsModule,
    ServicesModule,
    FormModule,
    SharedModule,
    AppRoutingModule
  ],
  declarations: [
    AppComponent,
    HeaderComponent,
    DeleteComponent
  ],
  entryComponents: [
    DeleteComponent
  ],
  providers: [
    Messages
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
