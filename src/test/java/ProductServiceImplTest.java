import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.example.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;


    @Test
    void testFindProductById_ProductExist() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
    }

    @Test
    void testFindProductById_ProductNotExist() {
        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.findProductById(1L);

        assertFalse(result.isEmpty());
    }
}
