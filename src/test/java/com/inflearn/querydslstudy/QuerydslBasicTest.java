package com.inflearn.querydslstudy;

import com.inflearn.querydslstudy.entity.Member;
import com.inflearn.querydslstudy.entity.QMember;
import com.inflearn.querydslstudy.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static com.inflearn.querydslstudy.entity.QMember.member;
import static com.inflearn.querydslstudy.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

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
    void start_query_dsl_test() {
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(30))
                .fetchOne();

        assertThat(findMember.getTeam().getName()).isEqualTo("teamB");

    }

    @Test
    void search_and_param_test() {
        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .where(
                        member.username.eq("member1"),
                        member.age.between(10, 30)
                ).fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void result_featch_test() {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = queryFactory
                .selectFrom(QMember.member)
                .where(member.age.eq(10))
                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(QMember.member)
                .fetchFirst();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        assertThat(results.getTotal()).isEqualTo(4);
        assertThat(results.getResults().size()).isEqualTo(4);
    }

    /*
     * 1. 회원 나이 내림차순
     * 2. 회원 이름 올림차순
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     * */
    @Test
    void sort_test() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        assertThat(members.size()).isEqualTo(3);
        assertThat(members.get(0).getUsername()).isEqualTo("member5");
        assertThat(members.get(1).getUsername()).isEqualTo("member6");
        assertThat(members.get(2).getUsername()).isNull();

    }

    @Test
    void paging() {
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(results.getTotal()).isEqualTo(4);
    }

    @Test
    void aggregation() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);

    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .leftJoin(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple tuple1 = result.get(0);
        Tuple tuple2 = result.get(1);
        assertThat(tuple1.get(team.name)).isEqualTo("teamA");
        assertThat(tuple1.get(member.age.avg())).isEqualTo(15);

        assertThat(tuple2.get(team.name)).isEqualTo("teamB");
        assertThat(tuple2.get(member.age.avg())).isEqualTo(35);

    }

    /*
     * teamA에 소속된 모든 회원
     * */
    @Test
    void join() {
        List<Member> members = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        Member member1 = members.get(0);
        Member member2 = members.get(1);
        assertThat(members.size()).isEqualTo(2);
        assertThat(member1.getAge()).isEqualTo(10);
        assertThat(member2.getAge()).isEqualTo(20);
    }

    /*
     * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * */
    @Test
    void join_on_filtering() {
        List<Tuple> members = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(member.team.name.eq("teamA"))
//                .where(member.team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : members) {
            System.out.println(tuple);
        }
    }

    /*
     * 연관관계가 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     * */
    @Test
    void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        List<Tuple> tuples = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) // 일반조인과 다르게 id를 매칭 하지않가서 on절에 있는 조건으로만 조건을 검. member.team_id = team.id 조건을 만들지않으
                .fetch();

        tuples
                .forEach(System.out::println);
    }

    @Test
    void fetch_join_no() {
        queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .fetch();
    }

    @Test
    void fetch_join_use() {
        queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .fetch();
    }

    /*
     * 나이가 가장 많은 회원
     * */
    @Test
    void subquery() {
        QMember memberSub = new QMember("memberSub");
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                )).fetch();

        assertThat(members.size()).isEqualTo(1);
        assertThat(members.get(0).getAge()).isEqualTo(40);
    }

    /*
     * 나이가 평균이상인 회원
     * */
    @Test
    void subquery_goe() {
        QMember memberSub = new QMember("memberSub");
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                )).fetch();

        assertThat(members.size()).isEqualTo(2);
        assertThat(members.get(0).getAge()).isEqualTo(30);
        assertThat(members.get(1).getAge()).isEqualTo(40);
    }

    @Test
    void subquery_In() {
        QMember memberSub = new QMember("memberSub");
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                )).fetch();

        assertThat(members.size()).isEqualTo(3);
        assertThat(members.get(0).getAge()).isEqualTo(20);
        assertThat(members.get(1).getAge()).isEqualTo(30);
        assertThat(members.get(2).getAge()).isEqualTo(40);
    }

    @Test
    void select_subquery() {
        QMember memberSub = new QMember("memberSub");
        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        result
                .forEach(System.out::println);
    }

    @Test
    void basic_case() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    void concat() {
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        result.forEach(System.out::println);
    }

}
