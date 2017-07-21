/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from "@angular/forms";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabAccessstrategyComponent } from './tab-accessstrategy.component';
import {TabService} from "../tab.service";
import {AccessStrategyComponent} from "../access-strategy/access-strategy.component";
import {Messages} from "../../messages";
import {RejectedattributesComponent} from "../rejectedattributes/rejectedattributes.component";
import {SharedModule} from "../../shared/shared.module";

describe('TabAccessstrategyComponent', () => {
  let component: TabAccessstrategyComponent;
  let fixture: ComponentFixture<TabAccessstrategyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ TabAccessstrategyComponent, AccessStrategyComponent, RejectedattributesComponent ],
      providers: [ TabService, Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabAccessstrategyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
