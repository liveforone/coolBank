package coolBank.coolBank.statement.service;

import coolBank.coolBank.account.model.Account;
import coolBank.coolBank.account.repository.AccountRepository;
import coolBank.coolBank.statement.model.State;
import coolBank.coolBank.statement.model.Statement;
import coolBank.coolBank.statement.repository.StatementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatementService {

    private final StatementRepository statementRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void saveStatementDeposit(String accountNumber, int money) {
        Account account = accountRepository.findOneByAccountNumber(accountNumber);

        Statement statement = Statement.builder()
                .state(State.DEPOSIT)
                .money(money)
                .account(account)
                .build();

        statementRepository.save(statement);
    }

    @Transactional
    public void saveStatementWithdraw(String accountNumber, int money) {
        Account account = accountRepository.findOneByAccountNumber(accountNumber);

        Statement statement = Statement.builder()
                .state(State.WITHDRAW)
                .money(money)
                .account(account)
                .build();

        statementRepository.save(statement);
    }
}
