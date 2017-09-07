import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TabSamlComponent } from './tab-saml.component';

describe('TabSamlComponent', () => {
  let component: TabSamlComponent;
  let fixture: ComponentFixture<TabSamlComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TabSamlComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabSamlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
