import React, { useState, useEffect } from "react";
import {
  Form,
  Select,
  Upload,
  Button,
  message,
  Card,
  Segmented,
  Typography,
} from "antd";
import { UploadOutlined, DownloadOutlined, SwapOutlined } from "@ant-design/icons";
import GetRegionAPI from "../API/Region/GetRegion/GetRegionAPI";
import ExportCsvApi from "../API/ImportExport/ExportCsvApi";
import ImportCsvApi from "../API/ImportExport/ImportCsvApi";


const { Title, Text } = Typography;

function ImportExport() {
  const [form] = Form.useForm();
  const [regions, setRegions] = useState();
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const [currentFileList, setCurrentFileList] = useState([]);
  const [activeView, setActiveView] = useState("export");

  useEffect(() => {
    const fetchRegions = async () => {
      try {
        const data = await GetRegionAPI();
        if (data) {
          setRegions(data);
        }
      } catch (error) {
        console.error("API Error:", error);
      }
    };
    fetchRegions();
  }, []);

  // --- Import CSV Logic ---
  const onImportFinish = async (values) => {
    if (values.csvFile && values.csvFile.length > 0) {
      const fileObjectFromAnt = values.csvFile[0];
      const file = fileObjectFromAnt.originFileObj;
      const formData = new FormData();
      formData.append("file", file, file.name);
      await ImportCsvApi(formData);
    } else {
      message.error("Please select a CSV file to upload.");
    }
  };

  const beforeUploadHandler = (file) => {
    if (!file || typeof file.name !== "string") {
      message.error("Invalid file object or missing file name.");
      return Upload.LIST_IGNORE;
    }
    const isCSV =
      file.type === "text/csv" || file.name.toLowerCase().endsWith(".csv");
    if (!isCSV) {
      message.error(
        `${file.name} is not a CSV file. Please upload a .csv file.`
      );
      return Upload.LIST_IGNORE;
    }
    return false;
  };

  const handleUploadChange = (info) => {
    let newFileList = info.fileList.slice(-1);
    newFileList = newFileList.filter((f) => f.status !== "error");
    newFileList.forEach((f) => {
      if (!f.uid) {
        f.uid = `rc-upload-${Date.now()}-${Math.random()}`;
      }
    });
    setCurrentFileList(newFileList);
    form.setFieldsValue({ csvFile: newFileList });
  };

  const uploadProps = {
    name: "file",
    accept: ".csv, text/csv, .xlsx",
    beforeUpload: beforeUploadHandler,
    onChange: handleUploadChange,
    onRemove: (file) => {
      const newFileList = currentFileList.filter(
        (item) => item.uid !== file.uid
      );
      setCurrentFileList(newFileList);
      form.setFieldsValue({ csvFile: newFileList });
      return true;
    },
    fileList: currentFileList,
    maxCount: 1,
  };

  const handleExportCSV = async (values) => {
    message.loading({ content: "Getting CSV...", key: "csv_gen" });
    await ExportCsvApi(values);
  };

  const segmentedOptions = [{ label: "Export Data", value: "export" }];
  if (role === "admin") {
    segmentedOptions.push({ label: "Import Data", value: "import" });
  }

  useEffect(() => {
    if (role !== "admin" && activeView === "import") {
      setActiveView("export");
    }
  }, [role, activeView]);

  return (
    <div className="import-export-page">
      {/* Page Header */}
      <div className="page-header">
        <div className="header-content">
          <div className="header-icon">
            <SwapOutlined />
          </div>
          <div className="header-text">
            <Title level={3} className="header-title">Import / Export</Title>
            <Text className="header-subtitle">
              Import or export inventory data as CSV files
            </Text>
          </div>
        </div>
      </div>

      {/* Main Card */}
      <Card className="import-export-card" bordered={false}>
        {/* Segmented Control */}
        <div className="segmented-container">
          <Segmented
            options={segmentedOptions}
            value={activeView}
            onChange={setActiveView}
            className="view-segmented"
          />
        </div>

        {activeView === "import" ? (
          <div className="view-container">
            <div className="view-description import-desc">
              <UploadOutlined className="desc-icon" />
              <Text className="desc-text">
                Upload a CSV file to import inventory data into the system.
              </Text>
            </div>

            <Form
              form={form}
              name="csv_import_form"
              onFinish={onImportFinish}
              layout="vertical"
              initialValues={{ csvFile: [] }}
            >
              <Form.Item
                name="csvFile"
                label="Upload CSV File"
                rules={[{ required: true, message: "Please upload a CSV file!" }]}
              >
                <Upload.Dragger {...uploadProps} className="upload-dragger">
                  <p className="ant-upload-drag-icon">
                    <UploadOutlined />
                  </p>
                  <p className="ant-upload-text">
                    Click or drag CSV file to this area to upload
                  </p>
                  <p className="ant-upload-hint">
                    Ensure the file is in .csv format.
                  </p>
                </Upload.Dragger>
              </Form.Item>

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  icon={<UploadOutlined />}
                  block
                  size="large"
                  className="submit-btn import-btn"
                >
                  Process Imported CSV
                </Button>
              </Form.Item>
            </Form>
          </div>
        ) : (
          <div className="view-container">
            <div className="view-description export-desc">
              <DownloadOutlined className="desc-icon" />
              <Text className="desc-text">
                Export inventory data as a CSV file for backup or analysis.
              </Text>
            </div>

            <Form
              form={form}
              name="csv_export_form"
              onFinish={handleExportCSV}
              layout="vertical"
              initialValues={{ csvFile: [] }}
            >
              {role === "admin" && (
                <Form.Item
                  label="Select Region"
                  name="regionName"
                  rules={[{ required: true, message: "Please select a region!" }]}
                >
                  <Select
                    allowClear
                    showSearch
                    size="large"
                    placeholder="Select Region"
                    optionFilterProp="children"
                    filterOption={(input, option) =>
                      (option?.children ?? "")
                        .toLowerCase()
                        .includes(input.toLowerCase())
                    }
                    filterSort={(optionA, optionB) =>
                      (optionA?.children ?? "")
                        .toLowerCase()
                        .localeCompare((optionB?.children ?? "").toLowerCase())
                    }
                  >
                    {regions?.map((region) => (
                      <Select.Option key={region} value={region}>
                        {region}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
              )}

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  icon={<DownloadOutlined />}
                  block
                  size="large"
                  className="submit-btn export-btn"
                >
                  Download Data as CSV
                </Button>
              </Form.Item>
            </Form>
          </div>
        )}
      </Card>
    </div>
  );
}

export default ImportExport;
