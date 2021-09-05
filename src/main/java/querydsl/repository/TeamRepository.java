package querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import querydsl.domain.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
