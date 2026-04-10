package nhom17.OneShop.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SepayWebhookDTO {

    // Khớp chính xác với JSON
    @JsonProperty("content")
    private String content;

    @JsonProperty("transferType")
    private String transferType;

    @JsonProperty("transferAmount")
    private BigDecimal transferAmount;

    @JsonProperty("gateway")
    private String gateway;

    @JsonProperty("accountNumber")
    private String accountNumber;
}