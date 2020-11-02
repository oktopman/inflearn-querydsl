package com.inflearn.querydslstudy.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserTest {

    @Autowired
    EntityManager em;

    @Test
    void create_user_test() {
        User user = new User();
        em.persist(user);

        JPAQueryFactory query = new JPAQueryFactory(em);
        QUser qUser = QUser.user;

        User result = query
                .selectFrom(qUser)
                .fetchOne();

        assertThat(user).isEqualTo(result);
        assertThat(user.getId()).isEqualTo(result.getId());
    }


}