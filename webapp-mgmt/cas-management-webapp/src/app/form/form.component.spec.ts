/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from "@angular/router/testing";
import { ActivatedRoute } from "@angular/router";
import { Location } from "@angular/common";

import { FormComponent } from './form.component';
import {SaveformtopComponent} from "./saveformtop/saveformtop.component";
import {Messages} from "../messages";
import {FormService} from "./form.service";
import {Data} from "./data";
import {ActivatedRouteStub} from "../../testing/router-stub";
import {AbstractRegisteredService, RegexRegisteredService} from "../../domain/registered-service";

let formServicesStub = {
  getService(id: number): Promise<AbstractRegisteredService> {
    return Promise.resolve(new RegexRegisteredService());
  },

  saveService(service: AbstractRegisteredService): Promise<number> {
    return Promise.resolve(1);
  }
};

let userServiceStub = {
  getRoles(): Promise<String[]> {
    return Promise.resolve([]);
  },
  getPermissions(): Promise<String[]> {
    return Promise.resolve([]);
  }
};

let activatedRoute: ActivatedRouteStub = new ActivatedRouteStub();

let stubLocation = {
  path() {
    return "";
  }
}

describe('FormComponent', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<FormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule
      ],
      declarations: [ FormComponent, SaveformtopComponent ],
      providers: [
        Messages,
        Data,
        {provide: FormService, useValue: formServicesStub},
        {provide: ActivatedRoute, useValue: activatedRoute},
        {provide: Location, useValue: stubLocation}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    activatedRoute.testData = new RegexRegisteredService();
    activatedRoute.testParams = {id: -1};
    activatedRoute.testUrl = { path() { return "";}};
    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;
    component.ngOnInit = function() { };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
