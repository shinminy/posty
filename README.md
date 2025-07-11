# Posty

포스팅 플랫폼 제공을 위한 프로젝트입니다. 현재 백엔드만 작업 중입니다.

## 개발

### 기술 스택

* Language: Java 17
* Framework: Spring Boot 3.4.3
* Database: MariaDB 11, Redis 7
* Message Broker: ActiveMQ Classic 6.1.7
* Antivirus: ClamAV 1.4
* Build Tool: Gradle 8.13 (Groovy DSL)
* Deployment: AWS EC2 (Ubuntu 24.04), systemd, GitHub Actions
* Documentation: Springdoc OpenAPI (Swagger UI)

### 사용 도구 및 기타

* IDE: IntelliJ IDEA
* Version Control: Git, GitHub
* CI/CD: GitHub Actions
* 지원 도구: ChatGPT (구조 설계 및 코드 리뷰 보조용)

## 서비스

서비스 목록은 아래와 같습니다.

* [posting-api](#posting-api)
* [file-api](#file-api)

### posting-api

포스팅 서비스를 위한 내부 REST API입니다. (현재 링크된 주소는 개발용 로컬 주소입니다. 서버 주소는 따로 알려드립니다.)

* API 문서 : [Swagger UI](https://localhost:15793/docs/swagger-ui/index.html)
* ActiveMQ 관리 페이지 : [ActiveMQ Console](https://localhost:8162/admin/index.jsp)

### file-api

파일 업로드 및 다운로드를 위한 REST API입니다.
