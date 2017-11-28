/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { SamlclientComponent } from './samlclient.component';
import {Messages} from '../../messages';
import {SharedModule} from '../../shared/shared.module';
import {SamlservicespaneComponent} from '../samlservicespane/samlservicespane.component';
import {RegexRegisteredService} from '../../../domain/registered-service';
import {SamlRegisteredService} from '../../../domain/saml-service';

describe('SamlclientComponent', () => {
  let component: SamlclientComponent;
  let fixture: ComponentFixture<SamlclientComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ SamlclientComponent, SamlservicespaneComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SamlclientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
