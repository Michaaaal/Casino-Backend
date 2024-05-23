package michal.malek.accounting.repositories;

import michal.malek.accounting.models.entities.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findAllByUserAccountId(Long userAccountId);
    List<PaymentTransaction> findAllByUserAccountIdAndIsDoneIsFalse(Long userAccountId);
}
