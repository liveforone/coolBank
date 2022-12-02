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
* 간편하게 송금, 이체, 입금, 출금 등 은행 서비스를 할 수 있게 하는 은행 플랫폼이다.
* 간편결제 기능은 넣지 않고 오로지 원래의 은행서비스에 집중한다.

# 2. 설계
* 사용자는 반드시 실명을 기입하되, mysql 예약어에 걸리지않게 컬럼이름은 nickname으로 한다.
* 회원은 송금 횟수로 등급을 매긴다.
* 하루 송금 제한 금액은 모든 등급에 한해 일천만원으로 제한한다.
* 회원은 통장을 최대 2개 개설가능하며 마이너스 통장은 그중 1개만 개설 가능하다.
* 계좌를 마이너스 통장으로 변경하면 0미만으로 값이 떨어지는것이 가능하다.(enum으로 계좌의 status 관리)
* 잔액에 항상 조심하고 이체, 송금, 대출등 서비스를 이용할때 최소한의 보안을 위해 비밀번호를 입력받는다.
* 대출시 대출 상품 이자만큼 통장에서 돈을 출금한다.
* 거래 내역 조회가 가능하도록 한다.

## ERD Diagram
![스크린샷(148)](https://user-images.githubusercontent.com/88976237/204199056-5f55c258-411d-4662-8ee7-5969739d2dd0.png)

## API 설계
### member
```
/ - get
/member/signup - get/post
/member/login - get/post
/member/logout - post
/member/prohibition - get
/member/my-page - get
/member/nickname-post - post
/admin - get
/member/change-email - post
/member/change-password - post
/member/withdraw - post
```
### account
```
/account/{id} - get
/normal-account/post - post
/credit-account/post - post
```
### statement
```
/statement/{accountNumber} - get
/deposit - post
/remit - post
/withdraw - post
```

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
```
statement -> account, manyToOne 단방향
account -> member, manyToOne 단방향
```

# 3. 스타일 가이드
* 유저를 제외한 모든 객체의 Null체크 + 중복 체크(유저는 시큐리티 단에서 이미 체크한다.)
* 함수와 긴 변수의 경우 줄바꿈 가이드를 지켜 작성한다.
* [줄바꿈 가이드](https://github.com/liveforone/study/blob/main/GoodCode/%EC%A4%84%EB%B0%94%EA%BF%88%EC%9C%BC%EB%A1%9C%20%EA%B0%80%EB%8F%85%EC%84%B1%20%ED%96%A5%EC%83%81.md)
* 매직넘버는 전부 상수화해서 처리한다.
* 분기문은 반드시 gate-way 스타일로 한다.
* [gate-way 스타일](https://github.com/liveforone/study/blob/main/GoodCode/%EB%8D%94%20%EC%A2%8B%EC%9D%80%20%EB%B6%84%EA%B8%B0%EB%AC%B8.md)
* entity -> dto 변환 편의메소드는 리스트나 페이징이 아닌 경우 컨트롤러에서 사용한다.

# 4. 상세 설명
## 입금, 송금, 출금
* statementController에서 입금, 송금, 출금을 관리한다.
* 입금과 출금의 경우에는 사용자가 '나'이다. 즉 내 계좌로 입금, 출금이다.
* 송금의 경우에는 내가 다른 사람의 계좌로 '송금'하는 api이다.
* 송금시에는 statement, 즉 거래내역이 두개가 찍힌다.
* 송금은 마이너스 통장이여도 잔액 0원 미만으로 내려갈시 송금 불가능하다.
* 하지만 출금은 가능하며, 출금시 계좌가 마이너스통장인지 확인후 마이너스이면 0원 미만으로도 잔액이 내려가는 것이 가능해진다.(일반통장은 불가능)
## 회원 등급 표
* 회원 등급은 accountId and state(send) 으로 뽑아온다.(and 쿼리 사용해서)
* 즉 해당 계좌 거래 내역중 입금 내역만 뽑는다.
* 해당 리스트의 길이가 아래의 표에 해당하면 등급이 업데이트 된다.
* 마이페이지에 접근 시 송금횟수 체크하고 바로 업데이트 처리한다.
* 송금횟수 50건 미만 BRONZE
* 50건 초과 SILVER
* 100건 초과 GOLD
* 500건 초과 PLATINUM
* 1000건 초과 DIA

# 5. 나의 고민
## RandomStringUtils로 무작위 문자형 숫자 만들때 중복
* 아무리 라이브러리로 12짜리의 무작위 문자형 숫자를 만든다 한들 중복이 안생길 수있나?
* 하는 의문이 지속하여 들었다.
* 의심이 가고 두려울때는 코드 몇자 추가되더라도 중복을 체크하는 것이 좋다.
* 따라서 while문으로 함수를 선언하고 해당 숫자로 계좌를 찾아 null(중복아님)을 확인한후
* 중복이 아니라면 리턴하는 방식을 선택하였다.
## 입출금 고민
* 입금과 출금(송금 아님)을 언제 할까 생각을 해보니 오프라인 상에서 출금은 오프라인 + 대출(신용카드)의 경우
* 발생한다는 것을 깨달았다. 따라서 송금처럼 내계좌, 상대 계좌 확인하는 로직을 넣지 않았다.
## 마이너스 통장 출금
* 출금시 로직 마이너스 통장을 어떻게 해결해야하나 하는 생각이 들었다.
* 왜냐하면 출금은 위에서 설명했듯 내가 하거나 대출(신용카드)로 빠져나가는 상황인데
* 일반적인 통장에서는 대출(신용카드)가 불가능하도록 설정했기 때문에 잔액이 0원 밑으로 떨어지면 안된다.
* 그렇지만 마이너스 통장은 당연히 잔액이 0원 밑으로 떨어져도 된다.
* gate way 스타일의 분기 처리는 else 문이 없기 때문에 절차적으로 if문을 통해서 걸러진다.
* 분기문 순서를 고민하다가 내 계좌 체크 -> 비밀 번호 체크 -> 마이너스 통장 체크
* -> 마이너스이면 바로 withdraw() 함수 호출
* -> 아닐경우 잔액과 출금 금액 비교 순으로 분기문을 작성해 절차적으로 분기처리를 했다.
* gate way 스타일의 분기문을 처리할때에는 절차적으로 분기순서를 따져야 논리오류나 다른 오류가 발생하지않고
* 정상적으로 작동한다. 따라서 gate way 스타일의 분기는 절차를 잘 따져보아야 한다.

# 6. 새롭게 추가한 점
* 널체크는 util 클래스를 만들고 커스텀 함수인 isNull()을 이용해 처리하는 것으로 전면 수정함.