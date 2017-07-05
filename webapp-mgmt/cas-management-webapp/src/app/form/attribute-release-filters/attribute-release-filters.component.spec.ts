import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeReleaseFiltersComponent } from './attribute-release-filters.component';

describe('AttributeReleaseFiltersComponent', () => {
  let component: AttributeReleaseFiltersComponent;
  let fixture: ComponentFixture<AttributeReleaseFiltersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AttributeReleaseFiltersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributeReleaseFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
