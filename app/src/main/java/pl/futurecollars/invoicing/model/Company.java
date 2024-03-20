package pl.futurecollars.invoicing.model;

import lombok.Data;

@Data
public class Company {

  private String taxIdentifications;
  private String address;
  private String name;

  public Company(String taxIdentifications, String address, String name) {
    this.taxIdentifications = taxIdentifications;
    this.address = address;
    this.name = name;
  }
}
