import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TabOauthComponent } from './tab-oauth.component';

describe('TabOauthComponent', () => {
  let component: TabOauthComponent;
  let fixture: ComponentFixture<TabOauthComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TabOauthComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabOauthComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
