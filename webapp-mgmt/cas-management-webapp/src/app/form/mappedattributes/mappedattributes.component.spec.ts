import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MappedattributesComponent } from './mappedattributes.component';

describe('MappedattributesComponent', () => {
  let component: MappedattributesComponent;
  let fixture: ComponentFixture<MappedattributesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MappedattributesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MappedattributesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
