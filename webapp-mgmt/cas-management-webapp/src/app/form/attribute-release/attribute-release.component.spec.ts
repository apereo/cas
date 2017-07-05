/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { AttributeReleaseComponent } from './attribute-release.component';
import {Messages} from "../../messages";
import {FormData, Data} from "../../../domain/form";

describe('AttributeReleaseComponent', () => {
  let component: AttributeReleaseComponent;
  let fixture: ComponentFixture<AttributeReleaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
      ],
      declarations: [ AttributeReleaseComponent ],
      providers: [
        Messages
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributeReleaseComponent);
    component = fixture.componentInstance;
    component.formData = new FormData();
    component.serviceData = new Data();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
