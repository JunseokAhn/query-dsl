package querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import querydsl.domain.Member;
import querydsl.domain.QTeam;
import querydsl.domain.Team;
import querydsl.repository.MemberRepository;
import querydsl.repository.TeamRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static querydsl.domain.QMember.*;
import static querydsl.domain.QTeam.*;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@PersistenceContext
	EntityManager em;
	JPAQueryFactory queryFactory;

	@Autowired
	MemberRepository memberRepository;
	@Autowired
	TeamRepository teamRepository;

	@BeforeEach
	public void init() throws Exception {

		queryFactory = new JPAQueryFactory(em);

		Team team1 = new Team("team1");
		Team team2 = new Team("team2");
		teamRepository.save(team1);
		teamRepository.save(team2);

		memberRepository.save(new Member("member1", 5, team1));
		memberRepository.save(new Member("member2", 10, team1));
		memberRepository.save(new Member("member3", 15, team2));
		memberRepository.save(new Member("member4", 20, team2));
		memberRepository.save(new Member("member5", 25, team2));

	}
	
	@Test
	public void findByName() throws Exception {

		//select * from member where name = "member1"
		Member member1 = queryFactory
				.select(member)
				.from(member)
				.where(member.name.eq("member1"))
				.fetchOne();

	    assertEquals(member1.getName(), "member1");

	}

	@Test
	public void findByNameAndAge() throws Exception {

		//select * from member where name like "%ber%" and age >= 20 limit 2
		List<Member> memberList = queryFactory
				.selectFrom(member)
				.where(member.name.contains("ber")
						.and(member.age.goe(20)))
				.limit(2)
				.fetch();

				/*.where(
						member.name.contains("ber")
						,(member.age.goe(20)
				)*/

		assertEquals(memberList.size(), 2);
	}

	@Test
	public void count() throws Exception {

		//select count(*) from member
		long totalCount = queryFactory
				.selectFrom(member)
				.fetchCount();


		//select member + count query
		QueryResults<Member> results = queryFactory
				.selectFrom(member)
				.fetchResults();

		long totalCount2 = results.getTotal();
		List<Member> memberList = results.getResults();

		assertEquals(memberList.size(), totalCount2);

	}

	@Test
	public void sort() throws Exception {

		em.persist(new Member(null, 500));
		em.persist(new Member("memberNull", 500));
		em.persist(new Member("member6", 700));
		em.persist(new Member("member6", 800));
		em.persist(new Member("member7", 800));
		em.persist(new Member("member8", 900));
		em.persist(new Member("member9", 1000));

		List<Member> memberList = queryFactory
				.selectFrom(member)
				.where(
						member.age.lt(900)
						, member.age.eq(700).not()
				)
				.orderBy(
						member.age.desc()
						, member.name.asc().nullsLast()
				)
				.fetch();

		for (Member m : memberList) {
			System.out.println(m);
		}

	}

	@Test
	public void paging() throws Exception {

		List<Member> memberList = queryFactory
				.selectFrom(member)
				.orderBy(member.name.asc())
				.offset(1)
				.limit(2)
				.fetch();

		assertEquals(memberList.size(), 2);
	}

	@Test
	public void aggregation() throws Exception {

		List<Tuple> result = queryFactory
				.select(
						member.count()
						, member.age.sum()
						, member.age.avg()
						, member.age.max()
				)
				.from(member)
				.fetch();

		Tuple tuple = result.get(0);

		assertEquals(tuple.get(member.count()), 5);
		assertEquals(tuple.get(member.age.sum()), 75);
		assertEquals(tuple.get(member.age.avg()), 15);
		assertEquals(tuple.get(member.age.max()), 25);

	}

	@Test
	public void groupBy() throws Exception {

		List<Tuple> result = queryFactory
				.select(team.name, member.age.avg())
				.from(member)
				.join(member.team, team)
				.groupBy(team.name)
				.orderBy(team.name.asc())
				.fetch();

		Tuple tuple1 = result.get(0);
		Tuple tuple2 = result.get(1);

		assertEquals(tuple1.get(team.name), "team1");
		assertEquals(tuple2.get(team.name), "team2");

	}
}
