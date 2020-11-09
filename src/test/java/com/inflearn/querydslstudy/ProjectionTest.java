package com.inflearn.querydslstudy;

import com.inflearn.querydslstudy.dto.MemberDto;
import com.inflearn.querydslstudy.dto.QMemberDto;
import com.inflearn.querydslstudy.dto.UserDto;
import com.inflearn.querydslstudy.entity.Member;
import com.inflearn.querydslstudy.entity.QMember;
import com.inflearn.querydslstudy.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import static com.inflearn.querydslstudy.entity.QMember.member;

@SpringBootTest
@Transactional
public class ProjectionTest {

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
    void simple_projection() {
        List<String> member = queryFactory
                .select(QMember.member.username)
                .from(QMember.member)
                .fetch();

        System.out.println(member);
    }

    @Test
    void tuple_projection() {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    void findDtoBySetter() {
        List<MemberDto> memberDtos = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        memberDtos.forEach(System.out::println);

    }

    @Test
    void findDto_by_field() {
        List<MemberDto> memberDtos = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        memberDtos.forEach(System.out::println);

    }

    @Test
    void findDto_by_constructor() {
        List<MemberDto> memberDtos = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        memberDtos.forEach(System.out::println);

    }

    @Test
    void find_by_userDto() {
        List<UserDto> users = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"), member.age.as("agee")))
                .from(member)
                .fetch();

        users.forEach(System.out::println);
    }

    @Test
    void query_projection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }
}
