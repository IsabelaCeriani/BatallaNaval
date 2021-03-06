package dpoi.BatallaNaval.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Shot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String shooterId;

    private int x;

    private int y;

    private boolean hit;

}
