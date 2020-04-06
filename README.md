

# 1강

## ORM(Object Relational Mapping)

* 객체는 객체재로, 데이터베이스는 데이터베이스대로 설계
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
       String id;			//MEMBER_ID
       Long teamId;		//TEAM_ID (FK)
       String username;//USERNAME
     }
     
     class Team {
       Long id;				//TEAM_ID (PK)
       String name;		//NAME
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

   * 상황에 따라 동일한 회원 조회 메서드를 여러벌 생성

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
     
     member1 == member2; //다름
     ~~~

   * 자바 컬렉션에서 조회

     ~~~java
     String memberId = "100";
     Member member1 = list.get(memberId);
     Member member2 = list.get(memberId);
     
     member1 == member2; //같다
     ~~~

##### 데이터 타입

##### 데이터 식별 방법



## JPA를 사용해야하는 이유

### SQL 중심적인 개발에서 객체 중심으로 개발

### 생산성

* 저장: **jpa.persist(member)**
* 조회: **Member member = jpa.find(memberId)**
* 수정: **mebmer.setName("변경할 이름")**
* 삭제: **jpa.remove(member)**

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


# 2강
## 실습
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


# 3강
## 필드와 컬럼 매핑
### 데이터베이스 스키마 자동 생성하기
* DDL을 애플리케이션 실행 시점에서 자동 생성
* 테이블 중심 -> 객체 중심
* 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성
* 이렇게 생성된 DDL은 **개발장비에서만 사용**
* 생성된 DDL은 운영서버에서는 사용하지 않거나, 적절히 다듬은 후 사용

### 데이터베이스 스키마 자동 생성하기
* `hibernate.hbm2ddl.auto`
    * `create`: 기존 테이블 삭제후 다시 생성
    * `create-drop`: create와 같으나 종료시점에 DROP
    * `update`: 변경 부분만 반영 (**운영에서는 사용 X**)
    * `validate`: 엔티티와 테이블이 정상 매핑되었는지만 확인
    * `none`: 사용하지 않음
    
### 매핑 어노테이션
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



### 식별자 매핑

#### 식별자 매핑 방법

* `@Id`(직접 매핑)
* IDENTITY: 데이터베이스에 위임, MySQL
* SEQUENCE: 데이터베이스 시퀀스 오브젝트 사용, ORACLE
  * `@SequenceGenerator` 필요
* TABLE: 키 생성용 테이블 사용, 모든 DB에서 사용
  * `@TableGenerator` 필요
* AUTO: 방언에 따라 자동 지정, 기본 값

#### 권장하는 식별자 전략

* 기본 키 제약 조건: null 아님, 유일, **불변**
* 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다. *대리키(대체키)를 사용하자*
  * 예를 들어 주민등록번호는 기본 키로 적절하지 않다
* **권장: Long + 대체키 + 키 생성 전략 사용**

