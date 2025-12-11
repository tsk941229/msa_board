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
select board_id, article_id from article limit 30 offset 1499970
```

#### Covering Index를 활용하여 쿼리 성능을 최적화 해보자

article 테이블을 아래와 같이 조회하면 내 환경 기준 약 6초가 걸린다 (정상 서비스 힘든 수준)

```mysql
select * from article where board_id = 1 order by article_id desc limit 30 offset 1499970;
```

위 쿼리가 느린 이유는 offset 1499970 때문에 1499970건을 스킵하는 과정에서 많은 데이터를 읽어야 하기 때문인데,    
하지만 미리 만들어둔 `idx_board_id_article_id` 인덱스의 `board_id`, `article_id` 컬럼만 이용해 서브쿼리에서 필요한 PK 30개만 먼저 가져온 뒤,  
그 PK를 기준으로 다시 전체 데이터를 조인하면 조회 속도가 크게 개선된다  

```mysql
select * from (
    select board_id, article_id from article where board_id = 1 order by article_id desc limit 30 offset 1499970
) t
left join article on t.article_id = article.article_id;
```
이 방식은 서브쿼리 단계에서 인덱스만 읽어 불필요한 조회를 피하고, 최종적으로 필요한 30건만 조인하므로 속도가 매우 빨라진다  
Index-Only Scan -> PK lookup

---
