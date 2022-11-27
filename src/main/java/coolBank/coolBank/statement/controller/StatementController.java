package coolBank.coolBank.statement.controller;

import coolBank.coolBank.account.model.Account;
import coolBank.coolBank.account.model.Category;
import coolBank.coolBank.account.service.AccountService;
import coolBank.coolBank.member.model.Member;
import coolBank.coolBank.member.service.MemberService;
import coolBank.coolBank.statement.dto.StateRequest;
import coolBank.coolBank.statement.service.StatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatementController {

    private final StatementService statementService;
    private final AccountService accountService;
    private final MemberService memberService;

    public static final int PASSWORD_MATCH = 1;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @RequestBody StateRequest request,
            Principal principal
    ) {
        Account account = accountService.getAccountDetailByNumber(request.getAccountNumber());
        Member member = memberService.getMemberEntity(principal.getName());

        if (account == null) {
            return ResponseEntity.ok("계좌가 존재하지 않습니다. 올바른 번호를 적어주세요");
        }

        int checkPasswordMatching = memberService.checkPasswordMatching(
                request.getPassword(),
                member.getPassword()
        );

        if (checkPasswordMatching != PASSWORD_MATCH) {
            return ResponseEntity.ok("비밀번호가 다릅니다. 다시 시도하세요");
        }

        accountService.deposit(
                request.getAccountNumber(),
                request.getMoney()
        );
        log.info("입금 성공");
        statementService.saveStatementDeposit(
                request.getAccountNumber(),
                request.getMoney()
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("/member/my-page"));

        return ResponseEntity
                .status(HttpStatus.MOVED_PERMANENTLY)
                .headers(httpHeaders)
                .build();
    }

    @PostMapping("/remit")
    public ResponseEntity<?> remit(
            @RequestBody StateRequest request,
            Principal principal
    ) {
        Account account = accountService.getAccountDetailByNumber(request.getAccountNumber());
        Account myAccount = accountService.getAccountDetailByEmail(principal.getName());
        Member member = memberService.getMemberEntity(principal.getName());

        if (account == null) {
            return ResponseEntity.ok("계좌가 존재하지 않습니다. 올바른 번호를 적어주세요");
        }

        if (request.getMoney() > myAccount.getBalance()) {
            return ResponseEntity.ok("송금 금액이 잔액보다 많아 불가능합니다.");
        }

        int checkPasswordMatching = memberService.checkPasswordMatching(
                request.getPassword(),
                member.getPassword()
        );

        if (checkPasswordMatching != PASSWORD_MATCH) {
            return ResponseEntity.ok("비밀번호가 다릅니다. 다시 시도하세요");
        }

        accountService.deposit(
                request.getAccountNumber(),
                request.getMoney()
        );
        log.info("입금성공 계좌번호 : " + request.getAccountNumber());
        accountService.withdraw(
                myAccount.getAccountNumber(),
                request.getMoney()
        );
        log.info("출금성공 계좌번호 : " + myAccount.getAccountNumber());

        statementService.saveStatementDeposit(
                request.getAccountNumber(),
                request.getMoney()
        );
        statementService.saveStatementWithdraw(
                myAccount.getAccountNumber(),
                request.getMoney()
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("/member/my-page"));

        return ResponseEntity
                .status(HttpStatus.MOVED_PERMANENTLY)
                .headers(httpHeaders)
                .build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestBody StateRequest request,
            Principal principal
    ) {
        Account account = accountService.getAccountDetailByNumber(request.getAccountNumber());
        Member member = memberService.getMemberEntity(principal.getName());

        if (account == null) {
            return ResponseEntity.ok("계좌가 존재하지 않습니다. 올바른 번호를 적어주세요");
        }

        int checkPasswordMatching = memberService.checkPasswordMatching(
                request.getPassword(),
                member.getPassword()
        );

        if (checkPasswordMatching != PASSWORD_MATCH) {
            return ResponseEntity.ok("비밀번호가 다릅니다. 다시 시도하세요");
        }

        /*
        * 마이너스 통장일 경우 계좌 잔액 분기 처리전 바로 빠져나감.
         */
        if (account.getCategory() == Category.CREDIT) {
            accountService.withdraw(
                    request.getAccountNumber(),
                    request.getMoney()
            );
            log.info("출금 성공");
            statementService.saveStatementWithdraw(
                    request.getAccountNumber(),
                    request.getMoney()
            );

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(URI.create("/member/my-page"));

            return ResponseEntity
                    .status(HttpStatus.MOVED_PERMANENTLY)
                    .headers(httpHeaders)
                    .build();
        }

        /*
        * 마이너스 통장이 아닐경우 계좌 잔액과 출금 금액 비교하는 분기처리를 거침.
         */
        if (request.getMoney() > account.getBalance()) {
            return ResponseEntity.ok("출금 금액이 잔액보다 많아 불가능합니다.");
        }

        accountService.withdraw(
                request.getAccountNumber(),
                request.getMoney()
        );
        log.info("출금 성공");
        statementService.saveStatementWithdraw(
                request.getAccountNumber(),
                request.getMoney()
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("/member/my-page"));

        return ResponseEntity
                .status(HttpStatus.MOVED_PERMANENTLY)
                .headers(httpHeaders)
                .build();
    }
}
