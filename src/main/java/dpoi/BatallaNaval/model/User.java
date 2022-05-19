package dpoi.BatallaNaval.model;

import dpoi.BatallaNaval.controllers.dtos.UserDTO;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    private UUID id;

    private String name;

    private String password;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }


}
