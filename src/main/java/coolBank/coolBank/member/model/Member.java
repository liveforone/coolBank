package coolBank.coolBank.member.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @Enumerated(value = EnumType.STRING)
    private Role auth;

    @Enumerated(value = EnumType.STRING)
    private Grade grade;

    private String nickname;

    @Builder
    public Member(
            Long id,
            String email,
            String password,
            Role auth,
            Grade grade,
            String nickname
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.auth = auth;
        this.grade = grade;
        this.nickname = nickname;
    }
}
