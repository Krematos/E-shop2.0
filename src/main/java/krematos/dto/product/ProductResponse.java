package krematos.dto.product;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResponse(Long id,

        @NotBlank(message = "Název produktu je povinný")
        @Size(min = 2, max = 100, message = "Název musí mít 2–100 znaků")
        String name,

        @NotBlank(message = "Popis produktu je povinný")
        @Size(max = 1000, message = "Popis nesmí překročit 1000 znaků")
        String description,

        @NotNull(message = "Cena je povinná")
        @Positive(message = "Cena musí být kladné číslo")
        @DecimalMin(value = "0.01", message = "Cena musí být větší než 0")
        @Digits(integer = 10, fraction = 2, message = "Neplatný formát ceny")
        BigDecimal price,

        String category,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        List<MultipartFile> imagesFilenames,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        List<String> images,

        Instant createdAt,
        Instant updatedAt) {


}
