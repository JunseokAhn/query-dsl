package querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import querydsl.domain.Member;
import querydsl.domain.QMember;
import querydsl.domain.Team;
import querydsl.dto.MemberDTO;
import querydsl.dto.QTeamDTO;
import querydsl.dto.TeamDTO;
import querydsl.repository.MemberRepository;
import querydsl.repository.TeamRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static com.querydsl.core.types.ExpressionUtils.*;
import static com.querydsl.core.types.Projections.*;
import static com.querydsl.jpa.JPAExpressions.*;
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

    @Test
    public void leftJoin() throws Exception {

        //select * from member left join team using (team_id)
        //team_id를 member.team에서 꺼내온다.
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .fetch();

        for (Member m : memberList) {
            System.out.println(m);
        }

		/*
		select *
		from member
			left join team team1
				on team1.team_id = member.team_id
			left join team team2
		 		on team1.team_id = team2.team_id

		join절에 team만 들어감으로 인해 세타조인 발생
			> member.team.name에서 필요한 team을 한번 더 조인해옴
		*/
        List<Tuple> tupleList = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.team.name.eq(team.name))
                .fetch();

        for (Tuple tuple : tupleList) {
            System.out.println(tuple);
        }


		/*
		select * from member left join team
		on member.team_id = team.team_id
		and team.name = team.name

		join절에 member.team이 들어감으로인해,
		member.team.name에서 필요한 team을 따로 조인으로 찾아오지 않음
		*/
        List<Tuple> tupleList2 = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq(member.team.name))
                .fetch();

        for (Tuple tuple : tupleList2) {
            System.out.println(tuple);
        }

    }


    @Test
    public void fetchJoin() throws Exception {

        em.flush();
        em.clear();

        List<Member> memberList = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .fetch();

    }

    @Test
    public void subQuery() throws Exception {

        //JPAExpressions.select로 서브쿼리 사용
        QMember sub = new QMember("sub");

        //select * from member where age in
        //( select age from member where age >= 15 )

        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(sub.age)
                                .from(sub)
                                .where(sub.age.goe(15))
                ))
                .fetch();

    }

    @Test
    public void caseQuery() throws Exception {

        List<String> ageList = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("그외"))
                .from(member)
                .fetch();

        List<String> ageList2 = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.eq(5)).then("다섯살")
                        .when(member.age.eq(10)).then("열살")
                        .when(member.name.eq("member1")).then("member20")
                        .otherwise("그외")
                )
                .from(member)
                .fetch();

    }

    @Test
    public void constant() throws Exception {

        List<Tuple> tupleList = queryFactory
                .select(member.name, Expressions.constant("constant"))
                .from(member)
                .fetch();

        //"name_age"
        List<String> stringList = queryFactory
                .select(member.name.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

    }

    @Test
    public void findByDTO() throws Exception {


        //Projections.bean
        //need getter, setter, noArgsConstructor
        List<MemberDTO> memberList = queryFactory
                .select(bean(MemberDTO.class, member.name, member.age))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : memberList) {
            System.out.println(memberDTO);
        }

        //Projections.fields
        //do not need getter, setter
        List<MemberDTO> memberList2 = queryFactory
                .select(fields(MemberDTO.class, member.name, member.age, member.age.as("age2")))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : memberList2) {
            System.out.println(memberDTO);
        }

        //Projections.constructor
        //need constructor
        List<MemberDTO> memberList3 = queryFactory
                .select(constructor(MemberDTO.class, member.name, member.age))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : memberList3) {
            System.out.println(memberDTO);
        }

    }

    @Test
    public void findSubqueryByDTO() throws Exception {

        QMember qm = new QMember("qm");

        //서브쿼리를 사용하는경우, 다른 별칭을가진 QMember를 사용하므로,
        //ExpressionUtils.as로 감싸줘야한다.

        List<MemberDTO> memberList = queryFactory
                .select(bean(MemberDTO.class,
                        member.name,
                        as(select(qm.member.age).from(qm), "age")
                ))
                .from(member)
                .fetch();

    }

    @Test
    public void findByQueryProjection() throws Exception {

        List<TeamDTO> teamList = queryFactory
                .select(new QTeamDTO(team.name))
                .from(team)
                .fetch();

        /*
        select team.name
        from team
        where team.team_id
           in (
               select member.team_id
               from member
           )
        */
        List<TeamDTO> teamList2 = queryFactory
                .select(new QTeamDTO(team.name))
                .from(team)
                .where(team.id.in(
                        select(member.team.id)
                                .from(member))
                ).fetch();

    }

    @Test
    public void dynamicQuery_booleanBuilder() throws Exception {

        String nameParam = "member1";
        Integer ageParam = 5;

        //booleanBuilder에 조건 설정
        BooleanBuilder bb = new BooleanBuilder();
        if (nameParam != null)
            bb.and(member.name.eq(nameParam));
        if (ageParam != null)
            bb.or(member.age.eq(ageParam));

        //조건을 넘겨받은 booleanBuilder를 where절에 입력
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(bb)
                .fetch();
    }

    @Test
    public void dynamicQuery_multiParam() throws Exception {

        String nameParam = "member1";
        Integer ageParam = null;

        //where절에서 받는 파라미터를 함수로 해결 > 재활용가능
        //null을 리턴받으면 무시됨
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(nameEq(nameParam), ageEq(ageParam))
                .fetch();

        //필요에따라서 여러 조건문을 조립해서 사용가능
        List<Member> memberList2 = queryFactory
                .selectFrom(member)
                .where(infoEq(nameParam, ageParam))
                .fetch();


    }

    private BooleanExpression infoEq(String nameParam, Integer ageParam) {
        return nameEq(nameParam).or(ageEq(ageParam));
    }

    private BooleanExpression nameEq(String nameParam) {
        return nameParam == null ? null : member.name.eq(nameParam);
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam == null ? null : member.age.eq(ageParam);
    }


    @Test
    public void bulk() throws Exception {


        long count = queryFactory
                .update(member)
                .set(member.name, "june")
                .where(member.age.goe(20))
                .execute();

        em.flush();
        em.clear();

        long count2 = queryFactory
                .update(member)
                .set(member.age, member.age.add(-1.9))
                .execute();

        em.flush();
        em.clear();

        long count3 = queryFactory
                .update(member)
                .set(member.age, member.age.multiply(3))
                .execute();

        em.flush();
        em.clear();

        long count4 = queryFactory
                .delete(member)
                .where(member.age.lt(15))
                .execute();

        em.flush();
        em.clear();

    }
}
