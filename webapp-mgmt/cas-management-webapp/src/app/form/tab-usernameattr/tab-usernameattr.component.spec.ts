/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabUsernameattrComponent } from './tab-usernameattr.component';
import {Data} from '../data';
import {Messages} from '../../messages';
import {UidattrsComponent} from '../uidattrs/uidattrs.component';
import {SharedModule} from '../../shared/shared.module';

describe('TabUsernameattrComponent', () => {
  let component: TabUsernameattrComponent;
  let fixture: ComponentFixture<TabUsernameattrComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ TabUsernameattrComponent, UidattrsComponent ],
      providers: [ Messages, Data]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabUsernameattrComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
