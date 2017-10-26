/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabMulitauthComponent } from './tab-mulitauth.component';
import {MultiauthpaneComponent} from '../multiauthpane/multiauthpane.component';
import {Data} from '../data';
import {Messages} from '../../messages';
import {SharedModule} from '../../shared/shared.module';

describe('TabMulitauthComponent', () => {
  let component: TabMulitauthComponent;
  let fixture: ComponentFixture<TabMulitauthComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ TabMulitauthComponent, MultiauthpaneComponent ],
      providers: [ Messages, Data]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabMulitauthComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
