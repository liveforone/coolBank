package coolBank.coolBank.statement.controller;

import coolBank.coolBank.account.model.Account;
import coolBank.coolBank.account.model.Category;
import coolBank.coolBank.account.service.AccountService;
import coolBank.coolBank.member.model.Grade;
import coolBank.coolBank.member.model.Member;
import coolBank.coolBank.member.service.MemberService;
import coolBank.coolBank.statement.dto.StateRequest;
import coolBank.coolBank.statement.dto.StateResponse;
import coolBank.coolBank.statement.service.StatementService;
import coolBank.coolBank.utility.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatementController {

    private final StatementService statementService;
    private final AccountService accountService;
    private final MemberService memberService;

    private static final int PASSWORD_MATCH = 1;
    private static final int THOUSAND_WON = 10000000;

    @GetMapping("/statement/{accountNumber}")
    public ResponseEntity<?> statementPage(
            @PageableDefault(page = 0, size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "id", direction = Sort.Direction.DESC)
            }) Pageable pageable,
            @PathVariable("accountNumber") String accountNumber
    ) {
        Page<StateResponse> statementList = statementService.getStatementList(accountNumber, pageable);

        return ResponseEntity.ok(statementList);
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @RequestBody StateRequest request,
            Principal principal
    ) {
        Account account = accountService.getAccountDetailByNumber(request.getAccountNumber());
        Member member = memberService.getMemberEntity(principal.getName());

        if (CommonUtils.isNull(account)) {
            return ResponseEntity.ok("????????? ???????????? ????????????. ????????? ????????? ???????????????");
        }

        int checkPasswordMatching = memberService.checkPasswordMatching(
                request.getPassword(),
                member.getPassword()
        );

        if (checkPasswordMatching != PASSWORD_MATCH) {
            return ResponseEntity.ok("??????????????? ????????????. ?????? ???????????????");
        }

        accountService.deposit(
                request.getAccountNumber(),
                request.getMoney()
        );
        log.info("?????? ??????");
        statementService.saveStatementDeposit(
                request.getAccountNumber(),
                request.getMoney()
        );

        String url = "/member/my-page";
        HttpHeaders httpHeaders = CommonUtils.makeHeader(url);

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

        if (CommonUtils.isNull(account)) {
            return ResponseEntity.ok("????????? ???????????? ????????????. ????????? ????????? ???????????????");
        }

        if (request.getMoney() > myAccount.getBalance()) {
            return ResponseEntity.ok("?????? ????????? ???????????? ?????? ??????????????????.");
        }

        if (request.getMoney() >= THOUSAND_WON) {
            return ResponseEntity.ok("?????? ????????? ?????? ?????? ??????????????????.");
        }

        int checkPasswordMatching = memberService.checkPasswordMatching(
                request.getPassword(),
                member.getPassword()
        );

        if (checkPasswordMatching != PASSWORD_MATCH) {
            return ResponseEntity.ok("??????????????? ????????????. ?????? ???????????????");
        }

        accountService.deposit(
                request.getAccountNumber(),
                request.getMoney()
        );
        log.info("???????????? ???????????? : " + request.getAccountNumber());
        accountService.withdraw(
                myAccount.getAccountNumber(),
                request.getMoney()
        );
        log.info("???????????? ???????????? : " + myAccount.getAccountNumber());

        statementService.saveStatementDeposit(
                request.getAccountNumber(),
                request.getMoney()
        );
        statementService.saveStatementSend(
                myAccount.getAccountNumber(),
                request.getMoney()
        );

        String url = "/member/my-page";
        HttpHeaders httpHeaders = CommonUtils.makeHeader(url);

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

        if (CommonUtils.isNull(account)) {
            return ResponseEntity.ok("????????? ???????????? ????????????. ????????? ????????? ???????????????");
        }

        int checkPasswordMatching = memberService.checkPasswordMatching(
                request.getPassword(),
                member.getPassword()
        );

        if (checkPasswordMatching != PASSWORD_MATCH) {
            return ResponseEntity.ok("??????????????? ????????????. ?????? ???????????????");
        }

        /*
        * ???????????? ????????? ?????? ?????? ?????? ?????? ????????? ?????? ????????????.
         */
        if (account.getCategory() == Category.CREDIT) {
            accountService.withdraw(
                    request.getAccountNumber(),
                    request.getMoney()
            );
            log.info("?????? ??????");
            statementService.saveStatementWithdraw(
                    request.getAccountNumber(),
                    request.getMoney()
            );

            String url = "/member/my-page";
            HttpHeaders httpHeaders = CommonUtils.makeHeader(url);

            return ResponseEntity
                    .status(HttpStatus.MOVED_PERMANENTLY)
                    .headers(httpHeaders)
                    .build();
        }

        /*
        * ???????????? ????????? ???????????? ?????? ????????? ?????? ?????? ???????????? ??????????????? ??????.
         */
        if (request.getMoney() > account.getBalance()) {
            return ResponseEntity.ok("?????? ????????? ???????????? ?????? ??????????????????.");
        }

        accountService.withdraw(
                request.getAccountNumber(),
                request.getMoney()
        );
        log.info("?????? ??????");
        statementService.saveStatementWithdraw(
                request.getAccountNumber(),
                request.getMoney()
        );

        String url = "/member/my-page";
        HttpHeaders httpHeaders = CommonUtils.makeHeader(url);

        return ResponseEntity
                .status(HttpStatus.MOVED_PERMANENTLY)
                .headers(httpHeaders)
                .build();
    }
}
