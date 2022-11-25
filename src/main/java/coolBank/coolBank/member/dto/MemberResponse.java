package coolBank.coolBank.member.dto;

import coolBank.coolBank.member.model.Grade;
import coolBank.coolBank.member.model.Role;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberResponse {

    private Long id;
    private String email;
    private Grade grade;
    private String nickname;

    @Builder
    public MemberResponse(
            Long id,
            String email,
            Role auth,
            Grade grade,
            String nickname
    ) {
        this.id = id;
        this.email = email;
        this.grade = grade;
        this.nickname = nickname;
    }
}
