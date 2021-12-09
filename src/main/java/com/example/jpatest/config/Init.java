package com.example.jpatest.config;

import com.example.jpatest.entity.Member;
import com.example.jpatest.entity.Team;
import com.example.jpatest.repository.MemberRepository;
import com.example.jpatest.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class Init {

    private final BaseInfo baseInfo;

    @PostConstruct
    public void init(){
        baseInfo.insert();
    }

    @Transactional
    @RequiredArgsConstructor
    @Component
    static class BaseInfo{

        private final MemberRepository memberRepository;
        private final TeamRepository teamRepository;

        public void insert(){
            Team team = new Team();
            team.setTeamName("testTeam1");

            teamRepository.save(team);

            Member member = new Member();
            member.setMemberName("testMember1");
            member.setTeam(team);

            team.getMembers().add(member);

            memberRepository.save(member);
        }
    }
}
