package coolBank.coolBank.statement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StateRequest {

    private String accountNumber;
    private int money;
    private String password;
}
