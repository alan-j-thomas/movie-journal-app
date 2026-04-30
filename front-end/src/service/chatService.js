import SockJS from 'sockjs-client/dist/sockjs';
import { Client } from '@stomp/stompjs';

// userId: not used for connection, but kept for compatibility
// userEmail: pass the user's email for principal mapping
export function connectChat(userId, onMessage, userEmail) {
  const wsUrl = `http://localhost:8086/ws?email=${encodeURIComponent(userEmail)}`;
  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    onConnect: () => {
      client.subscribe('/user/queue/messages', (msg) => {
        onMessage(JSON.parse(msg.body));
      });
    }
  });
  client.activate();
  return client;
}
