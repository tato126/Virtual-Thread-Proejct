# DemoChatServer 전체 코드

## 패키지 및 임포트
```java
package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
```

## 메인 클래스
```java
/**
 * 가상 스레드를 이용한 간단한 멀티채팅 서버 클래스
 *
 * @author chan
 */
public class DemoChatServer {

    // port 설정
    // TODO: 클래스로 분리하여 관리 가능
    private static final int PORT = 8090;

    // Map을 이용한 client 리스트 만들기
    // TODO: 해당 Map이 의미하는 것 무엇일까? -> String(접속자)로 접속자의 소켓을 찾아간다.
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    // main
    public static void main(String[] args) throws IOException {

        // print("간단한 멀티채팅 서버가 포트")
        System.out.printf("*** 간단한 멀티채팅 서버가 포트 " + PORT + "에서 시작되었습니다.");

        // try -with -resource
        try (
                ServerSocket serverSocket = new ServerSocket(PORT)) {

            // while문을 통해 연결이 끊길 때까지 클라이언트 처리
            while (true) {

                // 대기하면서 socket 응답
                Socket socket = serverSocket.accept();

                // 가상 스레드 클라이언트 처리
                Thread.startVirtualThread(() -> {
                    // TODO: run을 가상쓰레드로 처리할 수는 없을까?
                    new ClientHandler(socket).run();
                });
            }
        }
    }
```

## ClientHandler 내부 클래스
```java
    static class ClientHandler {

        // 클라이언트 처리 클래스
        // socket
        private final Socket socket;
        // writer out
        private PrintWriter out;
        // nickname
        private String nickname;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }


        // TODO: 가상 쓰레드로 처리가 가능하지 않을까?
        // 지금 로직은 너무 복잡하다. 이를 분리하거나 JVM이 관리하게 할 수는 없을까?
        // 클라이언트 요청처리
        void run() {

            // try-with-resource
            try (socket;

                 // BufferedReader
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                 // PrintWriter
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {


                // out에 writer 설정
                this.out = writer;

                // 닉네임 요청
                out.println("닉네임을 입력하세요");
                nickname = in.readLine();

                // 중복 체크
                if (clients.containsKey(nickname)) {
                    out.println("이미 사용중인 닉네임입니다. 연결을 종료합니다.");
                    return;
                }

                // 클라이언트 등록
                clients.put(nickname, this);

                // 입장 알림
                String welcomeMsg = nickname + "님이 입장하셨습니다. (현재 " + clients.size() + "명)";
                System.out.println("=========> " + welcomeMsg);
                broadcast("!!! " + welcomeMsg, null);

                // 메시지 처리
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("quit")) {
                        break;
                    }

                    String chatMsg = "[" + nickname + "]" + message;
                    System.out.println(".... " + chatMsg);
                    broadcast(chatMsg, nickname);
                }


            } catch (IOException e) {
                System.out.println("클라이언트 오류 : " + e.getMessage());
            } finally {

                // 클라이언트 제거
                if (nickname != null) {
                    clients.remove(nickname);
                    String exitMsg = nickname + "님이 퇴장하셨습니다. (현재" + clients.size() + "명)";
                    System.out.println("Bye!!! " + exitMsg);
                    broadcast("===> " + exitMsg, null);
                }
            }
        }

        void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        // TODO: broadcast를 따로 분리할 수 있을 것 같다.
        static void broadcast(String message, String sender) {

            // Entry는 무엇인가요?
            // entrySet은 무엇인가요?
            for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
                if (!entry.getKey().equals(sender)) { // 자기자신이 아니라면 모두에게 메시지 전송
                    entry.getValue().sendMessage(message);
                }
            }
        }
    }
}
```

## 주요 기능 설명

### 1. 서버 초기화
- **PORT**: 8090번 포트 사용
- **clients Map**: `ConcurrentHashMap<String, ClientHandler>`로 접속자 관리
  - Key: 사용자 닉네임
  - Value: 해당 사용자의 ClientHandler 객체

### 2. 클라이언트 연결 처리
- `ServerSocket.accept()`로 클라이언트 연결 대기
- 연결 시 가상 스레드(`Thread.startVirtualThread()`)로 각 클라이언트 독립 처리

### 3. ClientHandler 클래스
- **필드**:
  - `socket`: 클라이언트와의 연결
  - `out`: 클라이언트로 메시지 전송용 PrintWriter
  - `nickname`: 사용자 닉네임

- **주요 메서드**:
  - `run()`: 클라이언트 처리 로직 (닉네임 입력, 메시지 수신/전송)
  - `sendMessage()`: 개별 클라이언트에게 메시지 전송
  - `broadcast()`: 모든 클라이언트에게 메시지 전파 (발신자 제외)

### 4. 메시지 처리 흐름
1. 닉네임 입력 요청 및 중복 체크
2. 입장 알림 브로드캐스트
3. 메시지 수신 및 브로드캐스트 반복
4. 종료 시 퇴장 알림 및 클라이언트 제거

### 5. 예외 처리
- IOException 처리
- finally 블록에서 클라이언트 정리 작업

## TODO 항목 정리
1. PORT를 별도 설정 클래스로 분리
2. Map의 역할 명확화 (접속자 관리용)
3. run 메서드의 가상 스레드 최적화
4. broadcast 메서드를 별도 클래스로 분리

## 기술적 특징
- **Java 21 가상 스레드** 활용으로 효율적인 동시 접속 처리
- **ConcurrentHashMap** 사용으로 스레드 안전성 보장
- **try-with-resources**로 자동 리소스 관리
- **UTF-8 인코딩** 명시로 한글 채팅 지원