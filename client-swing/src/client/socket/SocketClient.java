package client.socket;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {

    private static final Gson gson = new Gson();

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * 서버 연결
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("[CLIENT] 서버 연결 완료: " + host + ":" + port);
    }

    /**
     * 서버로 메시지 전송
     */
    public void send(SocketMessage msg) {
        String json = gson.toJson(msg);
        out.println(json);
        out.flush();
        System.out.println("[CLIENT -> SERVER] " + json);
    }

    /**
     * 서버로부터 메시지 1줄 수신
     * (blocking: 별도의 Thread에서 돌려야 함)
     */
    public SocketMessage read() throws IOException {
        String line = in.readLine();
        System.out.println("[SERVER -> CLIENT RAW] " + line);
        return gson.fromJson(line, SocketMessage.class);
    }

    //소켓 종료
    public void close() throws IOException {
        if (socket != null) socket.close();
    }
}
