/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabAttrreleaseComponent } from './tab-attrrelease.component';
import {AttributeReleaseComponent} from "../attribute-release/attribute-release.component";
import {Messages} from "../../messages";
import {TabService} from "../tab.service";

describe('TabAttrreleaseComponent', () => {
  let component: TabAttrreleaseComponent;
  let fixture: ComponentFixture<TabAttrreleaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule ],
      declarations: [ TabAttrreleaseComponent, AttributeReleaseComponent ],
      providers: [ Messages, TabService ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabAttrreleaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
