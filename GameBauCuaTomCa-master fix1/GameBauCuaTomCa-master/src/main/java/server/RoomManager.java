package server;

import java.util.*;

public class RoomManager {
    private final List<GameRoom> rooms = new ArrayList<>();

    // Tìm room chưa đủ người hoặc tạo mới
    public synchronized GameRoom joinAnyRoom(PlayerHandler player) {
        for (GameRoom room : rooms) {
            if (!room.isFull()) {
                room.addPlayer(player);
                return room;
            }
        }
        // Nếu tất cả phòng đều đầy, tạo phòng mới
        GameRoom newRoom = new GameRoom();
        newRoom.addPlayer(player);
        rooms.add(newRoom);
        return newRoom;
    }

    // Có thể thêm các phương thức quản lý phòng, xóa phòng rỗng, v.v.
    public synchronized void removeEmptyRooms() {
        rooms.removeIf(GameRoom::isEmpty);
    }
}
