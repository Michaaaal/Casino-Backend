package michal.malek.slots.repositories;

import michal.malek.slots.models.SlotsGameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlotsGameRepository extends JpaRepository<SlotsGameEntity, Long> {
    List<SlotsGameEntity> findAllByUserIdAndWinAmountGreaterThanAndIsDoneIsTrue(long userId, double winAmount);
}
