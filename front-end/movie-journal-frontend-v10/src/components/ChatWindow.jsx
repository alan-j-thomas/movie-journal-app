import React, { useEffect, useRef, useState } from 'react';
import { connectChat } from '../service/chatService';
import styles from './css/ChatWindow.module.css';
import overlayStyles from './css/ChatOverlay.module.css';
import { formatMarkdown } from '../utils/formatMarkdown';

const ChatWindow = ({ user, friendEmail, friendName, onClose }) => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const stompClient = useRef(null);

  useEffect(() => {
    if (!user || !friendEmail) return;
    stompClient.current = connectChat(user.userId, () => {}, user.sub);
    // Subscribe to /user/queue/messages for private messages
    let subscription;
    if (stompClient.current && stompClient.current.connected) {
      subscription = stompClient.current.subscribe('/user/queue/messages', (msg) => {

        console.log('[ChatWindow] Received message:', msg.body);
        
        const incoming = JSON.parse(msg.body);
        console.log('Received message:', incoming);
        setMessages((prev) => {
          // Only add if not already present (by content, sender, recipient, and type)
          if (prev.some(m =>
            m.content === incoming.content &&
            m.sender === incoming.sender &&
            m.recipient === incoming.recipient &&
            m.type === incoming.type
          )) {
            return prev;
          }
          return [...prev, incoming];
        });
      });
    } else {
      // If not connected yet, wait for connection
      const interval = setInterval(() => {
        if (stompClient.current && stompClient.current.connected) {
          subscription = stompClient.current.subscribe('/user/queue/messages', (msg) => {
            const incoming = JSON.parse(msg.body);
            console.log('Received message (delayed):', incoming);
            setMessages((prev) => {
              if (prev.some(m =>
                m.content === incoming.content &&
                m.sender === incoming.sender &&
                m.recipient === incoming.recipient &&
                m.type === incoming.type
              )) {
                return prev;
              }
              return [...prev, incoming];
            });
          });
          clearInterval(interval);
        }
      }, 100);
    }
    return () => {
      if (subscription) subscription.unsubscribe();
      if (stompClient.current) stompClient.current.deactivate();
    };
  }, [user, friendEmail]);

  const sendMessage = (e) => {
    e.preventDefault();
    if (input && stompClient.current) {
      const newMsg = {
        sender: user.sub,
        recipient: friendEmail,
        content: input,
        type: 'CHAT'
      };
      stompClient.current.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(newMsg)
      });
      setInput('');
    }
  };

  const sendAIMessage = () => {
    if (input && stompClient.current) {
      // Send to backend for AI response - backend will handle showing to both users
      const aiMsg = {
        sender: user.sub,
        recipient: friendEmail,
        content: input,
        type: 'AI_REQUEST'
      };
      stompClient.current.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(aiMsg)
      });
      setInput('');
    }
  };

  return (
    <div className={overlayStyles['chat-overlay']}>
      <div className={styles['chat-window'] + ' ' + overlayStyles['chat-window-animate']}>
        <div className={styles['chat-window-header']}>
          <h3 className={styles['chat-window-title']}>
            Chat with <span style={{color: '#222'}}>{friendName || friendEmail}</span>
          </h3>
          <button className={styles['chat-window-close']} onClick={onClose}>×</button>
        </div>
        <div className={styles['chat-window-messages']}>
          {messages
            .filter(msg =>
              (msg.sender === user.sub && msg.recipient === friendEmail) ||
              (msg.sender === friendEmail && msg.recipient === user.sub) ||
              msg.type === 'AI_RESPONSE' ||
              msg.type === 'AI_REQUEST'
            )
            .map((msg, idx) => (
              <div
                key={idx}
                className={
                  styles['chat-window-message'] +
                  (msg.sender === user.sub && msg.type !== 'AI_REQUEST' ? ' ' + styles['you'] : '') +
                  (msg.type === 'AI_RESPONSE' ? ' ' + styles['ai-message'] : '') +
                  (msg.type === 'AI_REQUEST' && msg.sender === user.sub ? ' ' + styles['ai-request'] : '') +
                  (msg.type === 'AI_REQUEST' && msg.sender !== user.sub ? ' ' + styles['ai-request-other'] : '')
                }
              >
                <span className={styles['chat-window-message-sender']}>
                  {msg.type === 'AI_RESPONSE' ? '🤖 AI Assistant' : 
                   msg.type === 'AI_REQUEST' ? (msg.sender === user.sub ? 'You (asking AI)' : `${friendName || friendEmail} (asking AI)`) :
                   msg.sender === user.sub ? 'You' : (friendName || friendEmail)}
                </span>
                <br />
                {msg.type === 'AI_RESPONSE' ? 
                  <div dangerouslySetInnerHTML={{__html: formatMarkdown(msg.content)}} /> :
                  msg.content
                }
              </div>
            ))}
        </div>
        <form className={styles['chat-window-form']} onSubmit={sendMessage}>
          <input
            className={styles['chat-window-input']}
            value={input}
            onChange={e => setInput(e.target.value)}
          />
          <button type="submit" className={styles['chat-window-send']}>
            Send
          </button>
          <button type="button" className={styles['chat-window-ai']} onClick={sendAIMessage}>
            🤖 Ask AI
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChatWindow;
