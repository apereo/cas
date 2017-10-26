/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabProxyComponent } from './tab-proxy.component';
import {ProxyComponent} from '../proxy/proxy.component';
import {Data} from '../data';
import {Messages} from '../../messages';
import {SharedModule} from '../../shared/shared.module';

describe('TabProxyComponent', () => {
  let component: TabProxyComponent;
  let fixture: ComponentFixture<TabProxyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ TabProxyComponent, ProxyComponent ],
      providers: [ Messages, Data]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabProxyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
