package coolBank.coolBank.statement.service;

import coolBank.coolBank.account.model.Account;
import coolBank.coolBank.account.repository.AccountRepository;
import coolBank.coolBank.statement.dto.StateResponse;
import coolBank.coolBank.statement.model.State;
import coolBank.coolBank.statement.model.Statement;
import coolBank.coolBank.statement.repository.StatementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatementService {

    private final StatementRepository statementRepository;
    private final AccountRepository accountRepository;

    //== StatementResponse builder method ==//
    public StateResponse dtoBuilder(Statement statement) {
        return StateResponse.builder()
                .id(statement.getId())
                .state(statement.getState().getValue())
                .money(statement.getMoney())
                .createdDate(statement.getCreatedDate())
                .build();
    }


    //== entity -> dto 변환 메소드1 - 페이징 ==//
    public Page<StateResponse> entityToDtoPage(Page<Statement> statementPage) {
        return statementPage.map(this::dtoBuilder);
    }

    public Page<StateResponse> getStatementList(String accountNumber, Pageable pageable) {
        return entityToDtoPage(
                statementRepository.findStatementByAccountNumber(
                        accountNumber,
                        pageable
                )
        );
    }

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
    public void saveStatementSend(String accountNumber, int money) {
        Account account = accountRepository.findOneByAccountNumber(accountNumber);

        Statement statement = Statement.builder()
                .state(State.SEND)
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
