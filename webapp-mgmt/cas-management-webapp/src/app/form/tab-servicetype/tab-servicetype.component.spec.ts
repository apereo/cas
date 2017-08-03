/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabServicetypeComponent } from './tab-servicetype.component';
import {ServicetypeComponent} from "../servicetype/servicetype.component";
import {Messages} from "../../messages";
import {Data} from "../data";
import {SamlclientComponent} from "../samlclient/samlclient.component";
import {OauthclientComponent} from "../oauthclient/oauthclient.component";
import {SharedModule} from "../../shared/shared.module";
import {WsfedclientComponent} from "../wsfedclient/wsfedclient.component";
import {WsfedattrrelpoliciesComponent} from "../wsfedattrrelpolocies/wsfedattrrelpolicies.component";
import {SamlservicespaneComponent} from "../samlservicespane/samlservicespane.component";

describe('TabServicetypeComponent', () => {
  let component: TabServicetypeComponent;
  let fixture: ComponentFixture<TabServicetypeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ TabServicetypeComponent, ServicetypeComponent, SamlclientComponent, OauthclientComponent, WsfedclientComponent, WsfedattrrelpoliciesComponent, SamlservicespaneComponent ],
      providers: [ Messages, Data ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabServicetypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
