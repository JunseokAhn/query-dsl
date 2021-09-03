package querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import querydsl.domain.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
}
