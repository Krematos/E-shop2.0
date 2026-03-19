package krematos.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import krematos.dto.EventRequest;
import krematos.dto.TrendingProductDTO;
import krematos.service.AnalyticsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytika", description = "API pro logování událostí a získávání analytických dat (zobrazení, nákupy, trendy produkty)")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/view")
    public ResponseEntity<EventRequest> logView(@RequestBody EventRequest request) {
         analyticsService.logProductView(request.userId(), request.productId());
         return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase")
    public ResponseEntity<EventRequest> logPurchase(@RequestBody EventRequest request) {
         analyticsService.logProductPurchase(request.userId(), request.productId(), request.price(), request.quantity());
         return ResponseEntity.ok().build();
     }
}
