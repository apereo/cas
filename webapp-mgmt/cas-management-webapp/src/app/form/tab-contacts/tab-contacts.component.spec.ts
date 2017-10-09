import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TabContactsComponent } from './tab-contacts.component';

describe('TabContactsComponent', () => {
  let component: TabContactsComponent;
  let fixture: ComponentFixture<TabContactsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TabContactsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabContactsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
