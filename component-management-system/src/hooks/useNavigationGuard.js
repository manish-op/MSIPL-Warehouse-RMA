import { useEffect, useCallback } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Modal } from "antd";
import { ExclamationCircleOutlined } from "@ant-design/icons";

/**
 * Custom hook to protect navigation between different service modes (warehouse/rma)
 * Shows a confirmation modal when user tries to navigate to a different service
 * 
 * @param {string} currentMode - The current service mode ('warehouse' or 'rma')
 */
const useNavigationGuard = (currentMode) => {
    const navigate = useNavigate();
    const location = useLocation();

    const showNavigationWarning = useCallback((targetPath, event) => {
        // Prevent the default navigation
        if (event) {
            event.preventDefault();
        }

        const targetMode = targetPath.startsWith("/rma-") ||
            targetPath.startsWith("/unrepaired") ||
            targetPath.startsWith("/repaired")
            ? "rma"
            : "warehouse";

        // If navigating to same mode, allow it
        if (targetMode === currentMode) {
            return true;
        }

        // Show confirmation modal for cross-service navigation
        Modal.confirm({
            title: "Switch Service?",
            icon: <ExclamationCircleOutlined />,
            content: `You are currently in the ${currentMode === "rma" ? "RMA Request Portal" : "Warehouse Management System"}. 
                Navigating back will take you to the ${targetMode === "rma" ? "RMA Request Portal" : "Warehouse Management System"}. 
                Do you want to continue?`,
            okText: "Yes, Switch",
            cancelText: "Stay Here",
            onOk() {
                // Update the service mode in sessionStorage
                sessionStorage.setItem("msipl_service_mode", targetMode);
                navigate(targetPath);
            },
            onCancel() {
                // Stay on current page - do nothing
            },
        });

        return false;
    }, [currentMode, navigate]);

    useEffect(() => {
        // Handle browser back/forward button
        const handlePopState = (event) => {
            const currentPath = window.location.pathname;
            const currentPathMode = currentPath.startsWith("/rma-") ||
                currentPath.startsWith("/unrepaired") ||
                currentPath.startsWith("/repaired")
                ? "rma"
                : "warehouse";

            // If trying to navigate to different service mode
            if (currentPathMode !== currentMode) {
                // Push current state back to prevent navigation
                window.history.pushState(null, "", location.pathname);

                showNavigationWarning(currentPath, event);
            }
        };

        // Push a state to enable popstate detection
        window.history.pushState(null, "", location.pathname);

        // Listen to browser back/forward
        window.addEventListener("popstate", handlePopState);

        return () => {
            window.removeEventListener("popstate", handlePopState);
        };
    }, [currentMode, location.pathname, showNavigationWarning]);

    return { showNavigationWarning };
};

export default useNavigationGuard;
