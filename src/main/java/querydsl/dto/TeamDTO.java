package querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamDTO {

    private String name;

    @QueryProjection
    public TeamDTO(String name){
        this.name = name;
    }

}
