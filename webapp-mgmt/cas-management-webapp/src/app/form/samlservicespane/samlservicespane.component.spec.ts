import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SamlservicespaneComponent } from './samlservicespane.component';
import {FormModule} from "../form.module";
import {SharedModule} from "../../shared/shared.module";
import {FormsModule} from "@angular/forms";
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";

describe('SamlservicespaneComponent', () => {
  let component: SamlservicespaneComponent;
  let fixture: ComponentFixture<SamlservicespaneComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, SharedModule ],
      declarations: [ SamlservicespaneComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SamlservicespaneComponent);
    component = fixture.componentInstance;
    component.serviceData = new ServiceData();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
