package dpoi.BatallaNaval.respositories;

import dpoi.BatallaNaval.model.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, UUID> {
}
