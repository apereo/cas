import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeReleaseChecksComponent } from './attribute-release-checks.component';

describe('AttributeReleaseChecksComponent', () => {
  let component: AttributeReleaseChecksComponent;
  let fixture: ComponentFixture<AttributeReleaseChecksComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AttributeReleaseChecksComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributeReleaseChecksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
