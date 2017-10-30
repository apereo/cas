import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeReleasePoliciesComponent } from './attribute-release-policies.component';
import {FormData} from '../../../domain/form-data';
import {SharedModule} from '../../shared/shared.module';
import {FormModule} from '../form.module';
import {FormsModule} from '@angular/forms';
import {Messages} from '../../messages';
import {Data} from '../data';
import {RegexRegisteredService} from '../../../domain/registered-service';

describe('AttributeReleasePoliciesComponent', () => {
  let component: AttributeReleasePoliciesComponent;
  let fixture: ComponentFixture<AttributeReleasePoliciesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, SharedModule],
      declarations: [ AttributeReleasePoliciesComponent ],
      providers: [ Messages ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributeReleasePoliciesComponent);
    component = fixture.componentInstance;
    component.formData = new FormData();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
