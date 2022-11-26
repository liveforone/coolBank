package coolBank.coolBank.account.model;

import coolBank.coolBank.member.model.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;  //계좌 번호

    @Column(columnDefinition = "integer default 0")
    private int balance;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdDate;

    @Builder
    public Account(
            Long id,
            String accountNumber,
            int balance,
            Category category,
            Member member
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.category = category;
        this.member = member;
    }
}
