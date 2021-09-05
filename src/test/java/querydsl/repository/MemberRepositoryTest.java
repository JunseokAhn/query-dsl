package querydsl.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import querydsl.domain.Member;
import querydsl.domain.Team;
import querydsl.dto.MemberDTO;
import querydsl.dto.MemberSearchCondition;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    @BeforeEach
    public void init() throws Exception {

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
    public void search() throws Exception {

        //given
        MemberSearchCondition msc = new MemberSearchCondition();
        msc.setMemberName("member1");
        msc.setAgeLoe(20);

        //when
        List<MemberDTO> memberList = memberRepository.search(msc);

        //then
        assertEquals(memberList.size(), 1);

    }

    @Test
    public void pagingSearch() throws Exception {

        //given
        MemberSearchCondition msc = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);
        msc.setMemberName("%member%");

        //when
        Page<MemberDTO> memberPage = memberRepository.pagingSearch(msc, pageRequest);
        List<MemberDTO> memberList = memberPage.getContent();
        int totalPage = memberPage.getTotalPages();
        int currentPage = memberPage.getNumber();
        long totalElements = memberPage.getTotalElements();

        //then
        assertEquals(totalElements, 5);
        assertEquals(totalPage, 2);
        assertEquals(currentPage, 0);

    }


}