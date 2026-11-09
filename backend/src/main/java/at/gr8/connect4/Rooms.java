package at.gr8.connect4;
import java.util.*;
import java.io.IOException;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
@Component
public class Rooms extends TextWebSocketHandler {
 private final JsonMapper json=new JsonMapper();
 private final Map<String,Room> rooms=new HashMap<>();
 private final Map<String,Room> memberships=new HashMap<>();
 private final SecureRandom random=new SecureRandom();
 private static class Room {
  String code; Game game=new Game(); List<WebSocketSession> players=new ArrayList<>();
  List<String> names=new ArrayList<>(); Set<String> rematch=new HashSet<>();
  Room(String code){this.code=code;}
 }
 @Override protected synchronized void handleTextMessage(WebSocketSession s,TextMessage message) throws Exception {
  try {
   JsonNode m=json.readTree(message.getPayload()); String type=m.path("type").asText(); Room room=memberships.get(s.getId());
   if(type.equals("create") || type.equals("join")) {
    if(room!=null) throw new IllegalArgumentException("Du bist bereits in einem Raum.");
    String name=m.path("name").asText("").trim();
    if(name.isEmpty() || name.length()>24) throw new IllegalArgumentException("Bitte gib einen Namen mit 1–24 Zeichen ein.");
    if(type.equals("create")) {
     if(rooms.size()>=1000) throw new IllegalArgumentException("Der Server ist voll. Bitte später versuchen.");
     String code; do { code=""; for(int i=0;i<6;i++) code+="ABCDEFGHJKLMNPQRSTUVWXYZ23456789".charAt(random.nextInt(31)); } while(rooms.containsKey(code));
     room=new Room(code); rooms.put(code,room);
    } else {
     room=rooms.get(m.path("code").asText("").toUpperCase(Locale.ROOT));
     if(room==null) throw new IllegalArgumentException("Dieser Raum existiert nicht mehr.");
     if(room.players.size()>=2) throw new IllegalArgumentException("Dieser Raum ist bereits voll.");
    }
    room.players.add(s); room.names.add(name); memberships.put(s.getId(),room); broadcast(room); return;
   }
   if(room==null) throw new IllegalArgumentException("Bitte erstelle einen Raum oder tritt einem bei.");
   switch(type) {
    case "move" -> {
     if(room.players.size()!=2 || room.players.stream().anyMatch(p->!p.isOpen())) throw new IllegalArgumentException("Warte auf deinen Mitspieler.");
     if(!m.path("column").isInt()) throw new IllegalArgumentException("Ungültige Spalte.");
     room.game.move(room.players.indexOf(s)+1,m.path("column").asInt()); broadcast(room);
    }
    case "rematch" -> {
     if(!room.game.finished()) throw new IllegalArgumentException("Die Runde läuft noch.");
     room.rematch.add(s.getId());
     if(room.rematch.size()==2) {room.game=new Game(); Collections.reverse(room.players); Collections.reverse(room.names); room.rematch.clear();}
     broadcast(room);
    }
    case "leave" -> leave(s);
    default -> throw new IllegalArgumentException("Unbekannte Aktion.");
   }
  } catch(IllegalArgumentException e) {send(s,Map.of("type","error","message",e.getMessage()));}
  catch(tools.jackson.core.JacksonException e){send(s,Map.of("type","error","message","Ungültige Nachricht."));}
 }
 private void send(WebSocketSession s,Object value) throws IOException {if(s.isOpen()) s.sendMessage(new TextMessage(json.writeValueAsString(value)));}
 private void broadcast(Room r) throws IOException {
  for(int i=0;i<r.players.size();i++) send(r.players.get(i),Map.of("type","state","code",r.code,"you",i+1,"names",r.names,"game",r.game,"rematch",r.rematch.size(),"requestedRematch",r.rematch.contains(r.players.get(i).getId())));
 }
 private void leave(WebSocketSession s) throws IOException {
  Room r=memberships.remove(s.getId()); if(r==null)return;
  rooms.remove(r.code);
  for(WebSocketSession p:r.players) memberships.remove(p.getId());
  for(WebSocketSession p:r.players) {
   try {send(p,Map.of("type","closed","message",p==s?"Du hast den Raum verlassen.":"Dein Mitspieler hat den Raum verlassen. Erstelle einen neuen Raum für die nächste Partie."));}
   catch(IOException | IllegalStateException ignored) { /* A closing socket must not prevent notifying the other player. */ }
  }
 }
 @Override public synchronized void afterConnectionClosed(WebSocketSession s,CloseStatus status) throws Exception {leave(s);}
 @Override public void afterConnectionEstablished(WebSocketSession s){s.setTextMessageSizeLimit(2048);}
}
