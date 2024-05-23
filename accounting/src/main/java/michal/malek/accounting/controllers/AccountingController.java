package michal.malek.accounting.controllers;

import lombok.RequiredArgsConstructor;
import michal.malek.accounting.models.entities.*;
import michal.malek.accounting.models.response.AccountingResponse;
import michal.malek.accounting.services.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounting")
public class AccountingController {
    private final UserAccountService userAccountService;

    @PostMapping("/create-account")
    public ResponseEntity<?> createAccount(@RequestBody UserAccountDTO userAccountDTO){
        UserAccount account = userAccountService.createAccount(userAccountDTO);
        if(account!=null)
            return ResponseEntity.ok().body(account);
        return ResponseEntity.status(400).body(new AccountingResponse("UserAccount creation failed"));
    }

    @PostMapping("/get-account-balance")
    public ResponseEntity<?> getAccountBalance(@RequestBody UserAccountHolderId userAccountHolderId){
        UserAccountBalance userAccountBalance = userAccountService.getUserAccountBalance(userAccountHolderId);
        if(userAccountBalance != null){
            return ResponseEntity.ok(userAccountBalance);
        }
        return ResponseEntity.status(400).body("No Account");
    }

    @PostMapping("/validate-for-spin")
    public ResponseEntity<?> validateForSpin( @RequestBody BalanceUpdateDTO balanceUpdateDTO){
        long stake = balanceUpdateDTO.getAmount();
        try{
            boolean b = userAccountService.validateForSpin(stake, balanceUpdateDTO.getUserId());
            if(b)
                return ResponseEntity.ok(new AccountingResponse("SUCCESS"));
            return ResponseEntity.status(400).body(new AccountingResponse("Lack of founds"));
        }catch (Exception e){
            return ResponseEntity.status(400).body(new AccountingResponse(e.getMessage()));
        }
    }

    @PostMapping("/update-balance")
    public ResponseEntity<?> updateAccountBalance( @RequestBody BalanceUpdateDTO balanceUpdateDTO){
        System.out.println("UPDATING BALANCE FOR GAME");
        try{
            userAccountService.updateAccountBalanceForGame(balanceUpdateDTO);
            return ResponseEntity.ok(new AccountingResponse("SUCCESS"));
        }catch (RuntimeException e){
            return ResponseEntity.status(400).body(new AccountingResponse(e.getMessage()));
        }
    }

    @PostMapping("/check-transactions")
    public ResponseEntity<?> checkTransactions(@RequestBody UserAccountHolderId userAccountHolderId){
            System.out.println("CHECKING TRANSACTIONS");
        try{
            userAccountService.checkRechargeTransaction(userAccountHolderId);
            return ResponseEntity.ok(new AccountingResponse("SUCCESS"));
        }catch (RuntimeException e){
            return ResponseEntity.status(400).body(new AccountingResponse(e.getMessage()));
        }
    }

    @PatchMapping("/update-account-details")
    public ResponseEntity<?> updateAccountDetails(@RequestBody UserAccountDTO userAccountDTO){
        UserAccount account = userAccountService.updateAccount(userAccountDTO);
        if(account!=null)
            return ResponseEntity.ok().body(account);
        return ResponseEntity.status(400).body(new AccountingResponse("UserAccount update failed"));
    }

    @PostMapping("/get-account-details")
    public ResponseEntity<?> getAccountDetails(@RequestBody UserAccountHolderId userAccountHolderId){
        UserAccount account = userAccountService.getUserAccountByHolderId(userAccountHolderId.getUserId());
        if(account!=null)
            return ResponseEntity.ok().body(account);
        return ResponseEntity.status(400).body(new AccountingResponse("Lack of account"));
    }

    @PostMapping("/recharge")
    public ResponseEntity<?> recharge(@RequestBody BalanceUpdateDTO balanceUpdateDTO){
        try {
            String paymentLink = userAccountService.rechargeAccount(balanceUpdateDTO);
            return ResponseEntity.ok(new AccountingResponse(paymentLink));
        }catch (Exception e){
            return ResponseEntity.ok("Something went wrong " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @PostMapping("/paycheck")
    public ResponseEntity<?> paycheck(@RequestBody BalanceUpdateDTO balanceUpdateDTO){
        System.out.println("paycheckk!!");
        try {
            userAccountService.paycheck(balanceUpdateDTO);
            return ResponseEntity.ok(new AccountingResponse("SUCCESS"));
        }catch (Exception e){
            return ResponseEntity.ok("Something went wrong " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @PostMapping("/get-paycheck-transactions")
    public ResponseEntity<?> getPaycheckTransactions(@RequestBody UserAccountHolderId userAccountHolderId){
        try {
            List<PaycheckTransaction> paycheckList = userAccountService.getPaycheckList(userAccountHolderId);
            return ResponseEntity.ok(paycheckList);
        }catch (Exception e){
            return ResponseEntity.ok("Something went wrong " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }
    @PostMapping("/delete-user-account")
    public ResponseEntity<?> deleteUserAccount(@RequestBody UserAccountHolderId userAccountHolderId){
        System.out.println("delete");
        try {
            userAccountService.deleteUserAccount(userAccountHolderId);
            return ResponseEntity.ok(new AccountingResponse("SUCCESS"));
        }catch (Exception e){
            return ResponseEntity.ok("Something went wrong " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

}
