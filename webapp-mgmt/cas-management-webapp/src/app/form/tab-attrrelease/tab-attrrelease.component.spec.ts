/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TabAttrreleaseComponent } from './tab-attrrelease.component';
import {AttributeReleaseComponent} from '../attribute-release/attribute-release.component';
import {Messages} from '../../messages';
import {Data} from '../data';
import {SharedModule} from '../../shared/shared.module';
import {AttributeReleaseChecksComponent} from '../attribute-release-checks/attribute-release-checks.component';
import {AttributeReleaseFiltersComponent} from '../attribute-release-filters/attribute-release-filters.component';
import {AttributeReleasePoliciesComponent} from '../attribute-release-policies/attribute-release-policies.component';
import {AttributeReleasePrincipalRepoComponent} from '../attribute-release-principal-repo/attribute-release-principal-repo.component';
import {WsfedattrrelpoliciesComponent} from '../wsfedattrrelpolocies/wsfedattrrelpolicies.component';

describe('TabAttrreleaseComponent', () => {
  let component: TabAttrreleaseComponent;
  let fixture: ComponentFixture<TabAttrreleaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [
        TabAttrreleaseComponent,
        AttributeReleaseComponent,
        AttributeReleaseChecksComponent,
        AttributeReleaseFiltersComponent,
        AttributeReleasePoliciesComponent,
        AttributeReleasePrincipalRepoComponent,
        WsfedattrrelpoliciesComponent
      ],
      providers: [ Messages, Data ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabAttrreleaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
