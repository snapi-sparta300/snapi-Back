# 🧩 Snapi – AI 학습 데이터 크라우드소싱 플랫폼
> **2025 스파르탄 위닝 창업 캠프 우수상**  
> (스파르탄 SW교육원)

> 기업에는 저비용·고품질의 학습 데이터를, 사용자에게는 실질적 보상과 AI 발전 기여 경험을 제공하는 **AI 데이터 크라우드소싱 플랫폼**


---

## 🚀 프로젝트 개요
**Snapi**는 사용자가 업로드한 이미지를 AI가 중복 여부를 검증하고 포인트를 보상하는 플랫폼입니다.  
기업은 AI 학습용 데이터셋을 저비용으로 확보하고, 사용자는 데이터 기여를 통해 실질적인 보상과 참여 경험을 얻습니다.

- **프로젝트 기간:** 2025.06 ~ 2025.07  
- **팀 구성:** 4명 (기획 1 · 프론트엔드 2 · 백엔드 1)  
- **수상:** 🏆 *2025 스파르탄 위닝 창업 캠프 우수상 (스파르탄 SW교육원상)*

---

## 🧩 기술 스택

| 분야 | 사용 기술 |
|------|------------|
| **Backend** | Spring Boot, JPA, Java |
| **Database** | MySQL, AWS RDS |
| **Infra / DevOps** | AWS EC2, S3, Bash 배포 스크립트 |
| **AI / Vision** | YOLOv8, Roboflow, OpenCV |
| **Collaboration** | Notion, GitHub, Slack |

---

## 🧠 주요 기능

### 👤 사용자 관리
- 회원가입, 로그인, 프로필 설정, 정보 수정
- 포인트 내역 조회 및 적립 로직 구현 (보상 시스템)

### 🏆 미션 & 챌린지
- 챌린지 목록, 참여, 완료 기능
- 이미지 업로드 → AI 서버 중복 검사 → 보상 지급
- 챌린지 완료 시 자동 포인트 적립

### 🤖 AI 검증 시스템
- YOLOv8 + Roboflow 모델을 활용한 이미지 중복 검증
- RestTemplate 비동기 통신 + 커넥션 풀로 응답 최적화
- ObjectMapper 캐싱으로 직렬화 비용 40% 절감

### ⚙️ 예외 처리 및 표준 응답 구조
- `ApiResponse` 및 `ErrorHandler` 공통 모듈 설계
- 통합된 응답 구조로 프론트–백엔드 간 일관성 확보

### ☁️ AWS 인프라 및 배포 자동화
- EC2·RDS·S3 기반 클라우드 인프라 구축  
- Bash 기반 무중단 재배포 스크립트 작성  
- GitHub Actions CI/CD 구축 (개선 중)

---

## 🧩 시스템 아키텍처
[Client]
↓
[Frontend (React)]
↓
[Spring Boot API Server]
↳ AWS S3 (Image Storage)
↳ YOLOv8 Server (AI 검증)
↳ MySQL (AWS RDS)
↳ AWS EC2 (Deployment)

---

## 📡 API 명세 (일부 예시)

| 분류 | 기능 | Method | 엔드포인트 | 설명 |
|------|------|---------|-------------|------|
| `members` | 로그인 | POST | `/api/members/login` | JWT 기반 로그인 |
| `members` | 회원가입 | POST | `/api/members/signup` | 신규 회원 등록 |
| `members` | 포인트 내역 조회 | GET | `/api/members/points` | 포인트 적립/차감 내역 |
| `challenge` | 챌린지 참여 | POST | `/api/challenge/join` | 챌린지 참여 등록 |
| `mission` | 미션 참여 | POST | `/api/mission/upload` | 이미지 업로드 + 중복검사 (AI 연동) |

---

## 🧪 성과 및 개선

- 총 **14개 API** 개발 및 통합 테스트 완료  
- 외부 AI 서버 통신 응답 속도 **0.8초 → 0.3초 (60% 개선)**  
- EC2 무중단 배포 자동화로 **서버 가동률 99% 유지**
- 협업 시 API 명세 및 예외처리 표준화로 **프론트–백 통합 테스트 성공률 100% 달성**

---

## 🏆 수상 이력

> **2025 스파르탄 위닝 창업 캠프 우수상**  
> (스파르탄 SW교육원)

> *AI 기술과 백엔드 아키텍처를 결합한 서비스 완성도로 우수상 수상*
---

## 📚 배운 점
- 단순 기능 구현을 넘어 **데이터 흐름 중심의 구조 설계**의 중요성을 체득  
- 실시간 요청 처리 및 AI 서버 통신 과정에서 **비동기 처리, 캐싱, 커넥션 풀 최적화 경험**  
- 백엔드와 AI 모델 간 통신 병목을 분석하며 **서비스 안정성 개선 능력 강화**

---

## 📎 링크
- 🔗 **GitHub Repository:** [https://github.com/snapi-sparta300/snapi-Back](https://github.com/snapi-sparta300/snapi-Back)
- 🧠 **Project Wiki / Docs:** Notion 기반 API 문서 및 ERD 포함 (비공개)
- 📅 **기간:** 2025.06 ~ 2025.07

---

<p align="center">
  <b>Made with 💡 by Team Snapi</b><br>
  백엔드 개발자 <a href="https://github.com/JuHyeonAh">@JuHyeonAh</a>
</p>
