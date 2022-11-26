package coolBank.coolBank.account.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private int balance;

    @Builder
    public AccountResponse(Long id, String accountNumber, int balance) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
}
