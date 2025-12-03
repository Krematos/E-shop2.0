package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {
    private Long id;

    @NotBlank(message = "Název produktu je povinný")
    @Size(min = 2, max = 100, message = "Název musí mít 2–100 znaků")
    private String name;

    @NotBlank(message = "Popis produktu je povinný")
    @Size(max = 1000, message = "Popis nesmí překročit 1000 znaků")
    private String description;

    @NotNull(message = "Cena je povinná")
    @Positive(message = "Cena musí být kladné číslo")
    @DecimalMin(value = "0.01", message = "Cena musí být větší než 0")
    @Digits(integer = 10, fraction = 2, message = "Neplatný formát ceny")
    private BigDecimal price;

    private String category;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<MultipartFile> imagesFilenames;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> images;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
