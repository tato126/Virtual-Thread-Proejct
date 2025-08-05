package org.example.practice.multichat;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * 가상 스레드를 이용한 간단한 멀티채팅 클라이언트
 */
public class SimpleChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8090;
    private static volatile boolean running = true;
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("✅ 채팅 서버에 연결되었습니다!");
            
            // 서버 메시지 수신 스레드 (가상 스레드)
            Thread.startVirtualThread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null && running) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("서버 연결이 끊어졌습니다.");
                }
                running = false;
            });
            
            // 닉네임 입력 대기
            Thread.sleep(100);
            String nickname = scanner.nextLine();
            out.println(nickname);
            
            System.out.println("\n💬 채팅을 시작하세요! (종료: /quit)");
            System.out.println("─".repeat(50));
            
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
            System.err.println("❌ 서버 연결 실패: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n👋 채팅을 종료합니다.");
    }
}