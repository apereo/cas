/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { PropertiespaneComponent } from './propertiespane.component';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";
import {SharedModule} from "../../shared/shared.module";

describe('PropertiespaneComponent', () => {
  let component: PropertiespaneComponent;
  let fixture: ComponentFixture<PropertiespaneComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ PropertiespaneComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PropertiespaneComponent);
    component = fixture.componentInstance;
    component.serviceData = new ServiceData;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
