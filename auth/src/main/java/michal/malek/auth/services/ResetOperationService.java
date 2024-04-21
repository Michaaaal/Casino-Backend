package michal.malek.auth.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import michal.malek.auth.models.ResetOperations;
import michal.malek.auth.models.User;
import michal.malek.auth.repositories.ResetOperationsRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class ResetOperationService {

    private final ResetOperationsRepository resetOperationsRepository;

    @Transactional
    public ResetOperations initResetOperation(User user){
        ResetOperations resetOperations = new ResetOperations();

        resetOperations.setUid(UUID.randomUUID().toString());
        resetOperations.setUser(user);
        resetOperations.setCreateDate(new Timestamp(System.currentTimeMillis()));

        resetOperationsRepository.deleteAllByUser(user);

        return resetOperationsRepository.saveAndFlush(resetOperations);
    }

    public void endOperation(String uid){
        resetOperationsRepository.findByUid(uid).ifPresent(resetOperationsRepository::delete);
    }

    @Scheduled(cron = "0 0/10 * * * *")
    protected void deleteExpiredOperation(){
        List<ResetOperations> allExpiredResetOperations = resetOperationsRepository.findAllExpiredResetOperations();
        if(allExpiredResetOperations != null && !allExpiredResetOperations.isEmpty()){
            resetOperationsRepository.deleteAll(allExpiredResetOperations);
        }
    }

}
