package com.inflearn.querydslstudy.entity;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Table(name = "user")
public class User {

    @Id @GeneratedValue
    private Long id;
}
