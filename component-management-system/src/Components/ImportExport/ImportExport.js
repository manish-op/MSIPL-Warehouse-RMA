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
import GetRegionAPI from "../API/Region/GetRegion/GetRegionAPI";
import { UploadOutlined, DownloadOutlined } from "@ant-design/icons";
import ExportCsvApi from "../API/ImportExport/ExportCsvApi";
import ImportCsvApi from "../API/ImportExport/ImportCsvApi";

const { Title } = Typography;

function ImportExport() {
  const [form] = Form.useForm();
  const [regions, setRegions] = useState();
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const [currentFileList, setCurrentFileList] = useState([]);
  // State to control which view is active: 'import' or 'export'
  const [activeView, setActiveView] = useState("export"); // Default to export view

  //Api is calling for getting region from Database
  useEffect(() => {
    const fetchRegions = async () => {
      try {
        const data = await GetRegionAPI(); // Use await to get the data
        if (data) {
          setRegions(data);
        } else {
          
        }
      } catch (error) {
        //console.error('API Error:', error);
      }
    };

    fetchRegions(); // Call the async function
  }, []);



  // --- Import CSV Logic ---
  const onImportFinish = async (values) => {
    if (values.csvFile && values.csvFile.length > 0) {
      const fileObjectFromAnt = values.csvFile[0];
      const file = fileObjectFromAnt.originFileObj;


      const formData = new FormData();
      formData.append('file', file, file.name);


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
    return false; // Prevent Ant Design from automatically uploading
  };

  const handleUploadChange = (info) => {
    let newFileList = info.fileList.slice(-1); // Keep only the last selected file (maxCount: 1)

    newFileList = newFileList.filter((f) => {
      // Filter out files that might have an error status
      if (f.status === "error") {
        return false;
      }
      return true;
    });

    // Ensure UID is present for controlled component behavior
    newFileList.forEach((f) => {
      if (!f.uid) {
        f.uid = `rc-upload-${Date.now()}-${Math.random()}`;
      }
    });

    setCurrentFileList(newFileList);
    form.setFieldsValue({ csvFile: newFileList }); // Update the Form's value
  };

  const uploadProps = {
    name: "file",
    accept: ".csv, text/csv ,.xlsx",
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

  // Dynamically build Segmented options based on role
  const segmentedOptions = [
    { label: 'Export Data', value: 'export' },
  ];

  if (role === 'admin') {
    segmentedOptions.push({ label: 'Import Data', value: 'import' });
  }

  // Effect to handle view change if 'import' is no longer available
  useEffect(() => {
    if (role !== 'admin' && activeView === 'import') {
      setActiveView('export'); // Switch to export view if import is not allowed for current role
    }
  }, [role, activeView]);

  return (
    <div className="p-4 sm:p-8 md:p-12 bg-gray-100 min-h-screen flex items-center justify-center font-sans">
      <Card
        className="w-full max-w-4xl rounded-xl shadow-lg"
        title={
          <Title level={3} className="text-center mb-4 text-gray-800">
            CSV File
          </Title>
        }
      >
        <div className="flex justify-center mb-6">
          <Segmented
            options={segmentedOptions}
            value={activeView}
            onChange={setActiveView}
            className="rounded-lg bg-blue-50 shadow-sm"
          />
        </div>

        {activeView === "import" ? (
          <div className="animate-fade-in">
            <Title level={4} className="text-center text-blue-600 mb-6">
              Import CSV File
            </Title>

            <Form
              form={form}
              name="csv_import_form"
              onFinish={onImportFinish}
              layout="vertical"
              initialValues={{ csvFile: [] }}
              className="max-w-md mx-auto"
            >

              <Form.Item
                name="csvFile"
                label="Upload CSV"
                rules={[
                  { required: true, message: "Please upload a CSV file!" },
                ]}
              >
                <Upload.Dragger {...uploadProps} className="upload-zone">
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
                  className="w-full py-2 px-4 rounded-lg bg-blue-500 hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 transition-all duration-200"
                >
                  Process Imported CSV
                </Button>
              </Form.Item>
            </Form>
          </div>
        ) : (
          <div className="animate-fade-in">
            <Title level={4} className="text-center text-green-600 mb-6">
              Export Data to CSV
            </Title>

            <div className="text-center">
              <Form
                form={form}
                name="csv_export_form"
                onFinish={handleExportCSV}
                layout="vertical"
                initialValues={{ csvFile: [] }}
                className="max-w-md mx-auto"
              >
                {role === "admin" && (
                  <Form.Item
                    label="Region"
                    name="regionName"
                    rules={[
                      { required: true, message: "Please select a region!" },
                    ]}
                  >
                    {regions ? ( // Check if regions is defined
                      <Select
                        style={{ marginLeft: "2px", minWidth: "30px" }}
                        allowClear
                        showSearch
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
                        {regions.map((region) => (
                          <Select.Option key={region} value={region}>
                            {region}
                          </Select.Option>
                        ))}
                      </Select>
                    ) : (
                      <Select
                        style={{ minWidth: "150px" }}
                        value={""}
                      ></Select> // Or a loading indicator
                    )}
                  </Form.Item>
                )}

                <Form.Item>
                  <Button
                    type="primary"
                    htmlType="submit"
                    icon={<DownloadOutlined />}
                    className="py-2 px-6 rounded-lg bg-green-500 hover:bg-green-600 focus:outline-none focus:ring-2 
                    focus:ring-green-500 focus:ring-opacity-50 transition-all duration-200"
                  >
                    Download Data as CSV
                  </Button>
                </Form.Item>
              </Form>


            </div>
          </div>
        )}
      </Card>

    </div>
  );


}

export default ImportExport;
