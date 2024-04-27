package michal.malek.auth.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity(name = "reset_operations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetOperations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;
    private Timestamp createDate;
    private String uid;
}
