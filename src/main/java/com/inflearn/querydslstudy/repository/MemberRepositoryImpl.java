package com.inflearn.querydslstudy.repository;

import com.inflearn.querydslstudy.dto.MemberSearchCondition;
import com.inflearn.querydslstudy.dto.MemberTeamDto;
import com.inflearn.querydslstudy.dto.QMemberTeamDto;
import com.inflearn.querydslstudy.entity.Member;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;

import java.util.List;

import static com.inflearn.querydslstudy.entity.QMember.member;
import static com.inflearn.querydslstudy.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameCondition(condition.getUsername()),
                        teamNameCondition(condition.getTeamName()),
                        ageGoeCondition(condition.getAgeGoe()),
                        ageloeCondition(condition.getAgeLoe())
                )
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameCondition(condition.getUsername()),
                        teamNameCondition(condition.getTeamName()),
                        ageGoeCondition(condition.getAgeGoe()),
                        ageloeCondition(condition.getAgeLoe())
                )
                .offset(pageable.getOffset()) // 몇번째부터 시작
                .limit(pageable.getPageSize()) // 몇개까지
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> contents = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameCondition(condition.getUsername()),
                        teamNameCondition(condition.getTeamName()),
                        ageGoeCondition(condition.getAgeGoe()),
                        ageloeCondition(condition.getAgeLoe())
                )
                .orderBy(member.id.asc(), member.username.desc())
                .offset(pageable.getOffset()) // 몇번째부터 시작
                .limit(pageable.getPageSize()) // 몇개까지
                .fetch(); // content 만 가져옴

        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameCondition(condition.getUsername()),
                        teamNameCondition(condition.getTeamName()),
                        ageGoeCondition(condition.getAgeGoe()),
                        ageloeCondition(condition.getAgeLoe())
                );

//        return new PageImpl<>(contents, pageable, countQuery.fetchCount());
        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchCount);
        // getPage에서 content와 pageable의 totalsize를 보고 첫번째페이지나 마지막페이지이면 카운트쿼리메소드를 호출을 안함. 최적화 !
        // contents 사이즈가 page 사이즈보다 작을경우 count 쿼리를 날릴 필요없이 content 사이즈를 리턴하면 된다. 이런경우 카운트쿼리를 실행하지 않음 !
    }

    private BooleanExpression usernameCondition(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameCondition(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoeCondition(Integer age) {
        return age != null ? member.age.goe(age) : null;
    }

    private BooleanExpression ageloeCondition(Integer age) {
        return age != null ? member.age.loe(age) : null;
    }
}
