package coolBank.coolBank.statement.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class StateResponse {

    private Long id;
    private String state;
    private int money;
    private LocalDateTime createdDate;

    @Builder
    public StateResponse(Long id, String state, int money, LocalDateTime createdDate) {
        this.id = id;
        this.state = state;
        this.money = money;
        this.createdDate = createdDate;
    }
}
