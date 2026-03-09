package krematos.model.nosql;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.Instant;

@Data
@Document(collection = "user_events")
public class UserEvent {

    @Id
    private String id;

    private String eventType;
    private Long userId;
    private Long productId;

    private Double priceAtEvent;
    private Integer quantity;

    private Instant timestamp;
}
