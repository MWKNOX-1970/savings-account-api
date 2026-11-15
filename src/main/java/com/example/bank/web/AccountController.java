package com.example.bank.web;

import com.example.bank.action.CreateAccountAction;
import com.example.bank.action.GetAccountAction;
import com.example.bank.model.Account;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST endpoints for creating and fetching savings accounts.
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final CreateAccountAction createAccountAction;
    private final GetAccountAction getAccountAction;
    private final AccountResponseConverter accountResponseConverter;

    public AccountController(CreateAccountAction createAccountAction,
                             GetAccountAction getAccountAction,
                             AccountResponseConverter accountResponseConverter) {
        this.createAccountAction = createAccountAction;
        this.getAccountAction = getAccountAction;
        this.accountResponseConverter = accountResponseConverter;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        Account account = createAccountAction.execute(request.customerName(), request.accountNickName());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{accountNumber}")
                .buildAndExpand(account.accountNumber())
                .toUri();
        return ResponseEntity.created(location).body(accountResponseConverter.convert(account));
    }

    // TODO: the path uses the account number. Replace it with an obfuscated account id.
    @GetMapping("/{accountNumber}")
    public AccountResponse get(@PathVariable @Pattern(regexp = "\\d{12}", message = "accountNumber must be 12 digits") String accountNumber) {
        return accountResponseConverter.convert(getAccountAction.execute(accountNumber));
    }
}
