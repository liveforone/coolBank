package coolBank.coolBank.account.dto;

import coolBank.coolBank.account.model.Category;
import coolBank.coolBank.member.model.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountRequest {

    private Long id;
    private String accountNumber;
    private int balance;
    private Category category;
    private Member member;

    @Builder
    public AccountRequest(Long id, String accountNumber, int balance, Category category, Member member) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.category = category;
        this.member = member;
    }
}
