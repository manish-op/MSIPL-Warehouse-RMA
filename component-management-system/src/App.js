import "./App.css";
import { Routes, Route } from "react-router-dom";
import HomePage from "./Components/HomePage/HomePage";
import LoginForm from "./Components/LoginPage/LoginForm";
import Dashboard from "./Components/Dashboard/Dashboard";
import Profile from "./Components/UserProfile/Profile";
import Logout from "./Components/Logout/Logout";
import ChangePassword from "./Components/UserProfile/ChangePassword";
import AddEmployee from "./Components/Employee/AddEmployee/AddEmployee";
import EmployeeManagement from "./Components/Employee/EmployeeManagement";
import RegionManagement from "./Components/Region/RegionManagement";
import KeywordManagement from "./Components/Keyword/KeywordManagement.js";
import AddItem from "./Components/Items/AddItem/AddItem.js";
import UpdateItem from "./Components/Items/UpdateItem/UpdateItem.js";
import HistoryTable from "./Components/Items/HistoryRelated/HistoryTable.js";
import ProtectedRoute from "./Components/ProtectedRoute/ProtectedRoute.js";
import RepairingPage from "./Components/FRU/Repairing/RepairingPage.js";
import RepairingDashboardForManager from "./Components/FRU/Repairing/AdminOrManagerSection/RepairingDashboardForManager.js";
import InwardGatePass from "./Components/GatePass/InwardGatePass.js";
import ImportExport from "./Components/ImportExport/ImportExport.js";
import OutwardGatePass from "./Components/GatePass/OutwardGatePass.js";
import "@ant-design/v5-patch-for-react-19";
import StatusManagement from "./Components/StatusOptionComp/StatusManagement.js";
import { ThemeProvider } from "./context/ThemeContext.js";
import "bootstrap/dist/css/bootstrap.min.css";
import ChatComponent from "./Components/Chat/ChatComponent.js";
import EmployeeList from "./Components/EmployeeList/EmployeeList.js";
import ThresholdManager from "./Components/Threshold/ThresholdManager.js";
import ActiveAlerts from "./Components/Alerts/ActiveAlert.js";
import ItemSearch from "./Components/Items/ItemSearch/ItemSearch.js";
import EnhancedActivityFeed from "./Components/ActivityLogs/EnhancedActivityFeed.js";
import RmaDashboard from "./Components/RMA/RMADashboard.js";
import RmaRequestForm from "./Components/RMA/RmaRequestForm.js";
import RepairRequestForm from "./Components/RMA/RepairRequestForm.js";
import { useLocation } from "react-router-dom";
import UnrepairedPage from "./Components/Repair/UnrepairedPage.js";
import RepairedPage from "./Components/Repair/RepairedPage.js";
import AssignedPage from "./Components/Repair/AssignedPage.js";
import CantBeRepairedPage from "./Components/Repair/CantBeRepairedPage.js";
import AuditTrail from "./Components/RMA/AuditTrail.js";
import DepotDispatchPage from "./Components/RMA/DepotDispatchPage.js";
import FeedbackButton from "./Components/Feedback/FeedbackButton.js";

// checking the branch changes

const RepairRequestFormWrapper = () => {
  const location = useLocation();
  const { formData, items } = location.state || { formData: {}, items: [] };
  return <RepairRequestForm formData={formData} items={items} onFormSubmit={() => { }} />;
};

function App() {
  return (
    <ThemeProvider>
      <div className="App">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginForm />} />

          {/* All protected routes */}
          <Route element={<ProtectedRoute />}>
            {/* Warehouse dashboard and its nested routes */}
            <Route path="/dashboard" element={<Dashboard />}>
              {/* user profile related */}
              <Route path="profile" element={<Profile />} />
              <Route path="changePassword" element={<ChangePassword />} />
              <Route path="logout" element={<Logout />} />

              {/* Employee related */}
              <Route path="addEmployee" element={<AddEmployee />} />
              <Route path="employeeManagement" element={<EmployeeManagement />} />

              {/* Region Management */}
              <Route path="regionManagement" element={<RegionManagement />} />

              {/* Keyword Management */}
              <Route path="keywordManagement" element={<KeywordManagement />} />

              {/* Items */}
              <Route path="addItem" element={<AddItem />} />

              <Route path="updateItem" element={<UpdateItem />} />


              <Route path="historyTable" element={<HistoryTable />} />
              <Route
                path="statusManagement"
                element={<StatusManagement />}
              />

              {/* Item repairing */}
              <Route path="itemRepairing" element={<RepairingPage />} />

              {/* FRU manager dashboard */}
              <Route
                path="tickedDashboard"
                element={<RepairingDashboardForManager />}
              />

              {/* Import / Export */}
              <Route
                path="import_export_CSV"
                element={<ImportExport />}
              />

              {/* GatePass */}
              <Route path="inwardGatePass" element={<InwardGatePass />} />
              <Route path="outwardGatePass" element={<OutwardGatePass />} />

              {/* Activity / Alerts */}
              <Route path="activity-logs" element={<ChatComponent />} />
              <Route path="all-users" element={<EmployeeList />} />
              <Route path="thresholds" element={<ThresholdManager />} />
              <Route path="alerts/active" element={<ActiveAlerts />} />
              <Route path="itemSearch" element={<ItemSearch />} />

              <Route path="items/activity" element={<EnhancedActivityFeed />} />
            </Route>

            {/* RMA side (separate top-level paths) */}
            <Route path="/rma-dashboard" element={<RmaDashboard />} />
            <Route path="/rma-requests" element={<RmaRequestForm />} />
            <Route path="/rma-generate-form" element={<RepairRequestFormWrapper />} />
            <Route path="/unrepaired" element={<UnrepairedPage />} />
            <Route path="/assigned" element={<AssignedPage />} />
            <Route path="/repaired" element={<RepairedPage />} />
            <Route path="/cant-be-repaired" element={<CantBeRepairedPage />} />
            <Route path="/depot-dispatch" element={<DepotDispatchPage />} />
            <Route path="/audit-trail" element={<AuditTrail />} />
          </Route>
        </Routes>
        <FeedbackButton />
      </div>
    </ThemeProvider>
  );
}

export default App;
