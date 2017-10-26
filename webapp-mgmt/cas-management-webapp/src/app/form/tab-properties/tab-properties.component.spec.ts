/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabPropertiesComponent } from './tab-properties.component';
import {PropertiespaneComponent} from '../propertiespane/propertiespane.component';
import {Messages} from '../../messages';
import {Data} from '../data';
import {SharedModule} from '../../shared/shared.module';

describe('TabPropertiesComponent', () => {
  let component: TabPropertiesComponent;
  let fixture: ComponentFixture<TabPropertiesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ TabPropertiesComponent, PropertiespaneComponent ],
      providers: [ Messages, Data ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabPropertiesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
