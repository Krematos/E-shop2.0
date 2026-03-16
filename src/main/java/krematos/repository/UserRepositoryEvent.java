package krematos.repository;

import krematos.dto.TrendingProductDTO;
import krematos.model.nosql.UserEvent;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@Repository
public interface UserRepositoryEvent extends MongoRepository<UserEvent, String> {
    List<UserEvent> findByUserId(Long userId);

    List<UserEvent> findByProductIdAndEventType(Long productId, String eventType);

    @Aggregation(pipeline = {
            "{ '$match': { 'eventType': 'product_view' } }",       // 1. Vyfiltruj jen zobrazení produktů
            "{ '$group': { '_id': '$productId', 'views': { '$sum': 1 } } }", // 2. Seskup podle productId a počítej (+1 za každý)
            "{ '$sort': { 'views': -1 } }",                        // 3. Seřaď sestupně (-1) podle počtu zobrazení
            "{ '$limit': ?5 }"
    })
    List<TrendingProductDTO> findTopTrendingProducts(int limit);
}
