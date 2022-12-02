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

    //== 무작위 닉네임 생성 - 숫자 + 문자 ==//
    public String makeRandomNickname() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    //== 회원 등급 체크 ==//
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

    //== 이메일 중복 검증 ==//
    @Transactional(readOnly = true)
    public int checkDuplicateEmail(String email) {
        Member member = memberRepository.findByEmail(email);

        if (CommonUtils.isNull(member)) {
            return NOT_DUPLICATE;
        }
        return DUPLICATE;
    }

    //== 닉네임 중복 검증 ==//
    @Transactional(readOnly = true)
    public int checkDuplicateNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname);

        if (CommonUtils.isNull(member)) {
            return NOT_DUPLICATE;
        }
        return DUPLICATE;
    }

    //== 비밀번호 복호화 ==//
    public int checkPasswordMatching(String inputPassword, String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if(encoder.matches(inputPassword, password)) {
            return PASSWORD_MATCH;
        }
        return PASSWORD_NOT_MATCH;
    }

    //== spring context 반환 메소드(필수) ==//
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (member.getAuth() == Role.ADMIN) {  //어드민 아이디 지정됨, 비밀번호는 회원가입해야함
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
        }
        authorities.add(new SimpleGrantedAuthority(Role.MEMBER.getValue()));

        return new User(
                member.getEmail(),
                member.getPassword(),
                authorities
        );
    }

    //== 유저 엔티티 반환 ==//
    public Member getMemberEntity(String email) {
        return memberRepository.findByEmail(email);
    }

    //== 유저 responseDto 반환 ==//
    public MemberResponse getMemberByEmail(String email) {
        return entityToDtoDetail(
                memberRepository.findByEmail(email)
        );
    }

    //== 전체 유저 리턴 for admin ==//
    public List<Member> getAllMemberForAdmin() {
        return memberRepository.findAll();
    }

    //== 회원 가입 로직 ==//
    @Transactional
    public void joinUser(MemberRequest memberRequest) {
        //비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberRequest.setPassword(passwordEncoder.encode(memberRequest.getPassword()));
        memberRequest.setAuth(Role.MEMBER);  //기본 권한 매핑
        memberRequest.setGrade(Grade.BRONZE);
        memberRequest.setNickname(makeRandomNickname());  //무작위 닉네임 생성

        memberRepository.save(
                dtoToEntity(memberRequest)
        );
    }

    //== 로그인 - 세션과 컨텍스트홀더 사용 ==//
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
        처음 어드민이 로그인을 하는경우 이메일로 판별해서 권한을 admin 으로 변경해주고
        그 다음부터 어드민이 업데이트 할때에는 auth 칼럼으로 판별해서 db 업데이트 하지않고,
        GrantedAuthority 만 업데이트 해준다.
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
        //pw 암호화
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
