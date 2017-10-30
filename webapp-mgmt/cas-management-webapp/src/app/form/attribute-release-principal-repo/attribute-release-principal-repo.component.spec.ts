import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeReleasePrincipalRepoComponent } from './attribute-release-principal-repo.component';
import {SharedModule} from '../../shared/shared.module';
import {FormsModule} from '@angular/forms';
import {Messages} from '../../messages';
import {FormData} from '../../../domain/form-data';

describe('AttributeReleasePrincipalRepoComponent', () => {
  let component: AttributeReleasePrincipalRepoComponent;
  let fixture: ComponentFixture<AttributeReleasePrincipalRepoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, SharedModule],
      declarations: [ AttributeReleasePrincipalRepoComponent ],
      providers: [Messages]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributeReleasePrincipalRepoComponent);
    component = fixture.componentInstance;
    component.formData = new FormData;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
