package coolBank.coolBank.account.controller;

import coolBank.coolBank.account.model.Account;
import coolBank.coolBank.account.service.AccountService;
import coolBank.coolBank.member.service.MemberService;
import coolBank.coolBank.utility.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    private static final int CAN_MAKE_ACCOUNT_SIZE = 2;

    @GetMapping("/account/{id}")
    public ResponseEntity<?> accountDetail(@PathVariable("id") Long id) {
        Account account = accountService.getAccountDetailById(id);

        if (CommonUtils.isNull(account)) {
            return ResponseEntity.ok("해당 계좌가 없어 조회가 불가능합니다.");
        }

        return ResponseEntity.ok(
                accountService.entityToDtoDetail(account)
        );
    }

    @PostMapping("/normal-account/post")
    public ResponseEntity<?> makeNormalAccount(Principal principal) {
        List<Account> myAccountList = accountService.getMyAccountList(principal.getName());

        if (myAccountList.size() >= CAN_MAKE_ACCOUNT_SIZE) {
            return ResponseEntity.ok("게좌가 이미 2개가 개설되어 계좌를 추가 개설 할 수 없습니다.");
        }

        Long accountId = accountService.saveNormalAccount(principal.getName());
        log.info("일반 계좌 생성 성공");

        String url = "/account/" + accountId;
        HttpHeaders httpHeaders = CommonUtils.makeHeader(url);

        return ResponseEntity
                .status(HttpStatus.MOVED_PERMANENTLY)
                .headers(httpHeaders)
                .build();
    }

    @PostMapping("/credit-account/post")
    public ResponseEntity<?> makeCreditAccount(Principal principal) {
        Account creditAccount = accountService.getCreditAccount(principal.getName());

        if (!CommonUtils.isNull(creditAccount)) {
            return ResponseEntity.ok("이미 마이너스 통장이 존재합니다.");
        }

        Long accountId = accountService.saveCreditAccount(principal.getName());
        log.info("마이너스 계좌 생성 성공");

        String url = "/account/" + accountId;
        HttpHeaders httpHeaders = CommonUtils.makeHeader(url);

        return ResponseEntity
                .status(HttpStatus.MOVED_PERMANENTLY)
                .headers(httpHeaders)
                .build();
    }
}
