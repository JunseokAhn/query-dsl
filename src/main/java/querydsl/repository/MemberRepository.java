package querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import querydsl.domain.Member;

public interface MemberRepository extends JpaRepository <Member, Long>, MemberRepositoryCustom {
}
