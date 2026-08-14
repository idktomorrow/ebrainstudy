# MyBatis Mapper XML 치트시트

`src/main/resources/mappers/` 안에는 절대 넣지 말 것 — `application.yaml`의
`mapper-locations: classpath:mappers/**/*.xml` 설정 때문에 그 폴더 안 `.xml`은
전부 실제 매퍼로 로드를 시도함. 여기 있는 예시는 실제 인터페이스와 매칭 안 되는
가짜 코드라서, 폴더 안에 넣으면 서버 기동 시 `ClassNotFoundException`으로 죽음.

## 기본 틀

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--
  위 4줄(XML 선언 + DOCTYPE)은 고정 템플릿.
  "이 파일은 XML 문서다" + "MyBatis mapper 문법 규칙을 따른다"는 선언.
  내용은 절대 안 바뀜 -> 새 XML 만들 때마다 그대로 복사.
-->

<!-- namespace: 이 XML과 짝지어질 Mapper 인터페이스의 전체 경로(패키지+클래스명) -->
<mapper namespace="com.study2.practice.example.mapper.ExampleMapper">

    <!-- 아래에 select/insert/update/delete 태그들이 들어감 -->

</mapper>
```

## SELECT — 조회

```xml
<!--
  id         : 인터페이스 메서드명과 정확히 일치해야 매칭됨
  resultType : 결과 한 건이 매핑될 타입.
               List<Example>로 여러 건 받아도 List가 아니라 요소 타입(Example)만 적음
-->
<select id="findAll" resultType="com.study2.practice.example.entity.Example">
    SELECT id, name
    FROM example
</select>

<!--
  #{파라미터명} : 인터페이스 메서드 파라미터를 SQL에 바인딩.
  JDBC의 PreparedStatement '?' 자리와 같은 역할 -> SQL Injection 방지됨.
  인터페이스: Example findById(Integer id);
-->
<select id="findById" resultType="com.study2.practice.example.entity.Example">
    SELECT id, name
    FROM example
    WHERE id = #{id}
</select>

<!--
  파라미터가 여러 개면 DTO/Entity 객체로 받고, 그 객체의 필드명으로 #{} 지정.
  인터페이스: List<Example> search(ExampleSearchRequest request);
-->
<select id="search" resultType="com.study2.practice.example.entity.Example">
    SELECT id, name
    FROM example
    WHERE name LIKE CONCAT('%', #{keyword}, '%')
    LIMIT #{offset}, #{size}
</select>
```

## INSERT — 등록

```xml
<!--
  useGeneratedKeys="true" + keyProperty="id"
  : DB가 AUTO_INCREMENT로 채번한 PK를, INSERT에 넘긴 파라미터 객체의
    id 필드에 다시 채워 넣어줌. 이 덕분에 INSERT 실행 후 별도 조회 없이
    바로 생성된 id를 서비스 단에서 꺼내 쓸 수 있음.

  인터페이스: void insert(Example example);
-->
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO example (name)
    VALUES (#{name})
</insert>
```

## UPDATE — 수정

```xml
<!-- 인터페이스: void update(Example example); -->
<update id="update">
    UPDATE example
    SET name = #{name}
    WHERE id = #{id}
</update>
```

## DELETE — 삭제

```xml
<!-- 인터페이스: void delete(Integer id); -->
<delete id="delete">
    DELETE FROM example
    WHERE id = #{id}
</delete>
```

## 동적 SQL — 검색조건에 따라 SQL을 바꿔야 할 때

```xml
<!--
  <if test="조건">  : 조건이 참일 때만 안쪽 SQL을 포함시킴.
                       test 안에서는 파라미터 객체의 필드명을 그대로 씀.
  <where>            : 감싸고 있는 <if> 중 하나라도 살아남으면 WHERE를 붙여주고,
                       맨 앞에 남는 불필요한 AND/OR도 자동으로 제거해줌.

  파라미터가 객체 하나뿐이면(@Param 없이) #{}/test에서 "객체이름." 없이
  필드명만 바로 씀. 예: findAll(ExampleSearchRequest condition) ->  #{keyword}
-->
<select id="findAll" resultType="com.study2.practice.example.entity.Example">
    SELECT id, name
    FROM example
    <where>
        <if test="keyword != null and keyword != ''">
            AND name LIKE CONCAT('%', #{keyword}, '%')
        </if>
        <if test="categoryId != null">
            AND category_id = #{categoryId}
        </if>
    </where>
</select>
```

### `<sql>` + `<include>` — 같은 조건을 여러 쿼리에서 재사용

목록 조회(`findAll`)와 총 건수 조회(`countAll`)는 검색 조건(WHERE절)이 완전히 똑같아야 함.
복붙하면 나중에 한쪽만 고치는 실수가 생기기 쉬워서, 한 번 정의해두고 양쪽에서 가져다 씀.

```xml
<sql id="searchCondition">
    <where>
        <if test="keyword != null and keyword != ''">
            AND name LIKE CONCAT('%', #{keyword}, '%')
        </if>
    </where>
</sql>

<select id="findAll" resultType="com.study2.practice.example.entity.Example">
    SELECT id, name FROM example
    <include refid="searchCondition" />
</select>

<select id="countAll" resultType="int">
    SELECT COUNT(*) FROM example
    <include refid="searchCondition" />
</select>
```

### `#{}` vs `${}` — 페이지네이션(LIMIT/OFFSET)에서 주의할 점

- `#{}`: PreparedStatement의 `?` 자리로 바인딩됨. SQL Injection에 안전. 기본은 항상 이거.
- `${}`: 값을 SQL 문자열에 그대로 텍스트로 끼워넣음. SQL Injection 위험 있음 —
  사용자가 직접 입력하는 문자열엔 절대 쓰면 안 됨.

`LIMIT` 절엔 계산식이 들어가야 할 때가 있는데(`(페이지-1)*사이즈`), `#{}`로 바인딩된
값은 계산식에 못 씀(MySQL이 문법 에러 냄). 이럴 때만 예외적으로, 타입이 `int`로
보장된 값(사용자가 임의 문자열을 못 넣는 값)에 한해 `${}`를 씀.

```xml
<!-- page, size는 int 타입이라 SQL Injection 위험 없음 -->
LIMIT ${(page - 1) * size}, ${size}
```

### XML 특수문자 이스케이프

`<if test="...">` 안에서 `<`, `>`, `<=`, `>=`를 그냥 쓰면 XML 파서가 태그로
착각해서 파싱 에러가 남. XML 엔티티로 바꿔써야 함.

| 쓰고 싶은 것 | 실제로 쓸 것 |
|---|---|
| `<` | `&lt;` |
| `>` | `&gt;` |
| `<=` | `&lt;=` |
| `>=` | `&gt;=` |

```xml
<if test="startDate != null">
    AND created_at &gt;= #{startDate}
</if>
```

## 참고: resultType vs resultMap

- `resultType`: 컬럼명 == 필드명(또는 `map-underscore-to-camel-case: true` 설정으로
  스네이크케이스 -> 카멜케이스 자동 변환)이면 이걸로 충분. 지금까지는 전부 이 경우.
- `resultMap`: 컬럼명과 필드명이 안 맞거나, 조인 결과를 여러 객체로 나눠 담아야 하는
  복잡한 경우에 컬럼-필드 매핑을 직접 지정. 게시글/댓글 조인 같은 복잡한 조회가
  필요해지면 그때 따로 설명.
