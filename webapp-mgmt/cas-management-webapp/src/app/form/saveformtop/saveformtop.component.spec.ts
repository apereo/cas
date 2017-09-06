/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Location } from "@angular/common";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { SaveformtopComponent } from './saveformtop.component';
import {Messages} from "../../messages";
import {Data} from "../data";
import {SharedModule} from "../../shared/shared.module";
import {FormModule} from "../form.module";
import {FormsModule} from "@angular/forms";

let stubLocation = {
  path() {
    return "";
  }
}

describe('SaveformtopComponent', () => {
  let component: SaveformtopComponent;
  let fixture: ComponentFixture<SaveformtopComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ SaveformtopComponent ],
      providers: [
        Messages,
        {provide: Location, useValue: stubLocation},
        Data
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SaveformtopComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
