package coolBank.coolBank.account.service;

import coolBank.coolBank.account.dto.AccountRequest;
import coolBank.coolBank.account.dto.AccountResponse;
import coolBank.coolBank.account.model.Account;
import coolBank.coolBank.account.model.Category;
import coolBank.coolBank.account.repository.AccountRepository;
import coolBank.coolBank.member.model.Member;
import coolBank.coolBank.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    /*
    * 계좌 번호 만드는 함수
    * 12자리의 숫자를 무작위로 생성하여 문자열로 리턴
    * 중복을 체크하여 중복되지 않는 계좌 번호를 리턴한다.
     */
    private String makeAccountNumber() {
        String accountNumber;

        while (true) {
            String randNum = RandomStringUtils.random(12, 33, 125, false, true);
            Account account = accountRepository.findOneByAccountNumber(randNum);

            if (account == null) {  //중복 체크
                accountNumber = randNum;
                break;
            }
        }

        return accountNumber;
    }

    //== dto -> entity ==//
    public Account dtoToEntity(AccountRequest accountRequest) {
        return Account.builder()
                .id(accountRequest.getId())
                .accountNumber(accountRequest.getAccountNumber())
                .balance(accountRequest.getBalance())
                .category(accountRequest.getCategory())
                .member(accountRequest.getMember())
                .build();
    }

    //== AccountResponse builder method ==//
    public AccountResponse dtoBuilder(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .build();
    }

    //== entity -> dto 편의 메소드1 - 리스트형식 ==//
    public List<AccountResponse> entityToDtoList(List<Account> accountList) {
        List<AccountResponse> dtoList = new ArrayList<>();

        for (Account account : accountList) {
            dtoList.add(dtoBuilder(account));
        }

        return dtoList;
    }

    //== entity -> dto 편의 메소드2 - 디테일 ==//
    public AccountResponse entityToDtoDetail(Account account) {
        return dtoBuilder(account);
    }

    public List<Account> getMyAccountList(String email) {
        return accountRepository.findAccountList(email);
    }

    public Account getCreditAccount(String email) {
        return accountRepository.findCreditAccount(email, Category.CREDIT);
    }

    public Account getAccountDetailById(Long id) {
        return accountRepository.findOneById(id);
    }

    public Account getAccountDetailByEmail(String email) {
        return accountRepository.findOneByEmail(email);
    }

    public Account getAccountDetailByNumber(String accountNumber) {
        return accountRepository.findOneByAccountNumber(accountNumber);
    }

    //== 일반 통장 개설 ==//
    @Transactional
    public Long saveNormalAccount(String email) {
        Member member = memberRepository.findByEmail(email);

        AccountRequest dto = AccountRequest.builder()
                .accountNumber(makeAccountNumber())
                .balance(0)
                .member(member)
                .category(Category.NORMAL)
                .build();

        return accountRepository.save(
                dtoToEntity(dto)
        ).getId();
    }

    //== 마이너스 통장 개설 ==//
    @Transactional
    public Long saveCreditAccount(String email) {
        Member member = memberRepository.findByEmail(email);

        AccountRequest dto = AccountRequest.builder()
                .accountNumber(makeAccountNumber())
                .balance(0)
                .member(member)
                .category(Category.CREDIT)
                .build();

        return accountRepository.save(
                dtoToEntity(dto)
        ).getId();
    }

    @Transactional
    public void deposit(String accountNumber, int money) {
        accountRepository.depositAccount(accountNumber, money);
    }

    @Transactional
    public void withdraw(String accountNumber, int money) {
        accountRepository.withdrawAccount(accountNumber, money);
    }
}
