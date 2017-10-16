import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TabExpirationComponent } from './tab-expiration.component';

describe('TabExpirationComponent', () => {
  let component: TabExpirationComponent;
  let fixture: ComponentFixture<TabExpirationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TabExpirationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabExpirationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
