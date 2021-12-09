package com.example.jpatest.controller;

import com.example.jpatest.entity.Member;
import com.example.jpatest.entity.Team;
import com.example.jpatest.repository.MemberRepository;
import com.example.jpatest.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HelloController {

    /**
     * JPA 관련하여
     *
     * cascade persist의 진실?
     *
     * 	cascade persist는 부모가 저장될때 자식들을 같이 저장하는것으로들 많이 생각한다.
     * 	하지만 진실은..
     * 	부모의 영속성을 통해 자식을 관리한다는 것이다.
     * 	똑같은 말처럼 들릴 수 있으나..
     * 	부모를 최초 저장할때 자식을 관리하는게 아니라...
     * 	부모의 영속성에서 자식의 리스트를 갖고 있으면 해당 자식은 같이 영속화된다는 의미이다.
     *
     * 	예를 들어
     * 	부모가 새로운 자식을 품으면 (list에 새로운 자식이add 되면), 해당 자식은 save()를 호출하지 않아도
     * 	부모의 영속성에 들어가있으므로 부모가 영속성을 관리하기에 저장된다.
     *
     * 	또 다른 예로
     * 	자식을 삭제할때, 부모가 자식의 영속성을 관리하면..
     * 	ex) 1번자식을 부모에서 get 하면.. -> parent.getChildList.get(1)
     * 	해당 자식은 부모의 영속성 컨텍스트 안에 들어온다.
     * 	이때 자식을 delete() 호출해도 자식은 지워지지 않는다.
     *
     * 	부모가 cascade persist로 자식의 영속성을 관리하기 때문에 자식을 delete 해주어도 부모의 자식 리스트에서 해당 자식을 제거하지 않으면 부모가 다시 영속화하려 하기 때문이다. (한쪽은 지우려하고, 부모쪽은 영속화 시키려 하는데 영속성 관리의 주체가 부모이기 때문에 부모가 힘이 더 쌔서 안지워진다.)
     *
     * 	고로 cascade persist에서 부모가 자식을 영속성 컨텍스트에 가져오면, 그 이후로 해당 자식을 지우는 방법은 delete()를 호출하고 부모에서 해당 자식을 remove시켜야 한다 -> parent.getChildList.remove(1)
     *
     * 	반대로 부모에서 해당 자식을 조회하지 않는 이상 영속성에 해당 자식은 들어오지 않고, 그 자식은 다른곳에서 직접 불러서 지울 수 있다.
     * 	ex) parent.getchildList.get(2) -> 2번 자식을 불러옴
     * 		child1.delete() -> 1번 자식을 삭제함 -> 성공
     *
     * 	ex) parent.getchildList -> 전체 자식을 불러옴. 하지만 특정 자식을 까지는 않음. 까기 전까지 영속성 컨텍스트에 들어오지 않음. 물론 fetch eager라면.. 부모를 가져오는 순간 모든 자식을 가져와서 영속화시켜버린다. 상식적으로 lazy를 쓰니까 lazy를 가정하고 말하는것.
     * 		child1.delete() -> 1번 자식을 삭제함 -> 성공.
     *
     * 결론 : cascade persist 는 부모가 자식의 영속성을 관리하겠다는것. 고로 부모에서 특정 자식을 영속성 컨텍스트에 로딩하는 순간 해당 자식은 !무.조.건! 영속화된다. 그러니 저장도 알아서 나가고 지울떄도 부모에서 제외시키지 않는 이상 삭제가 불가능하다.
     *
     * 참고 : cascade delete 옵션이 있어도.. cascade persist 옵션이 있어도.. childList에서 자식을 remove 시키는것 만으로는 삭제가 나가지 않는다.
     * 	영속성을 부모가 관리하는데 list에서 삭제하는것만으로는 삭제되지 않는게 의아할 수 있다.
     * 하지만 삭제는 좀 더 신중하라는 의미에서 함부로 날리지 않는 설계의도가 있다.
     * 고로 orphan remove 옵션을 별도로 만들어 고아객체 (list remove 된 객체)를 삭제하는 생명주기 옵션이 따로 존재한다. 부모를 통해서 자식을 삭제하고 싶으면 orphan 옵션을 사용해라.
     * 	-> orphan을 사용하면 부모 자체가 삭제될때, 자식도 고아가 된다. cascade remove와 마찬가지로 모든 자식이 삭제하려 할것이다. db에서 결함성 오류가 발견되지 않는 이상 삭제된다.
     */

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    // 부모에 Cascade Persist가 있을시 list에 자식을 추가하는것 만으로 해당 자식은 save() 를 불리는것과 같은 효과를 갖는다. -> casade 없으면 당연히 자식은 저장되지 않는다
    @Transactional
    @GetMapping("/plusMember")
    public void addMember(){
        Team team = teamRepository.findById(1L).get();

        Member member = new Member();
        member.setMemberName("addedMember");
        member.setTeam(team);

        team.getMembers().add(member);
    }

    // 부모에 Cascade Detach or Remove가 걸려있어도 list에서 자식을 제외하면 자식은 지워지지 않는다.
    @Transactional
    @GetMapping("/deleteMember")
    public void deleteMember(){
        Team team = teamRepository.findById(1L).get();

        team.getMembers().remove(0);
    }

    // 부모를 영속성에 넣지 않고 자식만 불러서 지우면 잘 지워진다
    @Transactional
    @GetMapping("/deleteWithoutTeam")
    public void deleteWithoutTeam(){
        Member member = memberRepository.findById(2L).get();

        memberRepository.delete(member);
    }

    // 자식을 갖고 있는 부모 entity만 조회해도 자식은 지워진다
    @Transactional
    @GetMapping("/deleteWithTeam")
    public void deleteWithTeam(){
        Team team = teamRepository.findById(1L).get();

        Member member = memberRepository.findById(2L).get();

        memberRepository.delete(member);
    }

    // 자식을 갖고 있는 부모 Entity에서 자식 리스트를 가져와도 자식은 지워진다
    @Transactional
    @GetMapping("/deleteWithTeam2")
    public void deleteWithTeam2(){
        Team team = teamRepository.findById(1L).get();

        List<Member> members = team.getMembers();

        // Debug로 찍지 않는이상 List<Member> 를 활용하는 로직이 없기에 getMembers의 쿼리는 나가지 않는다 -> 부모의 영속성 안에 자식 영속성이 로드되지 않는다.
        System.out.println("======================================");

        Member member = memberRepository.findById(2L).get();

        memberRepository.delete(member);
    }

    // 부모 Entity에서 자식 리스트 중 해당 자식을 가져오면 자식은 삭제되지 않는다.
    @Transactional
    @GetMapping("/deleteWithTeam3")
    public void deleteWithTeam3(){
        Team team = teamRepository.findById(1L).get();

        //member = member1
        Member member1 = team.getMembers().get(0);

        Member member = memberRepository.findById(2L).get();

        memberRepository.delete(member);
//        memberRepository.delete(member1);
    }

    // 부모 Entity에서 자식 리스트 중 해당 자식을 가져오면 부모에서 해당 자식을 remove해주면 삭제된다.
    // 결론 : 부모 Entity에서 자식 리스트 중 해당 자식을 가져오면 해당 자식은 부모의 영속성에 같이 관리된다.
    // 고로 부모에서 자식을 빼주지 않는 이상 자식은 지워지는데 부모의 영속성에는 남기 때문에 자식은 지워지지 않는다.
    @Transactional
    @GetMapping("/deleteWithTeam4")
    public void deleteWithTeam4(){
        Team team = teamRepository.findById(1L).get();

        Member member = memberRepository.findById(2L).get();

        team.getMembers().remove(member);

        memberRepository.delete(member);
    }

    //!!! 근데 부모의 영속성에 자식이 들어있어서 삭제되지 않는건 모두 cascade 옵션 때문이다.
    // cascade옵션이 없으면 부모가 해당 자식을 로드해도 삭제 잘만 된다.
    // cascade의 본질은 결국 부모가 자식의 생명을 관리하겠다는 것이고, 생명을 관리하는 부모가 자식을 물고있으면 자식을 삭제시켜도 삭제되지 않는 것이다.


}
