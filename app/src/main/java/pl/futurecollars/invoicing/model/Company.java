package pl.futurecollars.invoicing.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Company {

  @ApiModelProperty(value = "Tax identification number", required = true, example = "123-123-11-11")
  private String taxIdentificationNumber;
  @ApiModelProperty(value = "Company address", required = true, example = "ul. Niewiarygodna 321, 00-001 Pcim")
  private String address;
  @ApiModelProperty(value = "Company name", required = true, example = "Nad nasza wsia przelecial meteoryt S.A.")
  private String name;

  public Company(String taxIdentificationNumber, String address, String name) {
    this.taxIdentificationNumber = taxIdentificationNumber;
    this.address = address;
    this.name = name;
  }
}
