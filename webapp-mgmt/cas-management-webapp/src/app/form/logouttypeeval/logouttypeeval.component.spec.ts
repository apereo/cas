/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { LogouttypeevalComponent } from './logouttypeeval.component';
import {Messages} from '../../messages';
import {Data} from '../data';
import {SharedModule} from '../../shared/shared.module';
import {RegexRegisteredService} from '../../../domain/registered-service';

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
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
