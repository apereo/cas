/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabProxyComponent } from './tab-proxy.component';
import {ProxyComponent} from "../proxy/proxy.component";
import {TabService} from "../tab.service";
import {Messages} from "../../messages";

describe('TabProxyComponent', () => {
  let component: TabProxyComponent;
  let fixture: ComponentFixture<TabProxyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule ],
      declarations: [ TabProxyComponent, ProxyComponent ],
      providers: [ Messages, TabService]
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
