package com.inflearn.querydslstudy.repository;

import com.inflearn.querydslstudy.dto.MemberSearchCondition;
import com.inflearn.querydslstudy.dto.MemberTeamDto;
import com.inflearn.querydslstudy.entity.Member;
import com.inflearn.querydslstudy.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static com.inflearn.querydslstudy.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@Profile("test")
@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void basic_test() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> members = memberRepository.findAll();
        assertThat(members).containsExactly(member);

        List<Member> result = memberRepository.findByUsername("member1");
        assertThat(result).containsExactly(member);
    }

    @Test
    void search_test() {
        //given
        initTeamAndMember();

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

//        MemberSearchCondition condition2 = new MemberSearchCondition();
//        condition2.setTeamName("teamB");
        //when
        List<MemberTeamDto> memberTeamDtos = memberRepository.search(condition);
//        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByBuilder(condition2);
        //then
        assertThat(memberTeamDtos.size()).isEqualTo(2);
        assertThat(memberTeamDtos).extracting("username").containsExactly("member3", "member4");
        assertThat(memberTeamDtos.get(0).getTeamName()).isEqualTo("teamB");
        assertThat(memberTeamDtos.get(0).getUsername()).isEqualTo("member3");

    }

    @Test
    void search_page_simple_test() {
        initTeamAndMember();

        MemberSearchCondition condition = new MemberSearchCondition();

        //when
        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, PageRequest.of(0, 10));

        //then
//        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3", "member4");

    }

    @Test
    void search_page_complex_test() {
        initTeamAndMember();

        MemberSearchCondition condition = new MemberSearchCondition();

        //when
        Page<MemberTeamDto> result = memberRepository.searchPageComplex(condition, PageRequest.of(0, 10));

        //then
//        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3", "member4");

    }

    private void initTeamAndMember() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test // left join이 불가능하기때문에 복잡한 실무에서 잘 사용되지않음
    void querydsl_predicateexecutor_test() {
        initTeamAndMember();
        Iterable<Member> result = memberRepository.findAll(member.age.between(20, 40).and(member.username.eq("member3")));
        for (Member member : result) {
            System.out.println(member);
        }
    }

}