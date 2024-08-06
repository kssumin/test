# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* 

### 요구사항 2 - get 방식으로 회원가입
* 

### 요구사항 3 - post 방식으로 회원가입
* 

### 요구사항 4 - redirect 방식으로 이동
#### Request
```markdown
Client request:

GET /index.html HTTP/1.1
Host: www.example.com
```

#### Response
```markdown
HTTP/1.1 302 Found
Location: http://www.iana.org/domains/example/
```

클라이언트는 response line의 상태 코드를 확인한 후 302라면 Location값을 읽어 서버에 재요청을 보낸다.


#### 회원가입 처리 이후에 index.html을 보내주는 방식은?
브라우저에서 새로고침을 하면 회원가입 요청 처리부터 다시 진행한다.

데이터 중복의 문제가 있다.

따라서 두 개의 api를 분리하여 회원가입 완료 이후 index.html을 요청하도록 클라이언트에게 유도한다(리다이렉트)

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 브라우저는 Content-Type 응답 헤더 값을 통해 응답 본문에 포함되어 있는 컨텐츠가 어떤 유형인지 확인한다.

즉, CSS 요청에 대해서는 Content-Type : text/css여야 브라우저가 이를 css로 인식할 수 있다.

### heroku 서버에 배포 후


### 리팩터링

#### enum
상수값이 서로 연관되어 있는 경우 자바의 enum을 쓰기에 적합하다.