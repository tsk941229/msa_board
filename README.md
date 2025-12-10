# msa_board

스프링부트로 직접 만들면서 배우는 대규모 시스템 설계 - 게시판  
인프런 강의 (by 쿠케 님) 보면서 대규모 시스템 설계 학습 

---

### spec

 - JDK 21
 - Spring boot 3.3.2

---

### Mysql (innoDB)의 index와 활용 팁

#### 1. Clustered Index
 - 실제 데이터가 PK 기준으로 정렬되어 저장된 인덱스 구조

#### 2. Secondary Index
 - PK 외의 컬럼에 생성하는 인덱스
 - Secondary Index의 leaf node는 해당 인덱스 컬럼 값 + PK 값을 저장한다
 - 기본적으로 우리가 생성하는 index가 Secondary Index라고 이해했음

Secondary Index는 실제 데이터를 가지고 있지 않고, PK로 Clustered Index를 통해 실제 데이터에 접근 한다 (Double Lookup)

```mysql
-- Secondary Index 생성
create index idx_board_id_article_id on article(
    board_id asc, article_id desc
);
``` 

#### 3. Covering Index

 - 쿼리가 필요한 모든 컬럼이 인덱스에 전부 존재해서 클러스터드 인덱스를 다시 찾아갈 필요가 없는 인덱스
 - 쿼리가 인덱스만 보고 해결되고, 테이블에 접근하지 않아도 되기 때문에 조회 속도가 매우 빠르다

```mysql
select board_id, article_id from article limit 30 offset 1999970
``` 

