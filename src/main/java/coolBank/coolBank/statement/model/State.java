package coolBank.coolBank.statement.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum State {

    DEPOSIT("STATE_DEPOSIT"),
    WITHDRAW("STATE_WITHDRAW");

    private String value;
}
