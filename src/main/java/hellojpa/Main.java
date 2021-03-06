package hellojpa;

import hellojpa.entity.Member;
import hellojpa.entity.MemberType;
import hellojpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();// 트랜잭션 얻기
        tx.begin(); // 트랜잭션 시작

        try {

            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setName("sam");
            member.setAge(31);
            member.setMemberType(MemberType.ADMIN);
            member.setTeam(team);
            em.persist(member); // 저장


            em.flush(); // 디비에 쿼리를 보냄
            em.clear(); // 영속성 컨텍스트의 캐시를 비움

            Member findMember = em.find(Member.class, member.getId());

            findMember.setName("melchor");

            Team findTeam = findMember.getTeam();

            findTeam.getName(); // 지연로딩

            Team team2 = new Team();
            team2.setName("TeamB");

            em.persist(team2);

            member.setTeam(team2); // 수정

            String jpql = "SELECT m FROM Member m JOIN FETCH m.team WHERE m.name LIKE '%melchor%'";
            List<Member> result = em.createQuery(jpql, Member.class)
                    .setFirstResult(10)
                    .setMaxResults(20)
                    .getResultList();

            tx.commit(); // 커밋

        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close(); //종료

    }

}
