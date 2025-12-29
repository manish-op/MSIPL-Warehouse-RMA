import { RmaRequestApi } from "./RmaRequest.api";
import { RmaDashboardApi } from "./RmaDashboard.api";
import { RmaWorkflowApi } from "./RmaWorkflow.api";
import { RmaDepotApi } from "./RmaDepot.api";
import { RmaDocumentsApi } from "./RmaDocuments.api";
import { RmaCommonApi } from "./RmaCommon.api";

export const RmaApi = {
    ...RmaRequestApi,
    ...RmaDashboardApi,
    ...RmaWorkflowApi,
    ...RmaDepotApi,
    ...RmaDocumentsApi,
    ...RmaCommonApi,
};
