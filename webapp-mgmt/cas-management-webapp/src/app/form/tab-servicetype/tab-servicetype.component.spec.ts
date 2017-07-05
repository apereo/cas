/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabServicetypeComponent } from './tab-servicetype.component';
import {ServicetypeComponent} from "../servicetype/servicetype.component";
import {Messages} from "../../messages";
import {TabService} from "../tab.service";
import {SamlclientComponent} from "../samlclient/samlclient.component";
import {OauthclientComponent} from "../oauthclient/oauthclient.component";

describe('TabServicetypeComponent', () => {
  let component: TabServicetypeComponent;
  let fixture: ComponentFixture<TabServicetypeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule ],
      declarations: [ TabServicetypeComponent, ServicetypeComponent, SamlclientComponent, OauthclientComponent ],
      providers: [ Messages, TabService ]
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
