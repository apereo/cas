import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributeReleasePrincipalRepoComponent } from './attribute-release-principal-repo.component';

describe('AttributeReleasePrincipalRepoComponent', () => {
  let component: AttributeReleasePrincipalRepoComponent;
  let fixture: ComponentFixture<AttributeReleasePrincipalRepoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AttributeReleasePrincipalRepoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AttributeReleasePrincipalRepoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
