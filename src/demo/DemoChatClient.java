package demo;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 가상 스레드를 이용한 간단한 멀티채팅 클라이언트
 *
 * @author chan
 */
public class DemoChatClient {

    // TODO 1: 서버 연결 정보 설정
    // - SERVER_HOST 상수 선언 (localhost)
    static final String SERVER_HOST = "localhost";
    // - SERVER_PORT 상수 선언 (8090)
    static final int SERVER_PORT = 8090;
    // - running 변수 선언 (volatile boolean, 클라이언트 실행 상태 관리)
    private static volatile boolean running = true;

    public static void main(String[] args) {

        // TODO 2: try-with-resources로 리소스 관리
        // - Socket 생성 (SERVER_HOST, SERVER_PORT 연결)
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {

            // - BufferedReader 생성 (서버로부터 메시지 읽기)
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            // - PrintWriter 생성 (서버로 메시지 보내기, autoFlush true)
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            // - Scanner 생성 (사용자 입력 받기)
            Scanner scanner = new Scanner(System.in);

            System.out.println("✅ 채팅 서버에 연결되었습니다!");

            // 서버 메시지 수신 스레드 (가상 스레드)
            Thread.startVirtualThread(() -> {
                try {
                    String serverMessage;

                    while ((serverMessage = in.readLine()) != null && running) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("서버 연결이 끊어졌습니다.");
                }
                running = false;
            });

            // 닉네임 입력 대기
            Thread.sleep(100);
            String nickname = scanner.nextLine();
            out.println(nickname);

            System.out.println("\n 채팅을 시작하세요! (종료: /quit)");
            System.out.println("-".repeat(50));

            // 메시지 입력 처리
            String message;
            while (running && scanner.hasNextLine()) {
                message = scanner.nextLine();

                if (message.equalsIgnoreCase("/quit")) {
                    out.println("/quit");
                    running = false;
                    break;
                }

                if (!message.trim().isEmpty()) {
                    out.println(message);
                }
            }
        } catch (IOException e) {
            System.out.println("서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n 채팅을 종료합니다.");

        // TODO 3: 서버 연결 성공 메시지 출력
        // System.out.println("✅ 채팅 서버에 연결되었습니다!");

        // TODO 4: 서버 메시지 수신용 가상 스레드 시작
        // Thread.startVirtualThread(() -> {
        //     - while 루프로 서버 메시지 계속 읽기
        //     - 읽은 메시지를 콘솔에 출력
        //     - 연결이 끊어지면 "서버 연결이 끊어졌습니다." 출력
        //     - running을 false로 설정
        // });

        // TODO 5: 닉네임 입력 처리
        // - Thread.sleep(100) // 서버의 "닉네임을 입력하세요" 메시지 대기
        // - scanner.nextLine()으로 닉네임 입력받기
        // - out.println()으로 서버에 닉네임 전송

        // TODO 6: 채팅 시작 안내 메시지
        // - "💬 채팅을 시작하세요! (종료: /quit)" 출력
        // - 구분선 출력 ("─" 50개)

        // TODO 7: 메시지 입력 루프
        // while (running && scanner.hasNextLine()) {
        //     - scanner.nextLine()으로 메시지 입력받기
        //     - "/quit" 입력시:
        //         - 서버에 "/quit" 전송
        //         - running = false
        //         - break
        //     - 빈 메시지가 아니면 서버로 전송
        // }

        // TODO 8: 예외 처리
        // } catch (IOException e) {
        //     - "❌ 서버 연결 실패: " + 에러메시지 출력
        // } catch (InterruptedException e) {
        //     - Thread.currentThread().interrupt()
        // }

        // TODO 9: 종료 메시지
        // System.out.println("\n👋 채팅을 종료합니다.");
    }
}