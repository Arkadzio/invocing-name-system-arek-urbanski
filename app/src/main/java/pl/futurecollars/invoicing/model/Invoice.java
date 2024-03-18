package pl.futurecollars.invoicing.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class Invoice {

  private int id;
  private LocalDate date;

  private Company buyer;
  private Company seller;

}