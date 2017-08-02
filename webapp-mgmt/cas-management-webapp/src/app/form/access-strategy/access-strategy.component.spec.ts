/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { AccessStrategyComponent } from './access-strategy.component';
import {RejectedattributesComponent} from "../rejectedattributes/rejectedattributes.component";
import {Messages} from "../../messages";
import {FormData} from "../../../domain/service-view-bean";
import {TabService} from "../tab.service";
import {SharedModule} from "../../shared/shared.module";
import {RegexRegisteredService} from "../../../domain/registered-service";

describe('AccessStrategyComponent', () => {
  let component: AccessStrategyComponent;
  let fixture: ComponentFixture<AccessStrategyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ AccessStrategyComponent, RejectedattributesComponent ],
      providers: [
        Messages
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessStrategyComponent);
    component = fixture.componentInstance;
    component.formData = new FormData();
    component.service = new RegexRegisteredService();
    component.selectOptions = new TabService().selectOptions;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
