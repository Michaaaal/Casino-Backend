package michal.malek.accounting.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import michal.malek.accounting.models.entities.*;
import michal.malek.accounting.models.response.AccountingResponse;
import michal.malek.accounting.services.UserAccountService;
import michal.malek.accounting.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounting")
public class AccountingController {
    private final UserAccountService userAccountService;
    private final UserService userService;

    @PostMapping("/create-account")
    public ResponseEntity<?> createAccount(HttpServletRequest request , @RequestBody UserAccountDTO userAccountDTO){
        long userId = userService.getUserId(request);
        if(userId == -1)
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");

        UserAccount account = userAccountService.createAccount(userId, userAccountDTO);
        if(account!=null)
            return ResponseEntity.ok().body(account);
        return ResponseEntity.status(400).body(new AccountingResponse("UserAccount creation failed"));
    }

    @GetMapping("/get-account-balance")
    public ResponseEntity<?> getAccountBalance(HttpServletRequest request){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        UserAccountBalance userAccountBalance = userAccountService.getUserAccountBalance(userId);
        if(userAccountBalance != null){
            return ResponseEntity.ok(userAccountBalance);
        }
        return ResponseEntity.status(400).body("No Account");
    }

    @GetMapping("/validate-for-spin")
    public ResponseEntity<?> validateForSpin(HttpServletRequest request, @RequestParam int stake){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        try{
            boolean b = userAccountService.validateForSpin(stake, userId);
            if(b)
                return ResponseEntity.ok(new AccountingResponse("SUCCESS"));
            return ResponseEntity.status(400).body(new AccountingResponse("Lack of founds"));
        }catch (Exception e){
            return ResponseEntity.status(400).body(new AccountingResponse(e.getMessage()));
        }
    }

    @GetMapping("/update-balance")
    public ResponseEntity<?> updateAccountBalance( HttpServletRequest request, @RequestParam long amount){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        try{
            userAccountService.updateAccountBalanceForGame(userId , amount);
            return ResponseEntity.ok(new AccountingResponse("SUCCESS"));
        }catch (RuntimeException e){
            return ResponseEntity.status(400).body(new AccountingResponse(e.getMessage()));
        }
    }

    @GetMapping("/check-transactions")
    public ResponseEntity<?> checkTransactions(HttpServletRequest request){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        try{
            userAccountService.checkRechargeTransaction(userId);
            return ResponseEntity.ok(new AccountingResponse("SUCCESS"));
        }catch (RuntimeException e){
            return ResponseEntity.status(400).body(new AccountingResponse(e.getMessage()));
        }
    }

    @PatchMapping("/update-account-details")
    public ResponseEntity<?> updateAccountDetails(HttpServletRequest request, @RequestBody UserAccountDTO userAccountDTO){
        long userId = userService.getUserId( request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        UserAccount account = userAccountService.updateAccount(userId,userAccountDTO);
        if(account!=null)
            return ResponseEntity.ok().body(account);
        return ResponseEntity.status(400).body(new AccountingResponse("UserAccount update failed"));
    }

    @GetMapping("/get-account-details")
    public ResponseEntity<?> getAccountDetails(HttpServletRequest request){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        UserAccountDTO account = userAccountService.getUserAccountByHolderId(userId);
        if(account!=null)
            return ResponseEntity.ok().body(account);
        return ResponseEntity.status(400).body(new AccountingResponse("Lack of account"));
    }

    @GetMapping("/recharge")
    public ResponseEntity<?> recharge(HttpServletRequest request, @RequestParam long amount){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        try {
            String paymentLink = userAccountService.rechargeAccount(userId, amount);
            return ResponseEntity.ok(new AccountingResponse(paymentLink));
        }catch (Exception e){
            return ResponseEntity.ok("Something went wrong " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @GetMapping("/paycheck")
    public ResponseEntity<?> paycheck(HttpServletRequest request, @RequestParam long amount){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        try {
            userAccountService.paycheck(userId,amount);
            return ResponseEntity.ok(new AccountingResponse("SUCCESS"));
        }catch (Exception e){
            return ResponseEntity.ok("Something went wrong " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @GetMapping("/get-paycheck-transactions")
    public ResponseEntity<?> getPaycheckTransactions(HttpServletRequest request){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        try {
            List<PaycheckTransaction> paycheckList = userAccountService.getPaycheckList(userId);
            return ResponseEntity.ok(paycheckList);
        }catch (Exception e){
            return ResponseEntity.ok("Something went wrong " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @DeleteMapping("/delete-user-account")
    public ResponseEntity<?> deleteUserAccount(HttpServletRequest request){
        long userId = userService.getUserId(request);
        if(userId == -1){
            return ResponseEntity.status(401).body("Cannot get Claims from Jwt Token");
        }

        try {
            userAccountService.deleteUserAccount(userId);
            return ResponseEntity.ok(new AccountingResponse("SUCCESS"));
        }catch (Exception e){
            return ResponseEntity.ok("Something went wrong " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

}
