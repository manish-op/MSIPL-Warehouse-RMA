import React, { useEffect, useState } from "react";
import Cookies from "js-cookie";
import { List, Avatar, Card, Skeleton, message } from "antd";
import { formatDistanceToNow } from "date-fns";
import { URL } from "../API/URL";
import "./ActivityFeed.css";

function getToken() {
  try {
    const cookie = Cookies.get("authToken");
    if (cookie) return atob(cookie);
  } catch (e) {}
  return null;
}

export default function ActivityFeed({ limit = 10 }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  async function fetchActivities() {
    setLoading(true);
    try {
      const token = getToken();
      const headers = token ? { Authorization: `Bearer ${token}` } : {};
      const res = await fetch(`${URL}/items/activity?limit=${limit}`, { headers });
      if (res.status === 401) {
        message.warning("Session expired. Please login again.");
        // optional: redirect to login
        setItems([]);
        setLoading(false);
        return;
      }
      if (!res.ok) {
        const txt = await res.text();
        message.error(`Failed to load activity: ${txt}`);
        setItems([]);
        setLoading(false);
        return;
      }
      const json = await res.json();
      setItems(json || []);
    } catch (err) {
      console.error(err);
      message.error("Network error while fetching activity");
      setItems([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchActivities();

  }, []);

  return (
    <Card title="Recent Activity" className="activity-card">
      {loading ? (
        <Skeleton active paragraph={{ rows: 6 }} />
      ) : (
        <List
          dataSource={items}
          locale={{ emptyText: "No recent activity" }}
          renderItem={(item) => (
            <List.Item className="activity-item" key={item.id}>
              <List.Item.Meta
                avatar={<Avatar>{(item.updatedByEmail || "U").charAt(0).toUpperCase()}</Avatar>}
                title={
                  <div className="activity-title">
                    <strong>{item.updatedByEmail}</strong>
                    {item.remark ? <span className="muted"> — {item.remark}</span> : null}
                  </div>
                }
                description={
                  <div className="activity-desc">
                    {item.serialNo ? <strong>{"Serial No. "+item.serialNo}</strong> : null}
                    {item.region ? <span> • {"updated for region "+item.region}</span> : null}
                    <div className="activity-time">
                      {item.updateDate ? formatDistanceToNow(new Date(item.updateDate), { addSuffix: true }) : null}
                    </div>
                  </div>
                }
              />
            </List.Item>
          )}
        />
      )}
    </Card>
  );
}
