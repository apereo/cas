/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { ServicedescComponent } from './servicedesc.component';
import {Messages} from "../../messages";
import {Data} from "../../../domain/form";
import {SharedModule} from "../../shared/shared.module";

describe('ServicedescComponent', () => {
  let component: ServicedescComponent;
  let fixture: ComponentFixture<ServicedescComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ ServicedescComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServicedescComponent);
    component = fixture.componentInstance;
    component.serviceData = new Data();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
