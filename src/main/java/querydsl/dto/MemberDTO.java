package querydsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDTO {

    private String name;
    private int age;
    //private int age2;

    public MemberDTO (String name, int age){
        this.name = name;
        this.age = age;
    }
}
