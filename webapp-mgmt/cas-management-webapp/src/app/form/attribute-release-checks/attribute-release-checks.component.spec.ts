import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeReleaseChecksComponent } from './attribute-release-checks.component';
import {SharedModule} from '../../shared/shared.module';
import {FormsModule} from '@angular/forms';
import {Messages} from '../../messages';
import {RegexRegisteredService} from '../../../domain/registered-service';

describe('AttributeReleaseChecksComponent', () => {
  let component: AttributeReleaseChecksComponent;
  let fixture: ComponentFixture<AttributeReleaseChecksComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule],
      declarations: [ AttributeReleaseChecksComponent ],
      providers: [ Messages ]
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
