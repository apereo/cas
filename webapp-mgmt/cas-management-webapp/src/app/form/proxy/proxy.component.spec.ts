/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { ProxyComponent } from './proxy.component';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";

describe('ProxyComponent', () => {
  let component: ProxyComponent;
  let fixture: ComponentFixture<ProxyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule ],
      declarations: [ ProxyComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProxyComponent);
    component = fixture.componentInstance;
    component.serviceData = new Data();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
