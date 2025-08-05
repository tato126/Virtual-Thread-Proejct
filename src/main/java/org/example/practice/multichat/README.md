# 가상 스레드 멀티채팅 프로그램

## 개요
Java의 가상 스레드(Virtual Thread)를 활용한 아주 간단한 멀티채팅 프로그램입니다.

## 특징
- ✅ **가상 스레드 사용**: 각 클라이언트를 가상 스레드로 처리
- ✅ **실시간 브로드캐스트**: 모든 사용자에게 메시지 전달
- ✅ **닉네임 지원**: 사용자별 고유 닉네임
- ✅ **입장/퇴장 알림**: 사용자 상태 변경 알림
- ✅ **간단한 명령어**: /quit로 종료

## 구성
- **SimpleChatServer.java**: 채팅 서버
- **SimpleChatClient.java**: 채팅 클라이언트

## 실행 방법

### 1. 서버 실행
```bash
cd /Users/chan/WorkSpace/Multi-Chat
javac src/main/java/org/example/practice/multichat/SimpleChatServer.java
java -cp src/main/java org.example.practice.multichat.SimpleChatServer
```

### 2. 클라이언트 실행 (여러 개 실행 가능)
```bash
# 터미널 1
java -cp src/main/java org.example.practice.multichat.SimpleChatClient

# 터미널 2
java -cp src/main/java org.example.practice.multichat.SimpleChatClient

# 터미널 3
java -cp src/main/java org.example.practice.multichat.SimpleChatClient
```

## 사용 예시

### 서버 콘솔
```
🚀 간단한 멀티채팅 서버가 포트 8090에서 시작되었습니다.
✅ Alice님이 입장하셨습니다. (현재 1명)
✅ Bob님이 입장하셨습니다. (현재 2명)
💬 [Alice] 안녕하세요!
💬 [Bob] 반갑습니다!
👋 Alice님이 퇴장하셨습니다. (현재 1명)
```

### 클라이언트 1 (Alice)
```
✅ 채팅 서버에 연결되었습니다!
닉네임을 입력하세요:
Alice

💬 채팅을 시작하세요! (종료: /quit)
──────────────────────────────────────────────────
📢 Alice님이 입장하셨습니다. (현재 1명)
📢 Bob님이 입장하셨습니다. (현재 2명)
안녕하세요!
[Bob] 반갑습니다!
/quit

👋 채팅을 종료합니다.
```

### 클라이언트 2 (Bob)
```
✅ 채팅 서버에 연결되었습니다!
닉네임을 입력하세요:
Bob

💬 채팅을 시작하세요! (종료: /quit)
──────────────────────────────────────────────────
📢 Bob님이 입장하셨습니다. (현재 2명)
[Alice] 안녕하세요!
반갑습니다!
📢 Alice님이 퇴장하셨습니다. (현재 1명)
```

## 주요 코드 설명

### 가상 스레드 사용
```java
// 서버: 각 클라이언트를 가상 스레드로 처리
Thread.startVirtualThread(() -> {
    new ClientHandler(clientSocket).run();
});

// 클라이언트: 서버 메시지 수신을 가상 스레드로 처리
Thread.startVirtualThread(() -> {
    // 서버 메시지 수신 로직
});
```

### 브로드캐스트 구현
```java
static void broadcast(String message, String sender) {
    for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
        if (!entry.getKey().equals(sender)) {
            entry.getValue().sendMessage(message);
        }
    }
}
```

## 확장 가능한 기능
1. **명령어 추가**: /list (사용자 목록), /whisper (귓속말)
2. **채팅방**: 여러 채팅방 생성 및 이동
3. **파일 전송**: 이미지, 문서 전송
4. **이모티콘**: 이모티콘 지원
5. **메시지 저장**: 채팅 로그 저장

## 장점
- **간단한 구조**: 이해하기 쉬운 코드
- **확장 가능**: 기능 추가가 용이
- **고성능**: 가상 스레드로 많은 사용자 처리 가능
- **실시간성**: 즉각적인 메시지 전달

이 프로그램은 가상 스레드의 장점을 활용한 간단하면서도 실용적인 멀티채팅 시스템입니다!