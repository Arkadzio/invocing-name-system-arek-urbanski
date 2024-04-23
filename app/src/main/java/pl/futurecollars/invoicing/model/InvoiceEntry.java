package pl.futurecollars.invoicing.model;

import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceEntry {

  @ApiModelProperty(value = "Product/service description", required = true, example = "Dell 321")
  private String description;
  @ApiModelProperty(value = "Number of items", required = true, example = "99")
  private int quantity;
  @ApiModelProperty(value = "Product/service netto price", required = true, example = "150.5")
  private BigDecimal price;
  @ApiModelProperty(value = "Product/service tax value", required = true, example = "15")
  private BigDecimal vatValue;
  @ApiModelProperty(value = "tax rate", required = true)
  private Vat vatRate;
}
