import React, { useState } from 'react';
import AIChatBox from './AIChatBox';
import './css/FloatingAIButton.css';

const FloatingAIButton = () => {
  const [open, setOpen] = useState(false);

  return (
    <>
      <button className="floating-ai-action-btn" onClick={() => setOpen(true)}>
        <img src='../src/assets/cloud.png' />
      </button>
      {open && <AIChatBox onClose={() => setOpen(false)} />}
    </>
  );
};

export default FloatingAIButton;
