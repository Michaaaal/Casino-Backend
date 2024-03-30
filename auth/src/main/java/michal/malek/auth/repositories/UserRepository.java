package michal.malek.auth.repositories;

import michal.malek.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLogin(String login);
    @Query(nativeQuery = true, value = "SELECT * from users WHERE login=?1 and is_lock=false and is_enabled=true")
    Optional<User> findUserByLoginAndIsEnabledAndIsLock(String login);
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByUid(String uid);

}
