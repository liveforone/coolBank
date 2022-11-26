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
     */
    private String makeAccountNumber() {
        return RandomStringUtils.random(12, 33, 125, false, true);
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

    public Account getAccountDetail(Long id) {
        return accountRepository.findOneById(id);
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
}
