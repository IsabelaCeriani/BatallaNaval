package dpoi.BatallaNaval.services;

import dpoi.BatallaNaval.controllers.dtos.GameRoomDTO;
import dpoi.BatallaNaval.exception.GameFullException;
import dpoi.BatallaNaval.exception.GameNotFoundException;
import dpoi.BatallaNaval.model.GameRoom;
import dpoi.BatallaNaval.respositories.GameRoomRepository;
import lombok.val;
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

    public GameRoomDTO getGame(UUID gameRoomId) {
        val gameRoom = gameRoomRepository.findById(gameRoomId);

        if (gameRoom.isPresent()) {
            return GameRoomDTO.builder()
                    .gameRoomId(gameRoom.get().getId())
                    .player1Id(gameRoom.get().getPlayer1Id())
                    .player2Id(gameRoom.get().getPlayer2Id())
                    .build();
        } else {
            throw new GameNotFoundException("Game not found with that id");
        }
    }

    public GameRoomDTO joinGame(UUID gameRoomId, String userId) {
        val gameRoomOptional = gameRoomRepository.findById(gameRoomId);

        if (gameRoomOptional.isPresent()) {
            if (gameRoomOptional.get().getPlayer2Id() == null) {
                val gameRoom= gameRoomOptional.get();
                gameRoom.setPlayer2Id(userId);
                gameRoomRepository.save(gameRoom);

                return gameRoom.toDTO();
            } else {
                throw new GameFullException("Game is full");
            }
        } else {
            throw new GameNotFoundException("Game not found with that id");
        }
    }
}
