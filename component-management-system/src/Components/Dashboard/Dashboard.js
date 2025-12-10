import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import "./Dashboard.css";
import Sidebar from "../Sidebar/Sidebar";
import { Outlet } from "react-router-dom";
import { ItemProvider } from "../Items/UpdateItem/ItemContext";
import useNavigationGuard from "../../hooks/useNavigationGuard"; // Import navigation guard


function Dashboard() {
  // Protect navigation from Warehouse to RMA
  useNavigationGuard("warehouse");
  return (
    <>

      <ItemProvider>

        <div className="mainPage">
          <Header />
          <div className="Dashboard-content">
            <Sidebar />
            <div className="content">
              <Outlet />
            </div>
          </div>
          <Footer />
        </div>

      </ItemProvider>
    </>
  );
}

export default Dashboard;