import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeReleasePoliciesComponent } from './attribute-release-policies.component';

describe('AttributeReleasePoliciesComponent', () => {
  let component: AttributeReleasePoliciesComponent;
  let fixture: ComponentFixture<AttributeReleasePoliciesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AttributeReleasePoliciesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributeReleasePoliciesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
