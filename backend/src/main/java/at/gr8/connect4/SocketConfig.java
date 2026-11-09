package at.gr8.connect4;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
@Configuration @EnableWebSocket
public class SocketConfig implements WebSocketConfigurer {
 private final Rooms rooms;
 public SocketConfig(Rooms rooms){this.rooms=rooms;}
 public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){registry.addHandler(rooms,"/ws");}
}
