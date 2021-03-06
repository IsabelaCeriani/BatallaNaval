package dpoi.BatallaNaval.services;

import dpoi.BatallaNaval.controllers.dtos.GameRoomDTO;
import dpoi.BatallaNaval.controllers.dtos.UserDTO;
import dpoi.BatallaNaval.exception.GameNotFoundException;
import dpoi.BatallaNaval.model.GameRoom;
import dpoi.BatallaNaval.model.Position;
import dpoi.BatallaNaval.model.Shot;
import dpoi.BatallaNaval.respositories.GameRoomRepository;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class GameRoomService {

    @Autowired
    private GameRoomRepository gameRoomRepository;
    @Autowired
    UserService userService;
    private final int[] boatSizes = {5,4,3,3,2};
    private final String[] boatDirections = {"HORIZONTAL RIGHT","HORIZONTAL LEFT","VERTICAL UP","VERTICAL DOWN"};

    public UUID createGame() {
        GameRoom gameRoom = GameRoom.builder()
                .positionsPlayer1(List.of())
                .positionsPlayer2(List.of())
                .shotsPlayer1(List.of())
                .shotsPlayer2(List.of())
                .build();

        return gameRoomRepository.save(gameRoom).getId();
    }

    public GameRoomDTO getGameDTO(UUID gameRoomId) {
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

    public void setPositions(UUID gameRoomId, Integer[][] positions, String userId) {
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
                throw new GameNotFoundException("User not found in game");
            }
        } else {
            throw new GameNotFoundException("Game not found with that id");
        }
    }


    public void setRandomPositions(UUID gameRoomId, String userId) {
        val game= getGameRoom(gameRoomId);

        if (game.getPlayer1Id().equals(userId)) {
            List<Position> positions = createRandomPositionList();
            if(game.getPositionsPlayer1().isEmpty()) {
                game.setPositionsPlayer1(positions);
                gameRoomRepository.save(game);
            }
            //game.setPositionsPlayer1(createRandomPositionList());
            //gameRoomRepository.save(game);
        } else if (game.getPlayer2Id().equals(userId)) {
            List<Position> positions = createRandomPositionList();
            if(game.getPositionsPlayer2().isEmpty()) {
                game.setPositionsPlayer2(positions);
                gameRoomRepository.save(game);
            }
            //game.setPositionsPlayer2(createRandomPositionList());
            //gameRoomRepository.save(game);
        } else {
            throw new GameNotFoundException("User not found in game");
        }
    }

    public List<Position> createRandomPositionList() {
        Random random = new Random();
        List<Position> positionList = new ArrayList<>();

        for(int i = 0; i < boatSizes.length; i++) {
            val boatSize = boatSizes[i];
            while (true) {
                val x = random.nextInt(10);
                val y = random.nextInt(10);
                val direction = boatDirections[random.nextInt(4)];
                System.out.println(x + " " + y + " " + direction);
                if (isValidPosition(x, y, boatSize, direction, positionList)) {
                    Integer[][] positions= createPositionsArray(x, y, boatSize, direction);
                    positionList.addAll(createPositionList(positions));
                    break;
                }
            }
        }
        return positionList;
    }


    private Integer[][] createPositionsArray(int x, int y, int boatSize, String direction) {
        Integer[][] positions = new Integer[boatSize][2];
        for (int i = 0; i < boatSize; i++) {
            if (direction.equals("HORIZONTAL RIGHT")) {
                positions[i][0] = x + i;
                positions[i][1] = y;
                //positions[x + i,y] = 1;
            } else if (direction.equals("HORIZONTAL LEFT")) {
                positions[i][0] = x - i;
                positions[i][1] = y;
                //positions[x - i][y] = 1;
            } else if (direction.equals("VERTICAL UP")) {
                positions[i][0] = x;
                positions[i][1] = y + i;
                //positions[x][y + i] = 1;
            } else if (direction.equals("VERTICAL DOWN")) {
                positions[i][0] = x;
                positions[i][1] = y - i;
                //positions[x][y - i] = 1;
            }
        }
        return positions;
    }

    private boolean isValidPosition(int x, int y, int boatSize, String direction, List<Position> positionList) {
        if(x < 0 || y < 0) return false;
        switch (direction) {
            case "HORIZONTAL RIGHT" -> {
                if (x + boatSize > 10) {
                    return false;
                }
                for (int i = 0; i < boatSize; i++) {
                    int finalX = x + i;
                    if (finalX < 0) return false;
                    if (positionList.stream().anyMatch(p -> p.getX() == finalX && p.getY() == y)) {
                        return false;
                    }
                }
                return true;
            }
            case "HORIZONTAL LEFT" -> {
                if (x - boatSize < 0) {
                    return false;
                }
                for (int i = 0; i < boatSize; i++) {
                    int finalX = x - i;
                    if (positionList.stream().anyMatch(p -> p.getX() == finalX && p.getY() == y)) {
                        return false;
                    }
                }
                return true;
            }
            case "VERTICAL UP" -> {
                if (y + boatSize > 10) {
                    return false;
                }
                for (int i = 0; i < boatSize; i++) {
                    int finalY = y + i;
                    if(finalY < 0) return false;
                    if (positionList.stream().anyMatch(p -> p.getX() == x && p.getY() == finalY)) {
                        return false;
                    }
                }
                return true;
            }
            case "VERTICAL DOWN" -> {
                if (y - boatSize < 0) {
                    return false;
                }
                for (int i = 0; i < boatSize; i++) {
                    int finalI = i;
                    if (positionList.stream().anyMatch(p -> p.getX() == x && p.getY() == y - finalI)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private List<Position> createPositionList(Integer[][] positions) {
        List<Position> positionList = new ArrayList<>();
        for (Integer[] position : positions) {
            positionList.add(Position.builder().x(position[0]).y(position[1]).build());
        }
        return positionList;
    }



    public Shot shoot(UUID gameRoomId, String shooterId, int x, int y) throws Exception {
        val gameRoomOptional = gameRoomRepository.findById(gameRoomId);

        if (gameRoomOptional.isPresent()) {
            val gameRoom = gameRoomOptional.get();
            if(shotExists(gameRoom, shooterId, x, y)) {
                throw new Exception("Shot already exists");
            }
            if (gameRoom.getPlayer1Id().equals(shooterId)) {
                val shot = Shot.builder()
                        .shooterId(shooterId)
                        .x(x)
                        .y(y)
                        .build();
                // check if the shot hits a position in player 2
                val position = gameRoom.getPositionsPlayer2().stream()
                        .filter(p -> p.getX()==(x) && p.getY()==(y))
                        .findFirst();
                shot.setHit(position.isPresent());
                gameRoom.getShotsPlayer1().add(shot);
                gameRoomRepository.save(gameRoom);
                changeTurn(gameRoomId);
                return shot;
            } else if (gameRoom.getPlayer2Id().equals(shooterId)) {
                val shot = Shot.builder()
                        .shooterId(shooterId)
                        .x(x)
                        .y(y)
                        .build();

                // check if the shot hits a position in player 1
                val position = gameRoom.getPositionsPlayer1().stream()
                        .filter(p -> p.getX()==(x) && p.getY()==(y))
                        .findFirst();
                shot.setHit(position.isPresent());
                gameRoom.getShotsPlayer2().add(shot);
                gameRoomRepository.save(gameRoom);
                changeTurn(gameRoomId);
                return shot;
            } else {
                throw new GameNotFoundException("Game not found with that that player");
            }
        } else {
            throw new GameNotFoundException("Game not found with that id");
        }
    }

    private boolean shotExists(GameRoom gameRoom, String shooterId, int x, int y) {
        if (gameRoom.getPlayer1Id().equals(shooterId)) {
            return gameRoom.getShotsPlayer1().stream()
                    .anyMatch(s -> s.getX() == x && s.getY() == y);
        } else if (gameRoom.getPlayer2Id().equals(shooterId)) {
            return gameRoom.getShotsPlayer2().stream()
                    .anyMatch(s -> s.getX() == x && s.getY() == y);
        } else {
            return false;
        }
    }

    private void changeTurn(UUID gameRoomId) {
        val game = getGameRoom(gameRoomId);
        if (game.getPlayerToShoot().equals(game.getPlayer1Id())) {
            game.setPlayerToShoot(game.getPlayer2Id());
        } else {
            game.setPlayerToShoot(game.getPlayer1Id());
        }
        gameRoomRepository.save(game);
    }

    public GameRoom getGameRoom(UUID gameRoomId) {

        val gameRoomOptional = gameRoomRepository.findById(gameRoomId);

        if (gameRoomOptional.isPresent()) {
            return gameRoomOptional.get();
        } else {
            throw new GameNotFoundException("Game not found with that id");
        }
    }

    public boolean isPlayerOneTurn(UUID gameRoomId) {
        val game= getGameRoom(gameRoomId);
        return game.getPlayerToShoot().equals(game.getPlayer1Id());

    }

    public boolean gameIsFull(UUID gameRoomId) {
        val game= getGameRoom(gameRoomId);
        return game.getPlayer1Id() != null && game.getPlayer2Id() != null;

    }

    public boolean playerBelongsToGame(UUID gameRoomId, String userId) {
        val game= getGameRoom(gameRoomId);
        if(gameIsFull(gameRoomId)){
            return game.getPlayer1Id().equals(userId) || game.getPlayer2Id().equals(userId);
        }else{
            if(game.getPlayer1Id()!= null){
                return game.getPlayer1Id().equals(userId);
            }
            return false;
        }
    }

    public boolean boardsAreReady(UUID gameRoomId) {
        val game= getGameRoom(gameRoomId);
        return !game.getPositionsPlayer1().isEmpty()&&!game.getPositionsPlayer2().isEmpty();
    }

    public boolean playerWon(UUID gameRoomId, String playerId) {
        val game= getGameRoom(gameRoomId);

        if (game.getPlayer1Id().equals(playerId)) {
            return game.getShotsPlayer1().stream().filter(Shot::isHit).count() == 17;
        } else if (game.getPlayer2Id().equals(playerId)) {
            return game.getShotsPlayer2().stream().filter(Shot::isHit).count() == 17;
        } else {
            throw new GameNotFoundException("Game not found with that that player");
        }
    }

    public boolean isPlayerOne(UUID gameRoomId,String shooterId) {
        val game= getGameRoom(gameRoomId);
        return game.getPlayer1Id().equals(shooterId);
    }

    public boolean hasPlayerOne(UUID gameRoomId) {
        val game= getGameRoom(gameRoomId);
        return game.getPlayer1Id() != null;

    }

    public void setPlayerOne(UUID gameRoomId, String userId) {
        val game= getGameRoom(gameRoomId);
        game.setPlayer1Id(userId);
        game.setPlayerToShoot(userId);
        gameRoomRepository.save(game);
    }

    public void setPlayerTwo(UUID gameRoomId, String userId) {
        val game= getGameRoom(gameRoomId);
        System.out.println("llegue hasta aca 4");
        game.setPlayer2Id(userId);
        gameRoomRepository.save(game);
    }

    public boolean gameEnded(UUID gameRoomId) {
        val game= getGameRoom(gameRoomId);
        return game.isGameEnded();
    }

    public void endGame(UUID gameRoomId) {
        val game= getGameRoom(gameRoomId);
        game.setGameEnded(true);
        gameRoomRepository.save(game);
    }

    public boolean allPositionsSet(UUID gameRoomId) {
        val game= getGameRoom(gameRoomId);
        return !game.getPositionsPlayer1().isEmpty() && !game.getPositionsPlayer2().isEmpty();
    }

    public Shot shootRandom(UUID gameRoomId, String shooterId) {
        val random = new Random();
        val gameRoom= getGameRoom(gameRoomId);
        Shot shot;

        if (gameRoom.getPlayer1Id().equals(shooterId)) {
            shot = getRandomShot(shooterId, random, gameRoom.getShotsPlayer1(), gameRoom.getPositionsPlayer2());

            gameRoom.getShotsPlayer1().add(shot);
            gameRoomRepository.save(gameRoom);
            changeTurn(gameRoomId);
            return shot;

        } else if (gameRoom.getPlayer2Id().equals(shooterId)) {

            shot = getRandomShot(shooterId, random, gameRoom.getShotsPlayer2(), gameRoom.getPositionsPlayer1());

            gameRoom.getShotsPlayer2().add(shot);
            gameRoomRepository.save(gameRoom);
            changeTurn(gameRoomId);
            return shot;
        } else {
            throw new GameNotFoundException("Game not found with that that player");
        }

    }

    private Shot getRandomShot(String shooterId, Random random, List<Shot> shooterShots, List<Position> opponentsPositions) {
        Shot shot;
        while (true) {
            int x = random.nextInt(10);
            int y = random.nextInt(10);

            if(shooterShots.stream().noneMatch(s -> s.getX() == x && s.getY() == y)){
                boolean isHit = opponentsPositions.stream().anyMatch(p -> p.getX()==(x) && p.getY()==(y));
                shot = Shot.builder()
                        .shooterId(shooterId)
                        .x(x)
                        .y(y)
                        .hit(isHit)
                        .build();
                break;
            }
        }
        return shot;
    }

    public boolean alreadyHasPositions(UUID gameRoomId, String userId) {
        val game= getGameRoom(gameRoomId);
        if(game.getPlayer1Id().equals(userId)){
            return !game.getPositionsPlayer1().isEmpty();
        }else{
            return !game.getPositionsPlayer2().isEmpty();
        }
    }

    public void abandonGame(UUID gameRoomId, String exitedPlayerId) {
        val game= getGameRoom(gameRoomId);
        userService.updatePlayerStatics(game.getPlayer1Id(),game.getPlayer2Id());
        if(game.getPlayer1Id().equals(exitedPlayerId)){
            userService.updateWinnerStatics(game.getPlayer2Id());
        }else{
            userService.updateWinnerStatics(game.getPlayer1Id());
        };
        gameRoomRepository.save(game);
        endGame(gameRoomId);
    }

    public UserDTO getOpponent(UUID gameRoomId, String userId) {
        val game= getGameRoom(gameRoomId);
        if(isPlayerOne(gameRoomId,userId)){
            return userService.getUser(game.getPlayer2Id()).toDTO();
        }else{
            return userService.getUser(game.getPlayer1Id()).toDTO();
        }
    }
}
