/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { MultiauthpaneComponent } from './multiauthpane.component';
import {Messages} from "../../messages";
import {TabService} from "../tab.service";
import {Data} from "../../../domain/form";
import {SharedModule} from "../../shared/shared.module";

describe('MultiauthpaneComponent', () => {
  let component: MultiauthpaneComponent;
  let fixture: ComponentFixture<MultiauthpaneComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ MultiauthpaneComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MultiauthpaneComponent);
    component = fixture.componentInstance;
    component.selectOptions = new TabService().selectOptions;
    component.serviceData = new Data();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
