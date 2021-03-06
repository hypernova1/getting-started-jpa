1. [JPA 들어가기](#JPA-들어가기)
2. [실습](#실습)
3. [필드와 컬럼매핑](#필드와-컬럼-매핑)
4. [연관관계 매핑](#연관관계-매핑)
5. [JPA 내부구조](#JPA-내부구조)
6. [JPA와 객체지향 쿼리](#JPA와-객체지향-쿼리)
7. [JPA 기반 프로젝트](#JPA-기반-프로젝트)

# JPA 들어가기

## ORM(Object Relational Mapping)

* 객체는 객체대로, 데이터베이스는 데이터베이스대로 설계
* ORM 프레임워크가 중간에서 설계
* 대중 적인 언어에는 대부분 ORM 기술이 존재



## SQL 중심 개발의 문제점

### 무한 반복, 지루한 코드

1. Insert, Update, Select, Delete...

~~~ java
public class Member {
  private String memberId;
  private String name;
  ...
}
~~~

~~~ mysql
INSERT INTO MEMBER (MEMBER_ID, NAME) VALUES
SELECT MEMBER_ID, NAME FROM MEMBER M
UPDATE MEMBER SET ...
~~~

* 전화번호 항목이 추가 되면 필요한 모든 쿼리에 전화번호 컬럼을 추가해야함

### 계층형 아키텍처

- 진정한 의미의 계층 분할이 어렵다.

### SQL에 의존적인 개발을 피하기 어렵다.

### 패러다임의 불일치

#### 1. 객체 VS 관계형 데이터베이스

#### 2. 객체를 관계형 데이터베이스에 저장

* 개발자가 손수 SQL을 매핑해줘야한다.

#### 3. 객체와 관계형 데이터베이스의 차이

##### 상속

1. 여러개의 상품이 있다고 가정할 경우 데이터베이스에서 구현할 때 수퍼 타입 (Item) 테이블을 만들고 서비타입(Album, Movie, Book 등)은 수퍼타입의  PK값을 가지고 조인을 해서 사용한다
2. 하지만 아이템마다 조회쿼리를 짤 때 조인 쿼리를 **개발자가 직접** 짜야 한다..
3. 아이템이 추가될때마다..

##### 연관관계

1. 객체는 **참조**를 사용: member.getTeam()

2. 테이블은 **외래키**를 사용: JOIN ON M.TEAM_ID = T.TEAM_ID

3. 객체를 테이블에 맞추어 모델링

   - ~~~java
     class Member {
       String id;	//MEMBER_ID
       Long teamId;	//TEAM_ID (FK)
       String username;	//USERNAME
     }
     
     class Team {
       Long id;	//TEAM_ID (PK)
       String name;	//NAME
     }
     ~~~

     

4. 객체 그래프 탐색

   * 객체는 자유롭게 객체 그래프를 탐색할 수 있어야 한다.

5. 처음 실행하는 SQL에 따라 탐색 범위 결정

   * ~~~ mysql
     SELECT M.*, T.*
     FROM MEMBER M
     JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID
     ~~~

     ~~~java
     member.getTeam(); 	//OK
     member.getOrder(); 	//null
     ~~~

6. 모든 객체를 미리 로딩할 수는 없다.

   * 상황에 따라 동일한 회원 조회 메서드를 여러번 생성

     * ~~~java
       memberDao.getMember(); //Member만 조회
       memberDao.getMemberWithTeam(); //Member와 Team조회
       
       //Member, Order, Delivery
       memberDao.getMemberWithOrderWithDelivery();
       ~~~

7. 비교하기

   * MyBatis 사용

     ~~~java
     String memberId = "100";
     Member member1 = memberDao.getMamber(memberId);
     Member member2 = memberDao.getMamber(memberId);
     
     member1 == member2; //false
     ~~~

   * 자바 컬렉션에서 조회

     ~~~java
     String memberId = "100";
     Member member1 = list.get(memberId);
     Member member2 = list.get(memberId);
     
     member1 == member2; //true
     ~~~

##### 데이터 타입

##### 데이터 식별 방법



## JPA를 사용해야하는 이유

### SQL 중심적인 개발에서 객체 중심으로 개발

### 생산성

* 저장: `jpa.persist(member)`
* 조회: `Member member = jpa.find(memberId)`
* 수정: `mebmer.setName("변경할 이름")`
* 삭제: `jpa.remove(member)`

### 유지보수

*  JPA 필드만 추가하면 됨

### 패러다임의 불일치 해결

### 성능

1. 1차 캐시와 동일성 보장

   * 동일한 트랜잭션에서 조회한 엔티티는 **동일성을 보장**

2. 트랜잭션을 지원하는 쓰기 지연

   1. 트랜잭션을 커밋할 때 까지 INSERT SQL을 모음

   2. JDBC BATCH SQL기능을 사용해서 **한번에 SQL을 전송**

3. 지연 로딩과 즉시로딩

   * 지연로딩: 객체가 실제 사용될 때 로딩
   * 즉시로디이: JOIN SQL로 한 번에 연관된 객체까지 미리 조회

### 데이터 접근 추상화와 벤더 독립성

### 표준


# 실습
### 객체 매핑하기
* `@Entity`: JPA가 관리할 객체

* `@Id`: DB PK와 매핑할 필드

* ~~~java
  @Entity
  public class Member {
    @Id
    private Long id;
    private String name;
  }
  ~~~

* ~~~mysql
  create table Member (
  	id bigint not null,
    name varchar(255),
    primary key (id)
  )
  ~~~



### 데이터베이스 방언

* JPA는 특성 데이터베이스에 종속적이지 않은 기술
* 각각의 데이터베이스가 제공하는 SQL 문법과 함수는 조금씩 다르다
  * 가변 문자: MySQL은 `VARCHAR`, Oracle은 `VARCHAR2`
  * 문자열을 자르는 함수: SQL 표준은 `SUBSTRING()`, Oracle은 `SUBSTR()`
  * 페이징: MySQL은 `LIMIT`, Oracle은 `ROWNUM`
* 방언: SQL 표준을 지키지 않거나 특정 데이터베이스만의 고유한 기능

### 애플리케이션 개발

### 주의

* 엔티티 매니저 팩토리는 하나만 생성해서 애플리케이션 전체에서 공유
* 엔티티 매니저는 쓰레드간 공유하면 안된다. (사용하고 바로 자원 해제)
* JPA의 모든 데이터 변경은 트랜잭션 안에서 실행


# 필드와 컬럼 매핑


## 데이터베이스 스키마 자동 생성하기

* DDL을 애플리케이션 실행 시점에서 자동 생성
* 테이블 중심 -> 객체 중심
* 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성
* 이렇게 생성된 DDL은 **개발장비에서만 사용**
* 생성된 DDL은 운영서버에서는 사용하지 않거나, 적절히 다듬은 후 사용

## 데이터베이스 스키마 자동 생성하기
* `hibernate.hbm2ddl.auto`
    * `create`: 기존 테이블 삭제후 다시 생성
    * `create-drop`: create와 같으나 종료시점에 DROP
    * `update`: 변경 부분만 반영 (**운영에서는 사용 X**)
    * `validate`: 엔티티와 테이블이 정상 매핑되었는지만 확인
    * `none`: 사용하지 않음
    
## 매핑 어노테이션
* `@Column`
  * 가장 많이 사용됨
  * `name`: 필드와 매핑할 테이블의 컬럼 이름
  * `insertable`, `updatable`: 읽기 전용
  * `nullable`: null 허용 여부 결정, DDL 생성시 사용
  * `unique`: 유니크 제약 조건, DDL 생성시 사용
  * `columnDefinition`, `length`, `precision`, `scale` (DDL)
* `@Temporal`
  * 날짜 타입 매핑
* `@Enumerated`
  * 열거형 매핑
  * `EnumType.ORDINAL`: 순서를 저장(기본값)
  * **`EnumType.STRING`**: 열거형 이름을 그대로 저장
* `@Lob`
  * CLOB, BLOB 매핑
  * `CLOB`: `String` ,`char[]`, `java.sql.CLOB`
  * `BLOB`: `byte[]`, `java.sql.BLOB`
* `@Transient`
  * 이 어노테이션이 선언된 필드는 매팽하지 않음
  * 애플리케이션에서 DB에 저장하지 않는 필드



## 식별자 매핑

### 식별자 매핑 방법

* `@Id`(직접 매핑)
* IDENTITY: 데이터베이스에 위임, MySQL
* SEQUENCE: 데이터베이스 시퀀스 오브젝트 사용, ORACLE
  * `@SequenceGenerator` 필요
* TABLE: 키 생성용 테이블 사용, 모든 DB에서 사용
  * `@TableGenerator` 필요
* AUTO: 방언에 따라 자동 지정, 기본 값

### 권장하는 식별자 전략

* 기본 키 제약 조건: null 아님, 유일, **불변**
* 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다. *대리키(대체키)를 사용하자*
  * 예를 들어 주민등록번호는 기본 키로 적절하지 않다
* **권장: Long + 대체키 + 키 생성 전략 사용**



# 연관관계 매핑

## 객체를 테이블에 맞추어 모델링

* 참조 대신에 외래키를 그대로 사용

* ~~~java
  // 팀저장
  Team team = new Team();
  team.setName("TeamA");
  em.persist(team);
  
  // 회원 저장
  Member member = new Member();
  mebmer.setName("member1");
  member.setTeamId(team.getId);
  em.persist(member);
  
  // 조회
  Member findMember = em.find(Member.class, member.getId());
  Long teamId = findMember.getTeamId();
  
  // 연관관계가 없음
  Team findTeam = em.find(Team.class, teamId);
  ~~~

* 객체지향적인 방법이 아님

* 객체를 테이블에 맞추어 데이터 중심으로 모델링하면, **협력 관계를 만들 수 없다.**

  * **테이블은 외래키로 조인**을 사용해서 연관된 테이블을 찾는다.
  * **객체는 참조**를 사용해서 연관된 객체를 찾는다.
  * 테이블과 객체 사이에는 위와 같은 큰 차이가 있다.

## 연관관계 매핑 이론

### 단방향 매핑

* ~~~java
  //팀저장
  Team team = new Team();
  team.setName("TeamA");
  em.persist(team);
  
  //회원저장
  Member member = new Member();
  member.setName("member1");
  member.setTeam(team); //단방향 연관관계 설정, 참조 저장
  em.persist(member);
  
  //조회
  Member findMember = em.find(Member.class, member.getId());
  
  // 참조를 사용해서 연관관계 조회
  Team findTeam = member.getTeam();
  ~~~

* 객체는 참조를 사용해서 연관관계를 조회할 수 있다. 이 것을 객체 그래프 탐색이라 한다.



### 양방향 매핑

~~~java
Team findTeam = em.find(Team.class, team.getId());

int memberSize = findTeam.getMembers().size(); //역방향 조회
~~~

* 반대 방향으로 객체 그래프 탐색

* 객체와 테이블이 관계를 맺는 차이

  * **객체 연관관계**
    * 회원 -> 팀 연관관계 1개 (단방향)
    * 팀 -> 회원 연관관계 1개 (단방향)
  * **테이블 연관관계**
    * 회원 <-> 팀 연관괸계 1개 (양방향)

* 객체의 양방향 연관관계

  * 객체의 양방향 관계는 사실 **양방향 관계가 아니라 서로 다른 단방향 관계 2개다.**

* 테이블의 양방향 연관관계

  * 테이블은 **외래키 하나**로 두 테이블의 연관관계를 관리

  * MEMBER.TEAM_ID 외래키 하나로 양방향 연관관계를 가짐 (양쪽으로 조인 가능)

  * ~~~mysql
    SELECT *
    FROM MEMBER M
    JOIN TEAM T
    ON M.TEAM_ID = T.TEAM_ID
    
    SELECT *
    FROM TEAM T
    JOIN MEMBER M
    ON T.TEAM_ID = M.TEAM_ID
    ~~~

* **연관관계의 주인** (Owner)

  * **양방향 매핑 규칙**
    * 객체의 두 관계 중 하나를 연관관계의 주인으로 지정
    * **연관관계의 주인만이 외래 키를 관리** (등록, 수정)
    * **주인이 아닌 쪽은 읽기만 가능**
    * 주인은 `mappedBy` 속성 사용 X
    * 주인이 아니면 `mappedBy` 속성으로 주인 지정
  * 누구를 주인으로?
    * 외래 키가 있는 곳을 주인으로 정해라

* 양방향 매핑시 가장 많이 하는 실수 

  * 연관 관계의 주인에 값을 입력하지 않음

  * ~~~java
    Team team = new Team();
    team.setName("TeamA");
    em.persist(team);
    
    Member member = new Member();
    member.setName("member1");
    
    //역방향(주인이 아닌 방향)만 연관관계 설정
    team.getMembers().add(member);
    em.persist
    ~~~

    * |  ID  | USERNAME | TEAM_ID  |
      | :--: | :------: | :------: |
      |  1   | member1  | **null** |

  * 해결 방법

    * 순수한 객체 관계를 고려하면 항상 **양쪽 다 값을 입력해야함**

* 양방향 매핑의 장점

  * **단방향 매핑만으로도 이미 연관관계 매핑은 완료**
  * 양방향 매핑은 반대방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐
  * JPQL에서 역방향으로 탐색할 일이 많음
  * 단방향 매핑을 잘 하고 양방향 매핑은 필요할 때 추가해도됨 (테이블에 영향을 주지 않음)



### 연관관계 매핑 어노테이션

* 다대일: `@ManyToOne`
* 일대다: `@OneToMay`
* 일대일: `@OneToOne`
* 다대다: `@ManyToMany`
* `@JoinColumn`, `@JoinTable`

### 상속 관계 매핑 어노테이션

* `@Inheritance`
* `@DiscriminatorColumn`
* `@DiscrimiatorValue`
* `@MappedSuperClass` (매핑 속성만 상속)

### 복합치 어노테이션

* `@IdClass`
* `@EmbeddedId`
* `@Embeddable`
* `@MapsId`



# JPA 내부구조

## 영속성 컨텍스트

* JPA를 이해하는데 가장 중요한 용어
* "*엔티티를 영구 저장하는 환경*"이라는 뜻
* `EntityManager.persist(entity)`
* 엔티티 매니저? 영속성 컨텍스트?
  * 영속성 컨텍스트는 논리적인 개념
  * 눈에 보이지 않는다
  * 엔티티 매니저를 통해서 영속성 컨텍스트에 접근

## 엔티티의 생명주기

* 비영속 (new/transient)

  * 영속성 컨텍스트와 전혀 관계가 없는 상태

  * ~~~java
    Member member = new Member();
    member.setId("member1");
    member.setUsername("회원");
    ~~~

* 영속 (managed)

  * 영속성 컨텍스트에 저장된 상태

  * ~~~java
    EntityManager em = emf.createEntityManager();
    em.getTransaction().begin();
    
    //객체를 저장한 상태(영속)
    em.persist(member);
    ~~~

* 준영속 (detached)

  * 영속성 컨텍스트에 저장되어 있다가 분리된 상태

  * ~~~java
    em.detach(member);
    ~~~

* 삭제 (removed)

  * 삭제된 상태

  * ~~~java
    em.remove(member);
    ~~~



## 영속성 컨텍스트의 이점

* 1차 캐시

  * ~~~java
    Member member = new Member();
    member.setId("member1");
    member.setUsername("회원1");
    
    //1차 캐시에 저장됨
    em.persist(member);
    
    //1차 캐시에서 조회
    Member findMember = em.find(Member.class, "member1");
    
    //1차 캐시에 없으므로 DB에서 조회 > 1차 캐시에 저장 > 반환
    Member findMember2 = em.find(Member.class, "member2");
    ~~~

* 동일성(identity) 보장

  * ~~~java
    Member a = em.find(Member.class, "member1");
    Member b = em.find(Member.class, "member2");
    
    System.out.println(a == b); //true
    ~~~

  * 1차 캐시로 반복 가능한 읽기(Repeatable Read) 등급의 트랜잭션 격리 수준을 데이터베이스가 아닌 애플리케이션 차원에서 제공

* 트랜잭션을 지원하는 쓰기 지연(transactional write-behind)

  * ~~~java
    EntityManager em = emf.createEntityManager();
    EntityTransaction transaction = em.getTransaction();
    //엔티티 매니저는 데이터 변경시 트랜잭션을 시작해야 한다.
    transaction.begin();
    
    em.persist(memberA);
    em.persist(memberB);
    //여기까지 INSERT SQL을 데이터베이스에 보내지 않는다
    
    //커밋하는 순간 데이터베이스에 INSERT SQL을 보낸다
    transaction.commit();
    ~~~

* 변경감지(Dirty Checking)

  * ~~~java
    EntityManager em = emf.createEntityManager();
    EntityTransaction transaction = em.getTransaction();
    transaction.begin(); //트랜잭션 시작
    
    //영속 엔티티 조회
    Member memberA = em.find(Member.class, "memberA");
    
    //영속 엔티티 수정
    memberA.setUsername("sam");
    memberA.setAge(31);
    
    transaction.commit(); //커밋
    ~~~

  * 1차 캐시가 생성되는 순간 스냅샷을 만들어두고 변경점이 있다면 UPDATE SQL을 데이터베이스에 보냄

* 지연로딩(Lazy Loading)



### 플러시 발생

* 변경 감지
* 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
* 쓰기 지연 SQL 저장소의 쿼리를 데이터베이승에 전송(등록, 수정, 삭제)

### 영속성 컨텍스트를 플러시하는 방법

* `em.flush()`: 직접 호출

* 트랜잭션 커밋: 플러시 자동 호출

* JPQL 쿼리 실행: 플러시 자동 호출

  * ~~~java
    em.persist(memberA);
    em.persist(memberB);
    em.persist(memberC);
    
    //중간에 JPQL 실행
    query = em.creatQuery("SELECT m FROM Member m" ,Member.class);
    List<Member> members = query.getResultList();
    ~~~

### 플러시 주의할 점

* 영속성 컨텍스트를 비우지 않음
* 영속성 컨텍스트의 변경내용을 데이터베이스에 동기화
* 트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화



### 준영속 상태

* 영속 -> 준영속
* 영속 상태의 엔티티가 영속성 컨텍스트에서 분리
* 영속성 컨텍스트가 제공하는 기능을 사용하지 못함
* 준영속 상태로 만드는 법
  * `em.detach(entity)`: 특정 엔티티만 준영속 상태로 전환
  * `em.clear()`: 영속성 컨텍스트를 완전히 초기화
  * `em.close`: 영속성 컨텍스트 종료



### Member를 조회할 때 Team도 함께 조회해야 할까?

* 단순히 Member만 조회하는 비즈니스 로직 `member.getName()`

  * 지연 로딩 LAZY를 사용해서 프록시로 조회

  * ~~~java
    Member member = em.find(Member.class, 1L); //여기서 Team은 프록시 객체(가짜 객체)로 저장된다.
    Team team = member.getTeam();
    team.getName(); //실제 team을 사용하는 시점에서 초기화(DB조회)
    ~~~

* Member와 Team을 자주 함께 사용한다면

  * 즉시로딩 EAGLE을 사용해서 함께 조회
  * JPA 구현체는 가능하면 조인을 사용해서 SQL 한 번에 함께 조회



### 프록시와 즉시 로딩 주의

* **가급적 지연로딩을 사용**
* 즉시 로딩을 적용하면 예상하지 못한 SQL이 발생
* 즉시 로딩은 JPQL에서 N+1 문제를 일으킨다.
* `@ManyToOne`, `@OneToOne`은 기본이 즉시 로딩
  * LAZY로 설정
* `@OneToMany`, `ManyToMany`는 기본이 지연 로딩



# JPA와 객체지향 쿼리

## JPQL 소개

### 가장 단순한 조회방법

* `EntityManager.find()`
* 객체 그래프 탐색 (`a.getB().getC()`)
* 나이가 18살 이상인 회원을 모두 검색하고 싶다면?
  * 문제는 검색 쿼리
  * 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
  * 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요



### JPQL

* 객체지향 SQL

* SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않음

* 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색

* SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원

* ~~~java
  //검색
  String jpql = "SELECT m FROM Member m WHERE m.name like '%hello%'";
  List<Member> result = em.createQuery(jpql, Member.class).getResultList();
  ~~~



### JPQL 문법

* `SELECT m FROM Member m WHERE m.age > 18`
* 엔티티와 속성은 대소문자 구분(Member, username)
* JPQL 키워드는 대소문자 구분 안 함(SELECT, FROM ,WHERE)
* 엔티티 이름을 사용(테이블이름이 아님)
* 별칭은 필수(m)

### 결과 조회 API

* `query.getResultList()`
  * 결과가 하나 이상
  * 리스트 반환
* `query.getSingleResult()`
  * 결과가 정확히 하나
  * 단일 객체 반환
  * 하나가 아니면 예외 발생

### 파라미터 바인딩 - 이름 기준, 위치 기준

~~~java
SELECT m FROM Member m WHERE m.username=:username
query.setParameter("username", usernameParam);

SELECT m FROM Member m WHERE m.username=:?1
query.setParameter(1, usernameParam);
~~~



### 프로젝션

* `SELECT m FROM Member m`: 엔티티 프로젝션
* `SELECT m.team FROM Member m`: 엔티티 프로젝션
* `SELECT username, age FROM Member m`: 단순 값 프로젝션
* `SELECT new jpabook.jpql.UserDTO(m.username, m.age) FROM Member m`:  `new` 명령어 (단순 값을 DTO로 바로 조회)
* DISTINCT는 중복 제거



### 페이징 API

* JPA는 페이징을 다음 두 API로 추상화

* `setFirstResult(int startPosition)`: 조회 시작 위치 (0부터 시작)

* `setMaxResults(int maxResult)`: 조회할 데이터 수

* ~~~java
  String jpql = "SELECT m FROM Member m ORDER BY m.name DESC";
  List<Member> resultList = em.createQuery(jpql, Member.class)
    .setFistResult(10)
    .setMaxResult(20)
    .getResultList();
  ~~~



### 집합과 정렬

* ~~~sql
  SELECT
  	COUNT(m),		//회원수
  	SUM(m.age), //나이의 합
  	AVG(m.age), //평균 나이
  	MAX(m.age), //최대 나이
  	MIN(m.age)  //최소 나이
  FROM Member m
  ~~~



### 조인

* ~~~sql
  SELECT m FROM Member m [INNER] JOIN m.team t //내부조인
  SELECT m FROM Member m LEFT [OUTER] JOIN m.team t //외부 조인
  SELECT count(m) FROM Member m, Team t WHERE m.username = t.name //세타조인
  ~~~

* 참고: 하이버네이트 5.1부터 세타 조인도 외부 조인 가능



### 페치 조인

* 엔티티 객체 그래프를 한 번에 조회하는 방법

* 별칭 사용 불가

* ~~~sql
  SELECT m FROM Member m JOIN FETCH m.team //JPQL
  
  SELECT M.*, T.* FROM MEMBER T INNER JOIN JOIN TEAM T ON M.TEAM_ID = T.ID //SQL
  ~~~

  

* ~~~java
  String jpql = "SELECT m FROM Member m JOIN FETCH m.team";
  List<Member> members = em.createQuery(jpql, Member.class)
    .getResultList();
  
  for (Member member : members) {
    //페치 조인으로 회원과 팀을 함께 조회해서 지연 로딩 발생 안 함
    System.out.println("username = " + member.getUserName() + ", teamname = " + member.getTeam().name());
  }
  ~~~



### JPQL 기타

* 서브 쿼리 지원
* EXISTS, IN
* BETWEEN, LIKE, IS NULL

### JPQL 기본 함수

* CONCAT
* SUBSTRING
* TRIM
* LOWER, UPPER
* LENGTH
* LOCATE
* ABS, SQRT, MOD
* SIZE, INDEX (JPA 용도)

### Named 쿼리 - 정적 쿼리

* 미리 정의해서 이름을 부여해두고 사용하는 JPQL

* 어노테이션, XML에 정의

* 애플리케이션 로딩 시점에서 초기화 후 재사용

* **애플리케이션 로딩 시점에 쿼리를 검증**

* ~~~java
  @Entity
  @NamedQuery(
  	name = "Member.findByUsername",
  	query = "SELECT m FROM Member m WHERE m.username = :username")
  public class Member {
    ...
  }
  
  List<Member> resultList = em.createNamedQuery("Member.findByUsername", Member.class)
    .setParameter("username", "회원1")
    .getResultList();
  ~~~



# JPA 기반 프로젝트

## Spring Data JPA

* 지루하게 반복되는 CRUD 문제를 세련된 방법으로 해결
* 개발자는 인터페이스만 작성
* 스프링 데이터 JPA가 구현 객체를 동적으로 생성해서 주입
* 메서드 이름만으로 JPQL 쿼리 생성

## QueryDSL

* SQL, JPQL을 코드로 작성할 수 있도록 도와주는 빌더 API
* JPA 크리테리아에 비해서 편하고 실용적
* 오픈소스

### SQL, JPQL의 문제점

* 문자라서 Type-check 불가능
* 해당 로직 실행 전까지 작동여부 확인 불가

### QueryDSL의 장점

* 문자가 아닌 **코드**로 작성

* **컴파일 시점에 문법 오류 발견**

* 코드 자동완성 (IDE 도움)

* 단순하고 쉬움: 코드 모양이 JPQL과 거의 비슷

* 동적 쿼리

* ~~~java
  //JPQL
  //SELECT m FROM Member m WHERE m.age > 18
  
  JPAFactoryQuery query = new JPAQueryFactory(em);
  QMember m = QMember.member;
  List<Member> list =
    	query.selectFrom(m)
    			 .where(m.age.gt(18))
    			 .orderBy(m.name.desc())
    			 .fetch();
  ~~~
