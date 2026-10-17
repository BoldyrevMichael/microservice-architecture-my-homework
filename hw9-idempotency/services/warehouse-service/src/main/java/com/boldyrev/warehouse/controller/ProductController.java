package com.boldyrev.warehouse.controller;

import com.boldyrev.warehouse.dto.ProductResponse;
import com.boldyrev.warehouse.service.StockService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/warehouse/products")
public class ProductController {

  private final StockService stockService;

  @Autowired
  public ProductController(StockService stockService) {
    this.stockService = stockService;
  }

  @GetMapping
  public List<ProductResponse> list() {
    return stockService.list();
  }

  @GetMapping("/{id}")
  public ProductResponse get(@PathVariable long id) {
    return stockService.get(id);
  }
}
