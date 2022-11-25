package coolBank.coolBank.member.dto;

import coolBank.coolBank.member.model.Grade;
import coolBank.coolBank.member.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberRequest {

    private Long id;
    private String email;
    private String password;
    private Role auth;
    private Grade grade;
    private String nickname;
}
