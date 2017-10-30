/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { AttributeReleaseComponent } from './attribute-release.component';
import {Messages} from '../../messages';
import {SharedModule} from '../../shared/shared.module';
import {AttributeReleaseChecksComponent} from '../attribute-release-checks/attribute-release-checks.component';
import {AttributeReleasePrincipalRepoComponent} from '../attribute-release-principal-repo/attribute-release-principal-repo.component';
import {AttributeReleaseFiltersComponent} from '../attribute-release-filters/attribute-release-filters.component';
import {AttributeReleasePoliciesComponent} from '../attribute-release-policies/attribute-release-policies.component';
import {WsfedattrrelpoliciesComponent} from '../wsfedattrrelpolocies/wsfedattrrelpolicies.component';
import {RegexRegisteredService} from '../../../domain/registered-service';

describe('AttributeReleaseComponent', () => {
  let component: AttributeReleaseComponent;
  let fixture: ComponentFixture<AttributeReleaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        SharedModule
      ],
      declarations: [
        AttributeReleaseComponent,
        AttributeReleaseChecksComponent,
        AttributeReleasePrincipalRepoComponent,
        AttributeReleaseFiltersComponent,
        AttributeReleasePoliciesComponent,
        WsfedattrrelpoliciesComponent
      ],
      providers: [
        Messages
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributeReleaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
