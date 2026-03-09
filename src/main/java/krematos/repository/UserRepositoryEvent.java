package krematos.repository;

import krematos.model.nosql.UserEvent;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@Repository
public interface UserRepositoryEvent extends MongoRepository<UserEvent, String> {
    List<UserEvent> findByUserId(Long userId);

    List<UserEvent> findByProductIdAndEventType(Long productId, String eventType);
}
