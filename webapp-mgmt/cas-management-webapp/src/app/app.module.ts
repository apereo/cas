import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';

import {AppComponent} from './app.component';
import {Messages} from './messages';
import {AppRoutingModule} from './app-routing.module';
import {ServicesModule} from './services/services.module';
import {HeaderComponent} from './header/header.component';
import {SharedModule} from './shared/shared.module';
import {FormModule} from './form/form.module';
import { DeleteComponent } from './delete/delete.component';
import {DomainsModule} from './domains/domains.module';
import { SearchComponent } from './search/search.component';
import {SearchService} from './search/SearchService';

import {HeaderService} from './header/header.service';
import {InitComponent} from 'app/init.component';
import {ControlsComponent} from './controls/controls.component';
import {ControlsService} from './controls/controls.service';
import { FooterComponent } from './footer/footer.component';
import {HttpClientModule} from '@angular/common/http';


@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpClientModule,
    DomainsModule,
    ServicesModule,
    FormModule,
    SharedModule,
    AppRoutingModule
  ],
  declarations: [
    AppComponent,
    HeaderComponent,
    DeleteComponent,
    SearchComponent,
    InitComponent,
    FooterComponent,
  ],
  entryComponents: [
    DeleteComponent
  ],
  providers: [
    Messages,
    SearchService,
    HeaderService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
