package org.example.practice.multichat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 가상 스레드를 이용한 간단한 멀티채팅 서버
 */
public class SimpleChatServer {
    private static final int PORT = 8090;
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    
    public static void main(String[] args) throws IOException {
        System.out.println("🚀 간단한 멀티채팅 서버가 포트 " + PORT + "에서 시작되었습니다.");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                // 가상 스레드로 클라이언트 처리
                Thread.startVirtualThread(() -> {
                    new ClientHandler(clientSocket).run();
                });
            }
        }
    }
    
    // 클라이언트 처리 클래스
    static class ClientHandler {
        private final Socket socket;
        private PrintWriter out;
        private String nickname;
        
        ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        void run() {
            try (socket;
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                
                this.out = writer;
                
                // 닉네임 요청
                out.println("닉네임을 입력하세요:");
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
                System.out.println("✅ " + welcomeMsg);
                broadcast("📢 " + welcomeMsg, null);
                
                // 메시지 처리
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    }
                    
                    String chatMsg = "[" + nickname + "] " + message;
                    System.out.println("💬 " + chatMsg);
                    broadcast(chatMsg, nickname);
                }
                
            } catch (IOException e) {
                System.err.println("❌ 클라이언트 오류: " + e.getMessage());
            } finally {
                // 클라이언트 제거
                if (nickname != null) {
                    clients.remove(nickname);
                    String exitMsg = nickname + "님이 퇴장하셨습니다. (현재 " + clients.size() + "명)";
                    System.out.println("👋 " + exitMsg);
                    broadcast("📢 " + exitMsg, null);
                }
            }
        }
        
        void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
    
    // 모든 클라이언트에게 메시지 전송
    static void broadcast(String message, String sender) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (!entry.getKey().equals(sender)) {
                entry.getValue().sendMessage(message);
            }
        }
    }
}