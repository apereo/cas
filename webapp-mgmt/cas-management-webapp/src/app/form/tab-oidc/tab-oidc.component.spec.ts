import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TabOIDCComponent } from './tab-oidc.component';

describe('TabOauthComponent', () => {
  let component: TabOIDCComponent;
  let fixture: ComponentFixture<TabOIDCComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TabOIDCComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabOIDCComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
