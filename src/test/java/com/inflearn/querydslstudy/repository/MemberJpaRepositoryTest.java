package com.inflearn.querydslstudy.repository;

import com.inflearn.querydslstudy.dto.MemberSearchCondition;
import com.inflearn.querydslstudy.dto.MemberTeamDto;
import com.inflearn.querydslstudy.entity.Member;
import com.inflearn.querydslstudy.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basic_test() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> members = memberJpaRepository.findAllUseQueryDsl();
        assertThat(members).containsExactly(member);

        List<Member> result = memberJpaRepository.findByUsernameUseQuerydsl("member1");
        assertThat(result).containsExactly(member);
    }

    @Test
    void search_test() {
        //given
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

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(30);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

//        MemberSearchCondition condition2 = new MemberSearchCondition();
//        condition2.setTeamName("teamB");
        //when
        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByBuilder(condition);
//        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByBuilder(condition2);
        //then
        assertThat(memberTeamDtos.size()).isEqualTo(2);
        assertThat(memberTeamDtos).extracting("username").containsExactly("member3", "member4");
        assertThat(memberTeamDtos.get(0).getTeamName()).isEqualTo("teamB");
        assertThat(memberTeamDtos.get(0).getUsername()).isEqualTo("member3");

    }


}