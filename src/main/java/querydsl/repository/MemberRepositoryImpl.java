package querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import querydsl.dto.MemberDTO;
import querydsl.dto.MemberSearchCondition;

import javax.persistence.EntityManager;
import java.util.List;

import static com.querydsl.core.types.Projections.*;
import static querydsl.domain.QMember.*;
import static querydsl.domain.QTeam.*;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberDTO> search(MemberSearchCondition msc) {

        return queryFactory
                .select(fields(MemberDTO.class, member.name, member.age))
                .from(member)
                .where(conditionCheck(msc))
                .fetch();
    }

    @Override
    public Page<MemberDTO> pagingSearch(MemberSearchCondition msc, Pageable pageable) {

        QueryResults<MemberDTO> results = queryFactory
                .select(fields(MemberDTO.class, member.name, member.age))
                .from(member)
                .where(conditionCheck(msc))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberDTO> memberList = results.getResults();
        long totalCount = results.getTotal();

        return new PageImpl<>(memberList, pageable, totalCount);
    }

    @Override
    public Page<MemberDTO> pagingSearch2(MemberSearchCondition msc, Pageable pageable) {

        List<MemberDTO> memberList = queryFactory
                .select(fields(MemberDTO.class, member.name, member.age))
                .from(member)
                .where(conditionCheck(msc))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<MemberDTO> countQuery = queryFactory
                .select(fields(MemberDTO.class, member.name, member.age))
                .from(member)
                .where(conditionCheck(msc));

        //검색된 list사이즈가 페이지의 사이즈보다 작거나 페이지가 1개일 경우
        //카운트쿼리를 생략하고 list의 사이즈를 totalCount로 가져가는 방식
        return PageableExecutionUtils.getPage(memberList, pageable, countQuery::fetchCount);
    }

    private BooleanExpression conditionCheck(MemberSearchCondition msc) {

        return memberLk(msc.getMemberName())
                .and(teamEq(msc.getTeamName()))
                .and(ageGoeEq(msc.getAgeGoe()))
                .and(ageLoeEq(msc.getAgeLoe()));
    }

    private BooleanExpression memberLk(String memberName) {
        return memberName == null ? null : member.name.like(memberName);
    }

    private BooleanExpression teamEq(String teamName) {
        return teamName == null ? null : team.name.eq(teamName);
    }

    private BooleanExpression ageGoeEq(Integer age) {
        return age == null ? null : member.age.goe(age);
    }

    private BooleanExpression ageLoeEq(Integer age) {
        return age == null ? null : member.age.loe(age);
    }

}
