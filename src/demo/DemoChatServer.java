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

/**
 * 가상 스레드를 이용한 간단한 멀티채팅 서버 클래스
 *
 * @author chan
 */
public class DemoChatServer {

    // port 설정
    private static final int PORT = 8090;

    // Map을 이용한 client 리스트 만들기
    // TODO: 해당 Map이 의미하는 것 무엇일까?
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
                    new ClientHandler(socket).run();
                });
            }
        }
    }

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

