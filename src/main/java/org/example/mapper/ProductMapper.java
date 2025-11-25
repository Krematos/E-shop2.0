package org.example.mapper;

import org.example.model.Product;
import org.example.dto.ProductDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // 1. Převod Entity -> DTO (Pro odeslání na frontend)
    // Ignorujeme 'images' (MultipartFile), protože z databáze soubory nechodí.
    @Mapping(target = "images", ignore = true)
    ProductDto toDto(Product product);

    // 2. Převod DTO -> Entity (Při vytváření)
    // DŮLEŽITÉ: Ignorujeme 'images', protože soubory zpracováváme ručně v Service!
    // Ignorujeme 'currency' a 'active', pokud je nemáš v DTO (nebo je přidej do DTO).
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "currency", ignore = true) // Pokud to v DTO nemáš, ignoruj to
    @Mapping(target = "active", ignore = true)   // Active se obvykle nastavuje defaultně v Entitě
    Product toEntity(ProductDto dto);


    // 3. Update existujícího produktu
    @Mapping(target = "id", ignore = true)       // ID se při update nemění
    @Mapping(target = "images", ignore = true)   // Obrázky řešíme v Service
    @Mapping(target = "createdAt", ignore = true) // Datum vytvoření se nemění
    @Mapping(target = "updatedAt", ignore = true) // To řeší @UpdateTimestamp v entitě
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromDto(ProductDto dto, @MappingTarget Product product);
}
