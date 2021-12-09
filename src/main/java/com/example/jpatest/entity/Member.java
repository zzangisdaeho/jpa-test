package com.example.jpatest.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long memberSeq;

    private String memberName;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

}
