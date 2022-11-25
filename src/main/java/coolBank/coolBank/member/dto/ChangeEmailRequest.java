package coolBank.coolBank.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeEmailRequest {

    private String email;
    private String password;
}
