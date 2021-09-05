package querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import querydsl.dto.MemberDTO;
import querydsl.dto.MemberSearchCondition;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberDTO> search(MemberSearchCondition msc);

    Page<MemberDTO> pagingSearch(MemberSearchCondition msc, Pageable pageable);

    Page<MemberDTO> pagingSearch2(MemberSearchCondition msc, Pageable pageable);

}
