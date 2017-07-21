/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { ServiceidComponent } from './serviceid.component';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";
import {SharedModule} from "../../shared/shared.module";

describe('ServiceidComponent', () => {
  let component: ServiceidComponent;
  let fixture: ComponentFixture<ServiceidComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ ServiceidComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceidComponent);
    component = fixture.componentInstance;
    component.serviceData = new Data();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
