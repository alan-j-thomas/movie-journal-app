import React, { useState } from 'react';
import './css/AIChatBox.css';

const AIChatBox = ({ onClose }) => {
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);

  const askAI = async () => {
    if (!input) return;
    setLoading(true);
    // Add user message
    setMessages(prev => [...prev, { sender: 'You', text: input, you: true }]);
    const res = await fetch('http://localhost:8088/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ promptMessage: input }),
    });
    const text = await res.text();
    // Add AI response
    setMessages(prev => [...prev, { sender: 'AI', text, you: false }]);
    setInput('');
    setLoading(false);
  };

  return (
    <div className="ai-chatbox-modal">
      <div className="ai-chatbox-header">
        <h3 className="ai-chatbox-title">Ask Me Anything</h3>
        <button className="ai-chatbox-close" onClick={onClose}>×</button>
      </div>
      <div className="ai-chatbox-messages">
        {messages.map((msg, idx) => (
          <div
            key={idx}
            className={
              'ai-chatbox-message' + (msg.you ? ' you' : '')
            }
          >
            <span className="ai-chatbox-message-sender">{msg.sender}:</span> {msg.text}
          </div>
        ))}
      </div>
      <form
        className="ai-chatbox-form"
        onSubmit={e => {
          e.preventDefault();
          askAI();
        }}
      >
        <textarea
          className="ai-chatbox-input"
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="Type your question..."
          rows={2}
        />
        <button className="ai-chatbox-send" type="submit" disabled={loading || !input}>
          {loading ? 'Thinking...' : 'Send'}
        </button>
      </form>
    </div>
  );
};

export default AIChatBox;
