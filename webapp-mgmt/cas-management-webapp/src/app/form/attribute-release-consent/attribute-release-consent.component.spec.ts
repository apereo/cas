import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeReleaseConsentComponent } from './attribute-release-consent.component';

describe('AttributeReleaseConsentComponent', () => {
  let component: AttributeReleaseConsentComponent;
  let fixture: ComponentFixture<AttributeReleaseConsentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AttributeReleaseConsentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributeReleaseConsentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
