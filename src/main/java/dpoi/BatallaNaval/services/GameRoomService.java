package dpoi.BatallaNaval.services;

import dpoi.BatallaNaval.controllers.dtos.GameRoomDTO;
import dpoi.BatallaNaval.exception.GameFullException;
import dpoi.BatallaNaval.exception.GameNotFoundException;
import dpoi.BatallaNaval.model.GameRoom;
import dpoi.BatallaNaval.model.Position;
import dpoi.BatallaNaval.respositories.GameRoomRepository;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class GameRoomService {

    @Autowired
    private GameRoomRepository gameRoomRepository;

    public UUID createGame() {
        GameRoom gameRoom = GameRoom.builder()
                .positionsPlayer1(List.of())
                .positionsPlayer2(List.of())
                .shotsPlayer1(List.of())
                .shotsPlayer2(List.of())
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
            if (gameRoomOptional.get().getPlayer2Id() == null && !Objects.equals(gameRoomOptional.get().getPlayer1Id(), userId)) {
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

    public boolean hasPlayerOne(UUID gameRoomId) {
        val gameRoomOptional = gameRoomRepository.findById(gameRoomId);

        if (gameRoomOptional.isPresent()) {
            return gameRoomOptional.get().getPlayer1Id() != null;
        } else {
            throw new GameNotFoundException("Game not found with that id");
        }
    }

    public void setPlayerOne(UUID gameRoomId, String userId) {
        val gameRoomOptional = gameRoomRepository.findById(gameRoomId);

        if (gameRoomOptional.isPresent()) {
            val gameRoom = gameRoomOptional.get();
            gameRoom.setPlayer1Id(userId);
            gameRoomRepository.save(gameRoom);
        } else {
            throw new GameNotFoundException("Game not found with that id");
        }
    }

    public GameRoom setPositions(UUID gameRoomId, Integer[][] positions, String userId) {
        val gameRoomOptional = gameRoomRepository.findById(gameRoomId);

        if (gameRoomOptional.isPresent()) {
            val gameRoom = gameRoomOptional.get();
            if (gameRoom.getPlayer1Id().equals(userId)) {
                gameRoom.setPositionsPlayer1(createPositionList(positions));
                gameRoomRepository.save(gameRoom);
            } else if (gameRoom.getPlayer2Id().equals(userId)) {
                gameRoom.setPositionsPlayer2(createPositionList(positions));
                gameRoomRepository.save(gameRoom);
            } else {
                throw new GameNotFoundException("Game not found with that id");
            }
            return gameRoomRepository.save(gameRoom);
        } else {
            throw new GameNotFoundException("Game not found with that id");
        }
    }

    private List<Position> createPositionList(Integer[][] positions) {
        List<Position> positionList = new ArrayList<>();
        for (Integer[] position : positions) {
            positionList.add(Position.builder().x(position[0]).y(position[1]).build());
        }
            return positionList;
    }

}
