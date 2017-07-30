/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { LogouttypeevalComponent } from './logouttypeeval.component';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";
import {TabService} from "../tab.service";
import {SharedModule} from "../../shared/shared.module";

describe('LogouttypeevalComponent', () => {
  let component: LogouttypeevalComponent;
  let fixture: ComponentFixture<LogouttypeevalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ LogouttypeevalComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LogouttypeevalComponent);
    component = fixture.componentInstance;
    component.serviceData = new ServiceData();
    component.selectOptions = new TabService().selectOptions;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
