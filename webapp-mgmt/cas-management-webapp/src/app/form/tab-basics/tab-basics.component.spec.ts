/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabBasicsComponent } from './tab-basics.component';
import {Messages} from '../../messages';
import {Data} from '../data';
import {ServicenameComponent} from '../servicename/servicename.component';
import {ServicedescComponent} from '../servicedesc/servicedesc.component';
import {LogoComponent} from '../logo/logo.component';
import {ThemeidComponent} from '../themeid/themeid.component';
import {LinkrefsComponent} from '../linkrefs/linkrefs.component';
import {EnabledComponent} from '../enabled/enabled.component';
import {ServiceidComponent} from '../serviceid/serviceid.component';
import {SharedModule} from '../../shared/shared.module';

describe('TabBasicsComponent', () => {
  let component: TabBasicsComponent;
  let fixture: ComponentFixture<TabBasicsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [
        TabBasicsComponent,
        ServiceidComponent,
        ServicenameComponent,
        ServicedescComponent,
        LogoComponent,
        ThemeidComponent,
        LinkrefsComponent,
        EnabledComponent
      ],
      providers: [ Messages, Data ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabBasicsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
