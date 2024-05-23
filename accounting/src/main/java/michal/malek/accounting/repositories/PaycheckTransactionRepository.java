package michal.malek.accounting.repositories;

import michal.malek.accounting.models.entities.PaycheckTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaycheckTransactionRepository extends JpaRepository<PaycheckTransaction, Long> {

    List<PaycheckTransaction> findAllByUserAccountId(int userAccountId);
}
