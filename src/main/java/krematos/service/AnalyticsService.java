package krematos.service;

import krematos.model.nosql.UserEvent;
import krematos.repository.UserRepositoryEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@AllArgsConstructor
@Service
public class AnalyticsService {

    private final UserRepositoryEvent userRepositoryEvent;

    public void logProductView(Long userId, Long productId) {
        UserEvent event = new UserEvent();
        event.setUserId(userId);
        event.setProductId(productId);
        event.setEventType("VIEW");
        event.setTimestamp(Instant.now());

        userRepositoryEvent.save(event);
    }

        public void logProductPurchase(Long userId, Long productId, Double price, Integer quantity) {
            UserEvent event = new UserEvent();
            event.setUserId(userId);
            event.setProductId(productId);
            event.setPriceAtEvent(price);
            event.setQuantity(quantity);
            event.setEventType("PURCHASE");
            event.setTimestamp(Instant.now());

            userRepositoryEvent.save(event);
        }

}
