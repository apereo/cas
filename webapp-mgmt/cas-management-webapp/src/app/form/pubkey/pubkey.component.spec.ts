/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { PubkeyComponent } from './pubkey.component';
import {Messages} from '../../messages';
import {SharedModule} from '../../shared/shared.module';
import {RegexRegisteredService} from '../../../domain/registered-service';

describe('PubkeyComponent', () => {
  let component: PubkeyComponent;
  let fixture: ComponentFixture<PubkeyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ PubkeyComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PubkeyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
