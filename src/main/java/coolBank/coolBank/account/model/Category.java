package coolBank.coolBank.account.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Category {

    NORMAL("NORMAL_BANKBOOK"),
    CREDIT("CREDIT_BANKBOOK");

    private String value;
}
