package dpoi.BatallaNaval.model;

import dpoi.BatallaNaval.controllers.dtos.GameRoomDTO;
import dpoi.BatallaNaval.model.Shot;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String player1Id;

    private String player2Id;

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Shot> shotsPlayer1;

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Shot> shotsPlayer2;

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Position> positionsPlayer1;

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Position> positionsPlayer2;


    public GameRoomDTO toDTO() {
        return GameRoomDTO.builder()
                .gameRoomId(this.id)
                .player1Id(this.player1Id)
                .player2Id(this.player2Id)
                .build();
    }
}
