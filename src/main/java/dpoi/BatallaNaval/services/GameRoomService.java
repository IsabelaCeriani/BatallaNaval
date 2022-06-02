package dpoi.BatallaNaval.services;

import dpoi.BatallaNaval.model.GameRoom;
import dpoi.BatallaNaval.respositories.GameRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameRoomService {

    @Autowired
    private GameRoomRepository gameRoomRepository;

    public UUID createGame(String userId) {
        GameRoom gameRoom = GameRoom.builder()
                .player1Id(userId)
                .build();

        return gameRoomRepository.save(gameRoom).getId();
    }
}
