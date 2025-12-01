package client.socket;

import java.util.List;

public class SocketMessage {

    // 공통 필드 (JOIN, CHAT, SYSTEM, DASHBOARD_UPDATE 등에서 사용)
    private String type;   // "JOIN", "CHAT", "DASHBOARD_UPDATE" ...
    private Integer floor;
    private String room;
    private String role;   // "USER", "ADMIN", "SENSOR", "SYSTEM"
    private String sender; // 닉네임 / 센서ID / SYSTEM
    private String msg;    // 채팅 내용이나 시스템 메시지

    // 좌석/체크인 관련 (나중에 SEAT_UPDATE 쓰고 싶으면)
    private Integer seatNo;
    private String userId;

    // 센서 관련
    private Double temp;
    private Double co2;
    private Double lux;

    // SEAT_UPDATE용 좌석 리스트 추가
    private List<SeatInfo> seats;

    // --- 내부 클래스: 좌석 상태 정보 ---
    public static class SeatInfo {
        private Integer seatNo;        // 좌석 번호
        private String state;          // "EMPTY", "IN_USE", "AWAY"
        private String userId;         // 자리 주인 (null이면 공석)
        private Integer remainSeconds; // 남은 시간(있으면)

        public Integer getSeatNo() { return seatNo; }
        public void setSeatNo(Integer seatNo) { this.seatNo = seatNo; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public Integer getRemainSeconds() { return remainSeconds; }
        public void setRemainSeconds(Integer remainSeconds) { this.remainSeconds = remainSeconds; }
    }

    // seats Getter/Setter
    public List<SeatInfo> getSeats() { return seats; }
    public void setSeats(List<SeatInfo> seats) { this.seats = seats; }

    // === 기본 생성자 ===
    public SocketMessage() {}

    // === getter / setter ===
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public Integer getSeatNo() { return seatNo; }
    public void setSeatNo(Integer seatNo) { this.seatNo = seatNo; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Double getTemp() { return temp; }
    public void setTemp(Double temp) { this.temp = temp; }

    public Double getCo2() { return co2; }
    public void setCo2(Double co2) { this.co2 = co2; }

    public Double getLux() { return lux; }
    public void setLux(Double lux) { this.lux = lux; }
}
