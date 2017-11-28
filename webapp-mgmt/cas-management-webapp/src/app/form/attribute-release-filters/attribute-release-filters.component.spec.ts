import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeReleaseFiltersComponent } from './attribute-release-filters.component';
import {SharedModule} from '../../shared/shared.module';
import {FormsModule} from '@angular/forms';
import {Messages} from '../../messages';

describe('AttributeReleaseFiltersComponent', () => {
  let component: AttributeReleaseFiltersComponent;
  let fixture: ComponentFixture<AttributeReleaseFiltersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, SharedModule],
      declarations: [ AttributeReleaseFiltersComponent ],
      providers: [ Messages ]
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
