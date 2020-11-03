package com.inflearn.querydslstudy.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    void test_entity() {
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

       em.flush();
       em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        assertThat(members.size()).isEqualTo(4);
        assertThat(members.get(0).getTeam().getName()).isEqualTo("teamA");

    }

}