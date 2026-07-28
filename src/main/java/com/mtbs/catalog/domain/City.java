package com.mtbs.catalog.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cities")
public class City extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String state;

  protected City() {
  }

  public City(String name, String state) {
    this.name = name;
    this.state = state;
  }

  public String getName() {
    return name;
  }

  public String getState() {
    return state;
  }
}
