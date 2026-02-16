import { useState, useRef, useEffect } from 'react';
import { askChat } from '../api/client';
import './Chatbot.css';

export default function Chatbot() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  useEffect(scrollToBottom, [messages]);

  const handleSend = async (e) => {
    e?.preventDefault();
    const text = input.trim();
    if (!text || loading) return;

    setInput('');
    setMessages((prev) => [...prev, { role: 'user', text }]);
    setLoading(true);

    try {
      const reply = await askChat(text);
      setMessages((prev) => [...prev, { role: 'assistant', text: reply }]);
    } catch (err) {
      setMessages((prev) => [...prev, { role: 'assistant', text: `Sorry, something went wrong: ${err.message}`, error: true }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <button
        type="button"
        className="chatbot-toggle"
        onClick={() => setOpen((o) => !o)}
        aria-label={open ? 'Close chat' : 'Open chat'}
      >
        {open ? '✕' : '💬'}
      </button>
      {open && (
        <div className="chatbot-panel card">
          <div className="chatbot-header">
            <span className="chatbot-title">Support Chat</span>
            <button type="button" className="chatbot-close" onClick={() => setOpen(false)} aria-label="Close">
              ✕
            </button>
          </div>
          <div className="chatbot-messages">
            {messages.length === 0 && (
              <p className="chatbot-placeholder">Ask about orders, shipping, returns, or products.</p>
            )}
            {messages.map((m, i) => (
              <div key={i} className={`chatbot-msg chatbot-msg--${m.role}`}>
                <span className="chatbot-msg-label">{m.role === 'user' ? 'You' : 'Support'}</span>
                <p className={m.error ? 'chatbot-msg-error' : ''}>{m.text}</p>
              </div>
            ))}
            {loading && (
              <div className="chatbot-msg chatbot-msg--assistant">
                <span className="chatbot-msg-label">Support</span>
                <p className="chatbot-typing">Thinking…</p>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>
          <form className="chatbot-form" onSubmit={handleSend}>
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type your question…"
              disabled={loading}
            />
            <button type="submit" className="btn btn-primary" disabled={loading || !input.trim()}>
              Send
            </button>
          </form>
        </div>
      )}
    </>
  );
}
