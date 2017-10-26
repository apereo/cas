/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { ServicenameComponent } from './servicename.component';
import {Messages} from '../../messages';
import {SharedModule} from '../../shared/shared.module';
import {RegexRegisteredService} from '../../../domain/registered-service';

describe('ServicenameComponent', () => {
  let component: ServicenameComponent;
  let fixture: ComponentFixture<ServicenameComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ ServicenameComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServicenameComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
