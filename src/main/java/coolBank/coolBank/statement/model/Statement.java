package coolBank.coolBank.statement.model;

import coolBank.coolBank.account.model.Account;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private State state;

    @Column(columnDefinition = "integer default 0")
    private int money;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @Builder
    public Statement(Long id, State state, int money, Account account) {
        this.id = id;
        this.state = state;
        this.money = money;
        this.account = account;
    }
}
