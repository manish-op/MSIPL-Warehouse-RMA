import React, { useEffect, useMemo, useState } from "react";
import { Card, Avatar, Spin, message } from "antd";
import { UserOutlined } from "@ant-design/icons";
import Cookies from "js-cookie";
import { URL } from "../API/URL";
import { useNavigate } from "react-router-dom";
import "./Profile.css";

function useCountUp(target, duration = 1000) {
  const [value, setValue] = useState(0);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const to = Number(target) || 0;
    const startTime = performance.now();
    let raf = 0;

    function step(ts) {
      const elapsed = ts - startTime;
      const t = Math.min(1, elapsed / duration);
      const eased = 1 - Math.pow(1 - t, 3); // easeOutCubic
      setProgress(eased);
      const cur = Math.round(to * eased);
      setValue(cur);
      if (t < 1) {
        raf = requestAnimationFrame(step);
      } else {
        setProgress(1);
        setValue(to);
      }
    }

    raf = requestAnimationFrame(step);
    return () => cancelAnimationFrame(raf);
  }, [target, duration]);

  return { value, progress };
}

function useCountUpArray(targetArray = [], duration = 1000) {
  const length = targetArray.length;
  const targetValues = useMemo(() => targetArray.map((v) => Number(v) || 0), [length, ...targetArray]);

  const [values, setValues] = useState(() => targetValues.map(() => 0));
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const from = targetValues.map(() => 0);
    const to = targetValues;
    const startTime = performance.now();
    let raf = 0;

    function step(ts) {
      const elapsed = ts - startTime;
      const t = Math.min(1, elapsed / duration);
      const eased = 1 - Math.pow(1 - t, 3);
      setProgress(eased);

      const next = from.map((f, i) => Math.round(f + (to[i] - f) * eased));
      setValues(next);

      if (t < 1) {
        raf = requestAnimationFrame(step);
      } else {
        setProgress(1);
        setValues(to);
      }
    }

    raf = requestAnimationFrame(step);
    return () => cancelAnimationFrame(raf);
  }, [duration, length, targetValues]);

  return { values, progress };
}

const COLORS = [
  "var(--brand-1)",
  "var(--brand-2)",
  "var(--brand-3)",
  "var(--brand-4)",
  "var(--brand-5)",
];

export default function Profile() {
  const navigate = useNavigate();

  // Profile data from local storage
  const name = localStorage.getItem("name") || "User";
  const email = localStorage.getItem("email") || "N/A";
  const mobile = localStorage.getItem("mobile") || "N/A";
  const role = (localStorage.getItem("_User_role_for_MSIPL") || "N/A").toUpperCase();
  const region = localStorage.getItem("region") || null;

  // Local state
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  function getAuthToken() {
    try {
      const cookie = Cookies.get("authToken");
      if (cookie) return atob(cookie);
    } catch (_) {}
    return localStorage.getItem("authToken");
  }

  async function fetchDashboardSummary() {
    setLoading(true);
    setError(null);
    const token = getAuthToken();
    const headers = {};
    if (token) headers["Authorization"] = `Bearer ${token}`;

    try {
      const res = await fetch(URL + "/admin/user/dashboard-summary", {
        method: "GET",
        credentials: "include",
        headers,
      });

      if (res.status === 401) {
        Cookies.remove("authToken", { path: "/" });
        localStorage.clear();
        message.warning("Session expired. Please login again.");
        navigate("/login");
        return;
      }

      if (!res.ok) {
        const txt = await res.text();
        setError(txt || "Server error");
        setLoading(false);
        return;
      }

      const data = await res.json();
      setSummary(data);
      setLoading(false);
    } catch (err) {
      setError(err.message || "Network error");
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchDashboardSummary();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Prepare animated values
  const totalTarget = summary?.totalItems || 0;
  const { value: animatedTotal, progress: totalProgress } = useCountUp(totalTarget, 1200);

  const regionCounts = useMemo(() => {
    if (!summary || !summary.regionCounts) return [];
    const isAdmin = role === "ADMIN";
    if (!isAdmin && region) {
      return summary.regionCounts.filter((rc) => (rc.region || "").toLowerCase() === (region || "").toLowerCase());
    }
    return summary.regionCounts;
  }, [summary, role, region]);

  const regionTargetCounts = regionCounts.map((r) => r.count || 0);
  const { values: animatedRegionCounts, progress: regionsProgress } = useCountUpArray(regionTargetCounts, 1000);

  const combinedProgress = regionCounts.length > 0 ? (totalProgress + regionsProgress) / 2 : totalProgress;

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return "Good Morning";
    if (hour < 18) return "Good Afternoon";
    return "Good Evening";
  };

  return (
    <div className="profile-dashboard container-flex">
      <h1 className="dashboard-title">My Dashboard</h1>

      <div className="profile-row">
        {/* Left column: Profile Card */}
        <div className="left-col">
          <Card className="user-profile-card">
            <div className="profile-header">
              <Avatar size={100} icon={<UserOutlined />} className="profile-avatar" />
              <h2 className="user-name">{`${getGreeting()}, ${name}`}</h2>
            </div>

            <div className="user-details-list">
              <p><strong>Email:</strong> {email}</p>
              <p><strong>Mobile:</strong> {mobile}</p>
              <p><strong>Role:</strong> {role}</p>
              {region && <p><strong>Region:</strong> {region}</p>}
            </div>
          </Card>
        </div>

        {/* Right column: Stats Card */}
        <div className="right-col">
          <Card className="stats-card">
            {loading ? (
              <div className="centered-spin"><Spin size="large" /></div>
            ) : error ? (
              <div className="error-text">{error}</div>
            ) : (
              <>
                {/* Top stat row */}
                <div className="stat-row">
                  <div className="stat-box main">
                    <div className="stat-label">TOTAL ITEMS</div>
                    <div className="stat-number">{animatedTotal.toLocaleString()}</div>
                  </div>

                  <div className="stat-box secondary">
                    <div className="stat-label">REGIONS</div>
                    <div className="stat-number">{regionCounts.length}</div>
                  </div>
                </div>

                
                <div style={{ margin: "8px 0 14px 0" }}>
                  <div style={{
                    height: 8,
                    width: "100%",
                    background: "var(--progress-track)", 
                    borderRadius: 6,
                    overflow: "hidden",
                    boxShadow: "inset 0 1px 2px rgba(0,0,0,0.03)"
                  }}>
                    <div style={{
                      height: "100%",
                      width: `${Math.round(combinedProgress * 100)}%`,
                      background: "linear-gradient(90deg, var(--brand-2), var(--brand-1))",
                      transition: "width 180ms linear"
                    }} />
                  </div>
                </div>

                {/* Region cards grid */}
                <div className="region-cards-grid">
                  {regionCounts.map((rc, idx) => {
                    const color = COLORS[idx % COLORS.length];
                    const animated = animatedRegionCounts[idx] ?? 0;
                    return (
                      <div className="region-card" key={rc.region || idx} style={{ borderColor: color }}>
                        <div className="region-pill" style={{ background: color }}>
                          {String(rc.region || "R").charAt(0).toUpperCase()}
                        </div>
                        <div className="region-info">
                          <div className="region-name">{String(rc.region || "UNKNOWN").toUpperCase()}</div>
                          <div className="region-count">{animated.toLocaleString()} items</div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}