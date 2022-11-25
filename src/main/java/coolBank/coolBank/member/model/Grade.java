package coolBank.coolBank.member.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Grade {

    BRONZE("GRADE_BRONZE"),
    SILVER("GRADE_SILVER"),
    GOLD("GRADE_GOLD"),
    PLATINUM("GRADE_PLATINUM"),
    DIA("GRADE_DIA");

    private String value;
}
