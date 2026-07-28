package com.mtbs.catalog.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "theaters")
public class Theater extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "city_id")
  private City city;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String address;

  protected Theater() {
  }

  public Theater(City city, String name, String address) {
    this.city = city;
    this.name = name;
    this.address = address;
  }

  public City getCity() {
    return city;
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }
}
