package querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import querydsl.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository <Member, Long> {
}
