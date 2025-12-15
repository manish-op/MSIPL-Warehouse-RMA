import "./App.css";
import { Routes, Route } from "react-router-dom";
import HomePage from "./Components/HomePage/HomePage";
import LoginForm from "./Components/LoginPage/LoginForm";
import Dashboard from "./Components/Dashboard/Dashboard";
import Profile from "./Components/UserProfile/Profile";
import Logout from "./Components/Logout/Logout";
import ChangePassword from "./Components/UserProfile/ChangePassword";
import AddEmployee from "./Components/Employee/AddEmployee/AddEmployee";
import ChangeEmployeePassword from "./Components/Employee/ChnageEmployeePassword/ChangeEmployeePassword";
import AssignEmployeeRegion from "./Components/Region/EmployeeRelated/AssignEmployeeRegion";
import ChangeEmployeeRole from "./Components/Role/ChangeEmployeeRole";
import AddRegion from "./Components/Region/Add/AddRegion";
import UpdateRegion from "./Components/Region/Update/UpdateRegion";
import AddKeyword from "./Components/Keyword/Add/Keyword/AddKeyword";
import AddSubKeyword from "./Components/Keyword/Add/SubKeyword/AddSubKeyword.js";
import AddItem from "./Components/Items/AddItem/AddItem.js";
import GetItemSearchBySerialNo from "./Components/Items/GetItem/GetItemSearchBySerialNo.js";
import UpdateItem from "./Components/Items/UpdateItem/UpdateItem.js";
import GetItemByKeyword from "./Components/Items/GetItem/GetItemByKeyword.js";
import GetHistoryBySerialNo from "./Components/Items/HistoryRelated/GetHistoryBySerialNo.js";
import HistoryTable from "./Components/Items/HistoryRelated/HistoryTable.js";
import UpdateKeyword from "./Components/Keyword/Update/Keyword/UpdateKeyword.js";
import UpdateSubKeyword from "./Components/Keyword/Update/SubKeyword/UpdateSubKeyword.js";
import KeywordManagement from "./Components/Keyword/KeywordManagement.js";
import ProtectedRoute from "./Components/ProtectedRoute/ProtectedRoute.js";
import RepairingPage from "./Components/FRU/Repairing/RepairingPage.js";
import RepairingDashboardForManager from "./Components/FRU/Repairing/AdminOrManagerSection/RepairingDashboardForManager.js";
import InwardGatePass from "./Components/GatePass/InwardGatePass.js";
import ImportExport from "./Components/ImportExport/ImportExport.js";
import OutwardGatePass from "./Components/GatePass/OutwardGatePass.js";
import "@ant-design/v5-patch-for-react-19";
import AddAvailabilityStatus from "./Components/StatusOptionComp/AvailabilityStatus/AddAvailabilityStatus.js";
import UpdateAvailabilityStatus from "./Components/StatusOptionComp/AvailabilityStatus/UpdateAvailabilityStatus.js";
import AddItemStatusOption from "./Components/StatusOptionComp/ItemStatus/AddItemStatusOption.js";
import UpdateItemStatusOption from "./Components/StatusOptionComp/ItemStatus/UpdateItemStatusOption.js";
import { ThemeProvider } from "./context/ThemeContext.js";
import "bootstrap/dist/css/bootstrap.min.css";
import ChatComponent from "./Components/Chat/ChatComponent.js";
import EmployeeList from "./Components/EmployeeList/EmployeeList.js";
import ThresholdManager from "./Components/Threshold/ThresholdManager.js";
import NotificationBell from "./Components/NotificationBell/NotificationBell.js";
import ActivityFeed from "./Components/ActivityLogs/ActivityFeed.js";
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
              <Route
                path="changEmployeePass"
                element={<ChangeEmployeePassword />}
              />
              <Route
                path="changEmployeeRegion"
                element={<AssignEmployeeRegion />}
              />
              <Route
                path="changEmployeeRole"
                element={<ChangeEmployeeRole />}
              />

              {/* Region */}
              <Route path="addNewRegion" element={<AddRegion />} />
              <Route path="updateRegion" element={<UpdateRegion />} />

              {/* Keyword / SubKeyword */}
              <Route path="addKeyword" element={<AddKeyword />} />
              <Route path="updateKeyword" element={<UpdateKeyword />} />
              <Route path="addSubKeyword" element={<AddSubKeyword />} />
              <Route path="updateSubKeyword" element={<UpdateSubKeyword />} />
              <Route path="keywordManagement" element={<KeywordManagement />} />

              {/* Items */}
              <Route path="addItem" element={<AddItem />} />
              <Route
                path="getItemBySerial"
                element={<GetItemSearchBySerialNo />}
              />
              <Route
                path="getItemByKeyword"
                element={<GetItemByKeyword />}
              />
              <Route path="updateItem" element={<UpdateItem />} />
              <Route path="itemHistory" element={<GetHistoryBySerialNo />} />
              <Route path="historyTable" element={<HistoryTable />} />
              <Route
                path="addAvailStatus"
                element={<AddAvailabilityStatus />}
              />
              <Route
                path="updateAvailStatus"
                element={<UpdateAvailabilityStatus />}
              />
              <Route path="addItemStatus" element={<AddItemStatusOption />} />
              <Route
                path="UpdateItemStatus"
                element={<UpdateItemStatusOption />}
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
              <Route path="alerts/active" element={<NotificationBell />} />
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
      </div>
    </ThemeProvider>
  );
}

export default App;
