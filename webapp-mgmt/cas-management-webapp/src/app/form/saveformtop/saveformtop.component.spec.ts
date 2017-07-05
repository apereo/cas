/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Location } from "@angular/common";
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { SaveformtopComponent } from './saveformtop.component';
import {Messages} from "../../messages";
import {TabService} from "../tab.service";

let stubLocation = {

}

describe('SaveformtopComponent', () => {
  let component: SaveformtopComponent;
  let fixture: ComponentFixture<SaveformtopComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SaveformtopComponent ],
      providers: [
        Messages,
        {provide: Location, useValue: stubLocation},
        TabService
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
