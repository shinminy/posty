# Posty

포스팅 플랫폼 제공을 위한 백엔드 프로젝트입니다.

## 개발

### 기술 스택

* Language: Java 17
* Framework: Spring Boot 3.4.3
* Database: MariaDB 11
* Cache: Redis 7
* Message Broker: ActiveMQ Classic 6.1.7
* File Security & Validation: Apache Tika 3.2.0, ClamAV 1.4
* Container: Docker 29 (MariaDB, Redis, ActiveMQ, ClamAV)
* Build Tool: Gradle 8.13 (Groovy DSL)
* Deployment: ~~AWS EC2~~ Oracle VirtualBox (Ubuntu 24.04), systemd, GitHub Actions, Docker Compose v2
* Documentation: Springdoc OpenAPI (Swagger UI)

### 사용 도구 및 기타

* Version Control: Git, GitHub
* IDE: IntelliJ IDEA
* CI/CD: GitHub Actions
* 지원 도구: ChatGPT, Claude, Gemini (구조 설계 및 코드 리뷰 보조용)

## 서비스

서비스 목록은 아래와 같습니다.

* [posting-api](#posting-api)
* [file-api](#file-api)

### posting-api

포스팅 서비스를 위한 내부 REST API입니다. (현재 문서에 링크된 주소는 개발용 로컬 주소입니다.)

#### posting-api 관련 페이지

* API 문서 : [Swagger UI](http://localhost:15793/docs/swagger-ui/index.html)
* ActiveMQ 관리 페이지 : [ActiveMQ Console](http://localhost:8161/admin/index.jsp)

#### posting-api의 VM 옵션 목록

```
-DFILE_API_URL=
-DFILE_API_TOKEN=
-DDB_URL=
-DDB_USERNAME=
-DDB_PASSWORD=
-DREDIS_HOST=
-DACTIVEMQ_BROKER_URL=
-DACTIVEMQ_USER=
-DACTIVEMQ_PASSWORD=
-DMAIL_HOST=
-DMAIL_USERNAME=
-DMAIL_PASSWORD=
-Dlogging.config=
-Duser.language=
-Duser.timezone=
```

* FILE_API_URL
    * file-api에 파일 업로드 및 삭제를 요청하기 위한 주소
    * 예 : `http://localhost:12684`
* FILE_API_TOKEN
    * file-api에 파일 업로드 및 삭제를 요청할 때 필요한 토큰 값
    * [file-api의 VM 옵션 목록](#file-api의-VM-옵션-목록)의 **API_TOKEN** 값과 같아야 함
* DB_URL
    * 데이터베이스 주소
    * spring.datasource.url에 들어갈 값
    * 예 : `jdbc:mariadb://localhost:3306/dbname`
* DB_USERNAME
    * 데이터베이스 접속 사용자 계정
    * spring.datasource.username에 들어갈 값
* DB_PASSWORD
    * 데이터베이스 접속 사용자 계정의 암호
    * spring.datasource.password에 들어갈 값
* REDIS_HOST
    * Redis 주소
    * spring.data.redis.host에 들어갈 값
    * 예 : `localhost`
* ACTIVEMQ_BROKER_URL
    * ActiveMQ 주소
    * spring.activemq.broker-url에 들어갈 값
    * 예 : `tcp://localhost:61616`
* ACTIVEMQ_USER
    * ActiveMQ 접속 계정
    * spring.activemq.user에 들어갈 값
* ACTIVEMQ_PASSWORD
    * ActiveMQ 접속 계정의 암호
    * spring.activemq.password에 들어갈 값
* MAIL_HOST
    * SMTP 서버 주소 (메일 인증 시 인증코드를 메일로 보내기 위해 사용)
    * spring.mail.host에 들어갈 값
    * 예 : `smtp.naver.com`
* MAIL_USERNAME
    * SMTP 서비스 계정 (메일 인증 시 인증코드를 메일로 보내기 위해 사용)
    * spring.mail.username에 들어갈 값
    * 예 : `user@naver.com`
* MAIL_PASSWORD
    * SMTP 서비스 계정의 암호로, 보통 앱 암호 (메일 인증 시 인증코드를 메일로 보내기 위해 사용)
    * spring.mail.password에 들어갈 값
* logging.config
    * 로그 설정 파일
    * 예 : `classpath:log4j2.xml`
* user.language
    * 기본 Locale 언어 (Validation 기본 안내 메시지에 대한 언어를 설정하기 위해 사용)
    * spring.web.locale에 들어갈 값
    * 예 : `en`
* user.timezone
    * 기본 시간대
    * spring.jackson.time-zone 등에 들어갈 값
    * 예 : `Asia/Seoul`

### file-api

파일 업로드 및 다운로드를 위한 REST API입니다.

#### file-api의 VM 옵션 목록

```
-DAPI_TOKEN=
-DTEMP_PATH=
-DBASE_PATH=
-DCLAMAV_HOST=
-Dlogging.config=
-Duser.language=
-Duser.timezone=
```

* API_TOKEN
    * 파일 업로드 및 삭제를 요청받았을 때 허용된 요청인지 확인하기 위한 토큰 값
    * [posting-api의 VM 옵션 목록](#posting-api의-VM-옵션-목록)의 **FILE_API_TOKEN** 값과 같아야 함
* TEMP_PATH
    * 파일 저장 전에 크기나 바이러스 검사 등을 위해 임시로 저장하는 위치
    * 예 : `/file/temp`
* BASE_PATH
    * 파일 저장 위치
    * 예 : `/file`
* CLAMAV_HOST
    * ClamAV 주소
    * 예 : `localhost`
* logging.config
    * 로그 설정 파일
    * 예 : `classpath:log4j2.xml`
* user.language
    * 기본 Locale 언어
    * 예 : `en`
* user.timezone
    * 기본 시간대
    * 예 : `Asia/Seoul`
