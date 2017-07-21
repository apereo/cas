/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { OauthclientComponent } from './oauthclient.component';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";
import {SharedModule} from "../../shared/shared.module";

describe('OauthclientComponent', () => {
  let component: OauthclientComponent;
  let fixture: ComponentFixture<OauthclientComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ OauthclientComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OauthclientComponent);
    component = fixture.componentInstance;
    component.serviceData = new Data();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
