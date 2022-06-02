package dpoi.BatallaNaval.model;

import lombok.*;

import javax.persistence.*;
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





}
