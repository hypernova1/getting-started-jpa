package hellojpa;

import hellojpa.entity.Member;
import hellojpa.entity.MemberType;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();// 트랜잭션 얻기
        tx.begin(); // 트랜잭션 시작

        try {
            Member member = new Member();
            member.setName("sam");
            member.setAge(31);
            member.setMemberType(MemberType.ADMIN);
            em.persist(member); // 저장
            tx.commit(); // 커밋

        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close(); //종료

    }

}
