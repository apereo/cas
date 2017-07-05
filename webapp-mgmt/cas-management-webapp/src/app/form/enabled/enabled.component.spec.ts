/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { EnabledComponent } from './enabled.component';
import {Data} from "../../../domain/form";
import {Messages} from "../../messages";

describe('EnabledComponent', () => {
  let component: EnabledComponent;
  let fixture: ComponentFixture<EnabledComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule ],
      declarations: [ EnabledComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EnabledComponent);
    component = fixture.componentInstance;
    component.serviceData = new Data();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
