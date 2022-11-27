# CoolBank
> 은행업무를 간편하게, CoolBank

## 기술 스택
* Spring Boot 3.0.0
* Language : Java17
* DB : MySql
* ORM : Spring Data Jpa
* Spring Security
* LomBok
* Gradle
* Apache commons lang3

# 1. 설명
* 간편하게 송금, 이체, 대출 등 은행 서비스를 할 수 있게 하는 은행 플랫폼이다.
* 간편결제 기능은 넣지 않고 오로지 원래의 은행서비스에 집중한다.

# 2. 설계
* 사용자는 반드시 실명을 기입하되, mysql 예약어에 걸리지않게 컬럼이름은 nickname으로 한다.
* 회원은 송금 횟수로 등급을 매긴다.
* 등급이 높을 수록 송금 수수료가 줄어든다.
* 하루 송금 제한 금액이 존재하며 등급으로 제한금액이 달라진다.
* 계좌를 마이너스 통장으로 변경하면 0미만으로 값이 떨어지는것이 가능하다.(enum으로 계좌의 status 관리)
* 잔액에 항상 조심하고 이체, 송금, 대출등 서비스를 이용할때 최소한의 보안을 위해 비밀번호를 입력받는다.
* 계좌 테이블은 따로 만들어 관리한다.(users와 분리)
* 대출시 대출 상품 이자만큼 통장에서 돈을 뺀다.
* 거래 내역 조회가 가능하도록 한다.
* 휴면 계좌가 있다.

## ERD Diagram
## API 설계
## Json Body
### member
```
{
    "email" : "yc1234@gmail.com",
    "password" : "1234"
}
{
    "email" : "ms1234@gmail.com",
    "password" : "1234"
}
{
    "email" : "admin@coolbank.com",
    "password" : "1234"
}
{
    "oldPassword" : "1234",
    "newPassword" : "1111"
}
```
### statement
```
{
    "accountNumber" : "416890941366",
    "money" : 40000,
    "password" : "1234"
}
```
## 연관관계

# 3. 스타일 가이드
* 유저를 제외한 모든 객체의 Null체크 + 중복 체크
* 함수와 긴 변수의 경우 줄바꿈 가이드를 지켜 작성한다.
* [줄바꿈 가이드](https://github.com/liveforone/study/blob/main/GoodCode/%EC%A4%84%EB%B0%94%EA%BF%88%EC%9C%BC%EB%A1%9C%20%EA%B0%80%EB%8F%85%EC%84%B1%20%ED%96%A5%EC%83%81.md)
* 매직넘버는 전부 상수화해서 처리한다.
* 분기문은 반드시 gate-way 스타일로 한다.
* [gate-way 스타일](https://github.com/liveforone/study/blob/main/GoodCode/%EB%8D%94%20%EC%A2%8B%EC%9D%80%20%EB%B6%84%EA%B8%B0%EB%AC%B8.md)
* entity -> dto 변환 편의메소드는 컨트롤러에서 사용한다.

# 4. 상세 설명
## 입금, 송금, 출금
* statementController에서 입금, 송금, 출금을 관리한다.
* 입금과 출금의 경우에는 사용자가 '나'이다. 즉 내 계좌로 입금, 출금이다.
* 송금의 경우에는 내가 다른 사람의 계좌로 '송금'하는 api이다.
* 송금시에는 statement, 즉 거래내역이 두개가 찍힌다.

# 5. 나의 고민

마이너스 통장 고민해보기
휴면 계좌, 하루 금액 제한
마이너스통장으로 권한 업데이트 하는 것을 고민해보기
이체수수료(등급별) 넣기

유저들어가면 내 계좌 나오고

enum값 가져와서 if 마이너스통장 인경우 마이너스로 떨어지는 계산 가능하게 하기

컬럼으로 막지말고 컨트롤러에서 트랜잭션을 차단하기
즉 통장 테이블을 따로만들고 개인은 통장을 최대 2개 개설가능(일반 한개, 마이너스 한개)

통장번호(id말고) 존재할것임. 송금 대출 등 모두 통장 번호로조회해서 사용한다.

돈관련 행위를 할때마다 비밀번호 match
대출테이블 : 이자, localDate

대출관해사도 깊이 생각해보기 테이블을 따로 뺄건지 어떻게 할것인지 생각해보기

@PageableDefault(page = 0, size = 10)
@SortDefault.SortDefaults({
@SortDefault(sort = "id", direction = Sort.Direction.DESC)
}) Pageable pageable,

거래 내역 만들기(내역에는 페이징 20개)

송금횟수는 입출금내역을 계좌에 맞게 찾으면서 
and status = 송금 인 쿼리로 짜서 가져온다.