import "./Footer.css";
// 1. Import useLocation from react-router-dom
import { useLocation } from "react-router-dom";

function Footer() {
  // 2. Get the current location object
  const location = useLocation();

  // 3. Define the list of paths where the footer should be hidden
  //    Add any other paths as needed (e.g., '/print-preview', '/user/profile')
  const hiddenOnPaths = ["/login", "/register"];

  // 4. Check if the current pathname is in the list
  if (hiddenOnPaths.includes(location.pathname)) {
    return null; // Don't render anything
  }

  // 5. If not hidden, return the footer as normal
  return (
    <footer className="footer">
      <div className="footer-left">
        <span>© {new Date().getFullYear()} Motorola Solutions India Pvt Ltd</span>
      </div>

      <div className="footer-center">
        <span>
          📍 Address:{" "}
          <a
            target="_blank"
            rel="noopener noreferrer"
            href="https://maps.app.goo.gl/nqPP5yeCWq1sPX6q9"
          >
            Gurgaon, Haryana, India
          </a>
        </span>
        <span>📞 Contact: 01244192000</span>
      </div>

      <div className="footer-right">
        <a
          target="_blank"
          rel="noopener noreferrer"
          href="https://www.motorolasolutions.com/en_us/about.html"
        >
          About
        </a>
        <a
          target="_blank"
          rel="noopener noreferrer"
          href="https://sites.google.com/motorolasolutions.com/india-managed-support-services/india-support-services/fso-useful-links"
        >
          Help
        </a>
      </div>
    </footer>
  );
}

export default Footer;