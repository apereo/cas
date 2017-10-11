import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TabWsfedComponent } from './tab-wsfed.component';

describe('TabWsfedComponent', () => {
  let component: TabWsfedComponent;
  let fixture: ComponentFixture<TabWsfedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TabWsfedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabWsfedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
