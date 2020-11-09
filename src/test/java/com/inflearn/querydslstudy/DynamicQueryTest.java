package com.inflearn.querydslstudy;

import com.inflearn.querydslstudy.entity.Member;
import com.inflearn.querydslstudy.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static com.inflearn.querydslstudy.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class DynamicQueryTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void setup() {
        queryFactory = new JPAQueryFactory(em);
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

    @Test
    void dynamic_query_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCondition, Integer ageCondition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCondition != null)
            builder.and(member.username.eq(usernameCondition));

        if (ageCondition != null)
            builder.and(member.age.eq(ageCondition));
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    void dynamic_query_whereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCondition, Integer ageCondition) {
        return queryFactory
                .selectFrom(member)
                .where(allEq(usernameCondition, ageCondition))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCondition) {
        return usernameCondition != null ? member.username.eq(usernameCondition) : null;
    }

    private BooleanExpression ageEq(Integer ageCondition) {
        return ageCondition != null ? member.age.eq(ageCondition) : null;
    }

    private BooleanExpression allEq(String usernameCondition, Integer ageCondition) {
        return usernameEq(usernameCondition).and(ageEq(ageCondition));
    }
}
