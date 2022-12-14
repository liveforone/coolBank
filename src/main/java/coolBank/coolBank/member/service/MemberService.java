package coolBank.coolBank.member.service;

import coolBank.coolBank.account.repository.AccountRepository;
import coolBank.coolBank.member.dto.MemberRequest;
import coolBank.coolBank.member.dto.MemberResponse;
import coolBank.coolBank.member.model.Grade;
import coolBank.coolBank.member.model.Member;
import coolBank.coolBank.member.model.Role;
import coolBank.coolBank.member.repository.MemberRepository;
import coolBank.coolBank.statement.model.State;
import coolBank.coolBank.statement.model.Statement;
import coolBank.coolBank.statement.repository.StatementRepository;
import coolBank.coolBank.utility.CommonUtils;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.RandomStringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final StatementRepository statementRepository;
    private final AccountRepository accountRepository;

    private static final int DUPLICATE = 0;
    private static final int NOT_DUPLICATE = 1;
    private static final int PASSWORD_MATCH = 1;
    private static final int PASSWORD_NOT_MATCH = 0;

    //== UserResponse builder method ==//
    public MemberResponse dtoBuilder(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .grade(member.getGrade())
                .nickname(member.getNickname())
                .build();
    }

    //== dto -> entity ==//
    public Member dtoToEntity(MemberRequest member) {
        return Member.builder()
                .id(member.getId())
                .email(member.getEmail())
                .password(member.getPassword())
                .auth(member.getAuth())
                .grade(member.getGrade())
                .nickname(member.getNickname())
                .build();
    }

    //== entity -> dto1 - detail ==//
    public MemberResponse entityToDtoDetail(Member member) {

        if (CommonUtils.isNull(member)) {
            return null;
        }
        return dtoBuilder(member);
    }

    //== ????????? ????????? ?????? - ?????? + ?????? ==//
    public String makeRandomNickname() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    //== ?????? ?????? ?????? ==//
    public Grade checkMemberGrade(List<Statement> statementList) {
        int size = statementList.size();

        if (size > 1000) {
            return Grade.DIA;
        }

        if (size > 500) {
            return Grade.PLATINUM;
        }

        if (size > 100) {
            return Grade.GOLD;
        }

        if (size > 50) {
            return Grade.SILVER;
        }

        return Grade.BRONZE;
    }

    //== ????????? ?????? ?????? ==//
    @Transactional(readOnly = true)
    public int checkDuplicateEmail(String email) {
        Member member = memberRepository.findByEmail(email);

        if (CommonUtils.isNull(member)) {
            return NOT_DUPLICATE;
        }
        return DUPLICATE;
    }

    //== ????????? ?????? ?????? ==//
    @Transactional(readOnly = true)
    public int checkDuplicateNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname);

        if (CommonUtils.isNull(member)) {
            return NOT_DUPLICATE;
        }
        return DUPLICATE;
    }

    //== ???????????? ????????? ==//
    public int checkPasswordMatching(String inputPassword, String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if(encoder.matches(inputPassword, password)) {
            return PASSWORD_MATCH;
        }
        return PASSWORD_NOT_MATCH;
    }

    //== spring context ?????? ?????????(??????) ==//
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (member.getAuth() == Role.ADMIN) {  //????????? ????????? ?????????, ??????????????? ?????????????????????
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
        }
        authorities.add(new SimpleGrantedAuthority(Role.MEMBER.getValue()));

        return new User(
                member.getEmail(),
                member.getPassword(),
                authorities
        );
    }

    //== ?????? ????????? ?????? ==//
    public Member getMemberEntity(String email) {
        return memberRepository.findByEmail(email);
    }

    //== ?????? responseDto ?????? ==//
    public MemberResponse getMemberByEmail(String email) {
        return entityToDtoDetail(
                memberRepository.findByEmail(email)
        );
    }

    //== ?????? ?????? ?????? for admin ==//
    public List<Member> getAllMemberForAdmin() {
        return memberRepository.findAll();
    }

    //== ?????? ?????? ?????? ==//
    @Transactional
    public void joinUser(MemberRequest memberRequest) {
        //???????????? ?????????
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberRequest.setPassword(passwordEncoder.encode(memberRequest.getPassword()));
        memberRequest.setAuth(Role.MEMBER);  //?????? ?????? ??????
        memberRequest.setGrade(Grade.BRONZE);
        memberRequest.setNickname(makeRandomNickname());  //????????? ????????? ??????

        memberRepository.save(
                dtoToEntity(memberRequest)
        );
    }

    //== ????????? - ????????? ?????????????????? ?????? ==//
    @Transactional
    public void login(MemberRequest memberRequest, HttpSession httpSession)
            throws UsernameNotFoundException
    {
        String email = memberRequest.getEmail();
        String password = memberRequest.getPassword();
        Member member = memberRepository.findByEmail(email);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(email, password);
        SecurityContextHolder.getContext().setAuthentication(token);
        httpSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        List<GrantedAuthority> authorities = new ArrayList<>();
        /*
        ?????? ???????????? ???????????? ???????????? ???????????? ???????????? ????????? admin ?????? ???????????????
        ??? ???????????? ???????????? ???????????? ???????????? auth ???????????? ???????????? db ???????????? ????????????,
        GrantedAuthority ??? ???????????? ?????????.
         */
        if (member.getAuth() != Role.ADMIN && ("admin@coolbank.com").equals(email)) {
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
            memberRepository.updateAuth(Role.ADMIN, memberRequest.getEmail());
        }

        if (member.getAuth() == Role.ADMIN) {
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
        }
        authorities.add(new SimpleGrantedAuthority(Role.MEMBER.getValue()));

        new User(
                member.getEmail(),
                member.getPassword(),
                authorities
        );
    }

    @Transactional
    public void updateNickname(String nickname, String email) {
        memberRepository.updateNickname(nickname, email);
    }

    @Transactional
    public void updateEmail(String oldEmail, String newEmail) {
        memberRepository.updateEmail(oldEmail, newEmail);
    }

    @Transactional
    public void updatePassword(Long id, String inputPassword) {
        //pw ?????????
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String newPassword =  passwordEncoder.encode(inputPassword);

        memberRepository.updatePassword(id, newPassword);
    }

    @Transactional
    public void updateGrade(Long accountId) {
        List<Statement> statement = statementRepository.findStatementByAccountIdAndSend(
                accountId,
                State.SEND
        );
        Long memberId = accountRepository.findOneById(accountId).getMember().getId();

        if (statement.isEmpty()) {
            return;
        }

        memberRepository.updateGrade(
                checkMemberGrade(statement),
                memberId
        );
    }

    @Transactional
    public void deleteUser(Long userId) {
        memberRepository.deleteById(userId);
    }
}
