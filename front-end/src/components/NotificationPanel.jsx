import React from 'react';
import styles from './css/NotificationPanel.module.css';

const NotificationPanel = ({ notifications = [], onClose }) => {
  return (
    <div className={styles['notification-panel']}>
      <div className={styles['notification-panel-title']}>{/* Notifications */}</div>
      <div className={styles['notification-list']}>
        {notifications.length === 0 ? (
          <div className={styles['notification-empty']}>{/* No notifications */}</div>
        ) : (
          notifications.map((n, i) => (
            <div
              key={i}
              className={
                styles['notification-item'] + (n.unread ? ' ' + styles['unread'] : '')
              }
            >
              {n.text}
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default NotificationPanel;
