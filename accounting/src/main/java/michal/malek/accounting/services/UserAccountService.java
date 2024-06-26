package michal.malek.accounting.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import michal.malek.accounting.exceptions.SuspiciousTransactionException;
import michal.malek.accounting.exceptions.UserAccountNotActiveException;
import michal.malek.accounting.exceptions.UserAccountNotFoundException;
import michal.malek.accounting.models.entities.*;
import michal.malek.accounting.repositories.PaycheckTransactionRepository;
import michal.malek.accounting.repositories.PaymentTransactionRepository;
import michal.malek.accounting.repositories.UserAccountRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final PayUService payUService;
    private final UserAccountRepository userAccountRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaycheckTransactionRepository paycheckTransactionRepository;

    public UserAccountDTO getUserAccountByHolderId(Long id){
        Optional<UserAccount> byUserId = userAccountRepository.findByUserId(id);
        if(byUserId.isPresent()){
            UserAccount userAccount = byUserId.get();
            return new UserAccountDTO(userAccount.getPhone(), userAccount.getEmail(), userAccount.getBankAccountNumber(), userAccount.getFirstName(),userAccount.getLastName());
        }
        return null;
    }

    public UserAccount createAccount(long userId , UserAccountDTO userAccountDTO){
        try {
            UserAccount userAccount= UserAccount.builder()
                    .userId(userId)
                    .bankAccountNumber(userAccountDTO.getBankAccountNumber())
                    .firstName(userAccountDTO.getFirstName())
                    .lastName(userAccountDTO.getLastName())
                    .phone(userAccountDTO.getPhone())
                    .email(userAccountDTO.getEmail())
                    .isActive(true) //TODO docelowo false, do implementacji system zatwierdzania np. przez przesylanie zdj dowodu
                    .balance(0)
                    .build();
                return userAccountRepository.saveAndFlush(userAccount);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format: " + userId);
        }
        return null;
    }

    public UserAccount updateAccount(long userId ,UserAccountDTO userAccountDTO){
        Optional<UserAccount> byUserId = userAccountRepository.findByUserId(userId);
        if(byUserId.isPresent()){
            UserAccount userAccount = byUserId.get();
            if(!userAccountDTO.getFirstName().equals(userAccount.getFirstName()))
                userAccount.setFirstName(userAccountDTO.getFirstName());

            if(!userAccountDTO.getLastName().equals(userAccount.getLastName()))
                userAccount.setLastName(userAccountDTO.getLastName());

            if(!userAccountDTO.getEmail().equals(userAccount.getEmail()))
                userAccount.setEmail(userAccountDTO.getEmail());

            if(!userAccountDTO.getPhone().equals(userAccount.getPhone()))
                userAccount.setPhone(userAccount.getPhone());

            if(!userAccountDTO.getBankAccountNumber().equals(userAccount.getBankAccountNumber()))
                userAccount.setBankAccountNumber(userAccount.getBankAccountNumber());

            return userAccountRepository.saveAndFlush(userAccount);
        }else
            return null;
    }

    public String rechargeAccount(long userId, long amount){

        Optional<UserAccount> byUserId = userAccountRepository.findByUserId(userId);

        if(byUserId.isPresent()){
            UserAccount userAccount = byUserId.get();

            if(!userAccount.isActive())
                throw new UserAccountNotActiveException();
            String order = payUService.createOrder(amount, userAccount);
            String orderId = payUService.getOrderId(order);

            System.out.println(order);

            JSONObject obj = new JSONObject(order);
            String redirectUri = obj.getString("redirectUri");

            PaymentTransaction paymentTransaction =
                    PaymentTransaction
                            .builder()
                            .userAccountId(userAccount.getId())
                            .payuId(orderId)
                            .isDone(false)
                            .date(new Date())
                            .amount((int) amount)
                            .build();

            paymentTransactionRepository.saveAndFlush(paymentTransaction);

            System.out.println(redirectUri);
            return redirectUri;
        }else
            throw new UserAccountNotFoundException("User Account not Found");
    }

    public UserAccountBalance getUserAccountBalance(long userId){
        Optional<UserAccount> byUserId = userAccountRepository.findByUserId(userId);
        return byUserId.map(userAccount -> new UserAccountBalance(userAccount.getBalance())).orElse(null);
    }

    public void checkRechargeTransaction(long userAccountHolderId)  {
        Optional<UserAccount> userAccount = userAccountRepository.findByUserId(userAccountHolderId);

        if(userAccount.isPresent()){

            if(!userAccount.get().isActive())
                throw new UserAccountNotActiveException();

            List<PaymentTransaction> paymentTransactions = paymentTransactionRepository.findAllByUserAccountIdAndIsDoneIsFalse(userAccount.get().getId());
            System.out.println(paymentTransactions.size());

            for(var elem : paymentTransactions){
                if(!elem.isDone()){
                    String orderStatus = payUService.checkOrderStatus(elem.getPayuId());
                    System.out.println(orderStatus);

                    switch (payUService.isPaymentGood(orderStatus, elem)) {
                        case 1 -> {
                            try {
                                this.updateAccountBalance(userAccount.get(), elem);
                            } catch (RuntimeException e) {
                                throw new RuntimeException("Failed to update Account balance");
                            }
                        }
                        case 0 -> {
                            this.paymentTransactionRepository.delete(elem);
                        }
                        case -1 -> {
                            this.blockUserAccount(userAccount.get());
                            throw new SuspiciousTransactionException("Suspicious Activity"); //TODO Admin notif
                        }

                    }
                }
            }
        }else throw new RuntimeException("Lack of finance-account");
    }

    private void blockUserAccount(UserAccount userAccount) {
        userAccount.setActive( false );
        userAccountRepository.saveAndFlush(userAccount);
    }


    @Transactional
    public void updateAccountBalance(UserAccount userAccount, PaymentTransaction paymentTransaction){
        paymentTransaction.setDone(true);
        paymentTransactionRepository.saveAndFlush(paymentTransaction);

        userAccount.setBalance(userAccount.getBalance() + paymentTransaction.getAmount());
        userAccountRepository.saveAndFlush(userAccount);
    }

    @Transactional
    public void updateAccountBalanceForGame(long userId, long amount){
        Optional<UserAccount> byUserId = userAccountRepository.findByUserId(userId);
        UserAccount userAccount = byUserId.orElse(null);
        if(userAccount!= null){
            userAccount.setBalance(userAccount.getBalance() + amount);
            userAccountRepository.saveAndFlush(userAccount);
        }else throw new UserAccountNotFoundException();
    }

    public boolean validateForSpin(long stake, long userId) {
        Optional<UserAccount> byUserId = userAccountRepository.findByUserId(userId);
        UserAccount userAccount = byUserId.orElse(null);
        if(userAccount != null && userAccount.isActive()){
            return userAccount.getBalance() >= stake;
        }else throw new UserAccountNotFoundException("or not active");
    }

    @Transactional
    public void paycheck(long userId, long amount) {
        Optional<UserAccount> byUserId = userAccountRepository.findByUserId(userId);



        if(byUserId.isPresent()){
            UserAccount userAccount = byUserId.get();
            if(userAccount.getBalance() < amount){
                this.blockUserAccount(userAccount);
                throw new SuspiciousTransactionException();
                //log
            }
            if(!userAccount.isActive()){
                throw new UserAccountNotActiveException();
                //log
            }

            PaycheckTransaction paycheckTransaction = PaycheckTransaction
                    .builder()
                    .userAccountId(Math.toIntExact(userAccount.getId()))
                    .amount(amount)
                    .date(new Date())
                    .isDone(false)
                    .build();

            userAccount.setBalance((userAccount.getBalance() - amount));
            userAccountRepository.saveAndFlush(userAccount);
            paycheckTransactionRepository.saveAndFlush(paycheckTransaction);

        }else throw new UserAccountNotFoundException("Lack of account");
    }

    public List<PaycheckTransaction> getPaycheckList(long userId){
        Optional<UserAccount> byUserId = userAccountRepository.findByUserId(userId);
        if(byUserId.isPresent()){
            List<PaycheckTransaction> allByUserAccountId = paycheckTransactionRepository.findAllByUserAccountId(Math.toIntExact(byUserId.get().getId()));
            if(allByUserAccountId.isEmpty()){
                return null;
            }

            allByUserAccountId.sort(new Comparator<PaycheckTransaction>() {
                @Override
                public int compare(PaycheckTransaction o1, PaycheckTransaction o2) {
                    return o2.getDate().compareTo(o1.getDate());
                }
            });

            return allByUserAccountId;
        }
        return null;
    }

    @Transactional
    public void deleteUserAccount(long userId){
        Optional<UserAccount> byUserId = userAccountRepository.findByUserId(userId);
        if(byUserId.isPresent()){
            userAccountRepository.delete(byUserId.get());
        }else throw new UserAccountNotFoundException();
    }
}

