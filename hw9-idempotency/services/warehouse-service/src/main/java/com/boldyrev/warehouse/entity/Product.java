package com.boldyrev.warehouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

  @Id private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Integer available;

  public Product(long id, String name, int available) {
    this.id = id;
    this.name = name;
    this.available = available;
  }
}
