# 📚 NetLibrary UI Client

본 레포지토리는 **Java Swing 기반 데스크톱 UI 클라이언트**를 구현한 프로젝트입니다.  
네트워크 통신을 통해 서버와 실시간으로 연동되며, 도서관 좌석 이용 현황, 채팅, 사용자 상태 등을 시각적으로 제공합니다.

---

## 🖥️ 프로젝트 개요

- **프로젝트 성격**: 데스크톱 애플리케이션 (Client)
- **UI 기술 스택**: Java Swing
- **역할**: 사용자 / 관리자 UI 제공
- **연동 대상**: Socket 서버 (Chat / Seat / Sensor 데이터)

본 UI 클라이언트는 웹이 아닌 **Java Swing을 활용한 네이티브 데스크톱 UI**로 구현되어,  
실시간 데이터 반영과 빠른 화면 전환에 초점을 맞추었습니다.

---

## 🎯 UI 설계 목표

- Java Swing 기반의 **경량 데스크톱 UI**
- 소켓 통신 기반 **실시간 상태 반영**
- 사용자 / 관리자 역할 분리
- 직관적인 좌석 상태 시각화
- 네트워크 실습 및 구조 이해를 위한 클라이언트 구현

---

## 🛠️ 기술 스택

| 구분 | 기술 |
|----|----|
| Language | Java |
| UI Framework | **Java Swing** |
| Network | TCP Socket |
| Data Format | JSON |
| Build Tool | Gradle |
| IDE | IntelliJ IDEA |

---

## 🧩 주요 화면 구성

- **LoginScreen**
  - 사용자 로그인
  - 서버 소켓 연결 초기화

- **FloorSelectionScreen**
  - 층 / 열람실 선택

- **MainScreen**
  - 좌석 현황 대시보드
  - 센서 데이터 요약 표시

- **SeatMapScreen**
  - 좌석 상태 시각화 (사용 중 / 비어 있음 / 외출 중)
  - 체크인 / 좌석 변경

- **ChatScreen**
  - 실시간 채팅
  - 관리자 / 사용자 메시지 구분

- **Admin UI**
  - 관리자 전용 화면
  - 공지 및 모니터링 기능

---

## 📡 서버 연동 방식

- **TCP Socket 기반 통신**
- JSON 메시지 구조 사용
- UI 이벤트 → SocketMessage 생성 → 서버 전송
- 서버 응답 → UI 상태 즉시 갱신

```json
{
  "type": "CHAT",
  "role": "USER",
  "floor": 2,
  "room": "A",
  "sender": "userId",
  "msg": "안녕하세요"
}
```
---


## 📁 프로젝트 구조
```
client-swing/
 ├─ .idea/                     # IntelliJ IDEA 설정 파일
 │   ├─ misc.xml
 │   ├─ modules.xml
 │   └─ vcs.xml
 │
 ├─ src/
 │   └─ client/
 │       ├─ socket/            # TCP 소켓 통신 관련 클래스
 │       │   ├─ SocketClient.java
 │       │   └─ SocketMessage.java
 │       │
 │       └─ ui/                # Java Swing UI 구성
 │           ├─ screen/         # 화면 단위 UI (Frame / Screen)
 │           ├─ Main.java       # UI 실행 진입점
 │           ├─ RoundedBorder.java
 │           └─ SeatPanel.java  # 좌석 UI 컴포넌트
 │
 ├─ resources/                 # 폰트, 이미지 등 리소스
 │
 ├─ .gitignore
 └─ client-swing.iml

```

---

## ✨ 주요 특징

- **Java Swing 기반 데스크톱 UI**
- TCP Socket 기반 실시간 통신
- 서버 상태 변경 시 UI 즉시 반영
- 사용자 / 관리자 역할 분리
- 네트워크 구조 이해를 위한 실습용 아키텍처

---

## 🚀 실행 방법

1. 서버 애플리케이션 실행
2. UI 클라이언트 실행
3. 로그인 → 층 / 열람실 선택 → 메인 화면 진입

> ⚠️ 본 UI 클라이언트는 서버가 실행 중이어야 정상 동작합니다.

---

## 📌 참고 사항

- 본 레포지토리는 **UI(Client) 전용** 레포지토리입니다.
- 서버, DB, 센서 시뮬레이터는 별도 레포지토리에서 관리됩니다.
- 네트워크 수업 및 시스템 설계 실습을 목적으로 합니다.

---

## 👩‍💻 개발자

- Java Swing UI 설계 및 구현
- TCP Socket 통신 연동
- 실시간 좌석 / 채팅 UI 처리
- 네트워크 기반 클라이언트 아키텍처 설계

