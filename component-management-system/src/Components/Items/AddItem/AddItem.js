import React from "react";
import "./AddItem.css";
import {
  Form,
  Input,
  Select,
  Button,
  Row,
  Col,
  Card,
  AutoComplete,
  Checkbox,
  message,
  Typography,
  Space,
  Tooltip,
  Dropdown,
  Menu,
  Grid,
} from "antd";
import { useState, useEffect } from "react";
import GetRegionAPI from "../../API/Region/GetRegion/GetRegionAPI";
import GetKeywordAPI from "../../API/Keyword/GetKeyword/GetKeywordAPI";
import GetSubKeywordAPI from "../../API/Keyword/SubKeyword/GetSubKeyword/GetSubKeywordAPI";
import GetItemAvailabilityStatusOptionAPI from "../../API/StatusOptions/ItemAvailabilityStatusOption/GetItemAvailabilityStatusOptionAPI";
import GetItemStatusOptionAPI from "../../API/StatusOptions/ItemStatusOption/GetItemStatusOptionAPI";
import AddItemAPI from "../../API/ItemRelatedApi/AddItem/AddItemAPI";

const { Text } = Typography;
const { useBreakpoint } = Grid;

const DRAFT_KEY = "additem:draft";
const LAST_KEY = "additem:last";
const TEMPLATES_KEY = "additem:templates";

function AddItem() {
  const screens = useBreakpoint();
  const isXs = !screens.sm; // treat anything < sm as "small"
  const [form] = Form.useForm();
  const role = localStorage.getItem("_User_role_for_MSIPL");
  const [regions, setRegions] = useState();
  const [keywords, setKeywords] = useState();
  const [subkeywords, setSubKeywords] = useState();
  const [itemAvailabilityOption, setItemAvailabilityOption] = useState();
  const [itemStatusOption, setItemStatusOption] = useState();

  // serial UI state
  const [autoGenerateSerial, setAutoGenerateSerial] = useState(true);
  const [createdSerial, setCreatedSerial] = useState(null);
  const [serverMessage, setServerMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  // templates state (simple map name->values)
  const [templates, setTemplates] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem(TEMPLATES_KEY) || "{}");
    } catch {
      return {};
    }
  });
  const [selectedTemplateKey, setSelectedTemplateKey] = useState(null);

  // --- Defined the options for the new dropdown ---
  const systemNameOptions = ["ASTRO", "TETRA", "ASTRO/TETRA"];
  const moduleForOptions = ["CONSOLE", "RADIO", "SERVER", "RCW"];

  //  Format options for AutoComplete (it prefers an array of { value: 'text' } objects)
  const systemOptionsForAutoComplete = systemNameOptions.map((opt) => ({
    value: opt,
  }));
  const moduleOptionsForAutoComplete = moduleForOptions.map((opt) => ({
    value: opt,
  }));

  // API calls for fetching options
  useEffect(() => {
    const fetchAllData = async () => {
      try {
        const [
          regionsData,
          keywordsData,
          availabilityData,
          statusData,
        ] = await Promise.all([
          GetRegionAPI(),
          GetKeywordAPI(),
          GetItemAvailabilityStatusOptionAPI(),
          GetItemStatusOptionAPI(),
        ]);

        if (regionsData) setRegions(regionsData);
        if (keywordsData) setKeywords(keywordsData);
        if (availabilityData) setItemAvailabilityOption(availabilityData);
        if (statusData) setItemStatusOption(statusData);
      } catch (error) {
        console.error("API Fetch Error:", error);
      }
    };
    fetchAllData();
  }, []);

  // handle keyword selection and fetch subkeywords
  const handleKeywordChange = (selectedKeyword) => {
    form.setFieldsValue({ subKeyword: null });
    setSubKeywords(null);
    if (selectedKeyword) {
      GetSubKeywordAPI(selectedKeyword)
        .then((data) => {
          if (data && data.subKeywordList) {
            const subKeywordValues = data.subKeywordList.map(
              (item) => item.subKeyword
            );
            setSubKeywords(subKeywordValues);
          } else {
            setSubKeywords(null);
          }
        })
        .catch(() => setSubKeywords(null));
    }
  };

  // helper to extract serial from text like "Item added successfully. Serial: abc-0001"
  const extractSerialFromText = (txt) => {
    if (!txt) return null;
    const m = txt.match(/Serial:\s*([^\s,]+)/i);
    return m ? m[1] : null;
  };

  // Build payload helper (keeps keys consistent)
  const buildPayload = (values) => ({
    serialNo: values.serialNo ? values.serialNo.toString().trim() : "",
    keyword: values.keyword ? values.keyword : "",
    rackNo: values.rackNo ? values.rackNo : "",
    availableStatus: values.availableStatus ? values.availableStatus : "",
    itemStatus: values.itemStatus ? values.itemStatus : "",
    region: values.region ? values.region : "",
    boxNo: values.boxNo ? values.boxNo : "",
    partNo: values.partNo ? values.partNo : "",
    modelNo: values.modelNo ? values.modelNo : "",
    spareLocation: values.spareLocation ? values.spareLocation : "",
    system: values.system ? values.system : "",
    moduleFor: values.moduleFor ? values.moduleFor : "",
    systemVersion: values.systemVersion ? values.systemVersion : "",
    subKeyword: values.subKeyword ? values.subKeyword : "",
    remark: values.remark ? values.remark : "",
    partyName: values.partyName ? values.partyName : "",
    itemDescription: values.itemDescription ? values.itemDescription : "",
    autoGenerateSerial: autoGenerateSerial,
  });

  // Load draft on mount (if exists)
  useEffect(() => {
    try {
      const savedDraft = JSON.parse(localStorage.getItem(DRAFT_KEY) || "null");
      if (savedDraft) {
        form.setFieldsValue(savedDraft);
      }
    } catch (e) {
      // ignore
    }
  }, [form]);

  // keep templates in sync if changed elsewhere (optional)
  useEffect(() => {
    const onStorage = (e) => {
      if (e.key === TEMPLATES_KEY) {
        try {
          setTemplates(JSON.parse(e.newValue || "{}"));
        } catch {
          setTemplates({});
        }
      }
    };
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  // on values change: autosave draft
  const onValuesChange = (changed, all) => {
    try {
      localStorage.setItem(DRAFT_KEY, JSON.stringify(all));
    } catch (e) {
      // ignore quota errors
    }
  };

  // ACTION HANDLERS (toolbar)
  const applyLast = () => {
    try {
      const last = JSON.parse(localStorage.getItem(LAST_KEY) || "null");
      if (!last) {
        message.info("No last values saved yet");
        return;
      }
      form.setFieldsValue(last);
      message.success("Applied last values");
    } catch {
      message.error("Failed to apply last saved values");
    }
  };

  const saveTemplate = () => {
    const values = form.getFieldsValue();
    const key = window.prompt("Template name (short):");
    if (!key || !key.trim()) {
      message.info("Template name required");
      return;
    }
    const name = key.trim();
    const next = { ...(templates || {}) };
    next[name] = values;
    setTemplates(next);
    try {
      localStorage.setItem(TEMPLATES_KEY, JSON.stringify(next));
      setSelectedTemplateKey(name);
      message.success("Template saved: " + name);
    } catch {
      message.error("Failed to save template");
    }
  };

  const clearDraft = () => {
    if (!window.confirm("Clear current draft?")) return;
    try {
      localStorage.removeItem(DRAFT_KEY);
      form.resetFields();
      message.success("Draft cleared");
    } catch {
      message.error("Failed to clear draft");
    }
  };

  const applyTemplate = (key) => {
    if (!key) return;
    const tpl = templates[key];
    if (!tpl) {
      message.error("Template not found");
      return;
    }
    form.setFieldsValue(tpl);
    message.success("Template applied: " + key);
    setSelectedTemplateKey(key);
  };

  const deleteTemplate = (key) => {
    if (!key) return;
    if (!window.confirm("Delete template '" + key + "'?")) return;
    const nt = { ...templates };
    delete nt[key];
    setTemplates(nt);
    try {
      localStorage.setItem(TEMPLATES_KEY, JSON.stringify(nt));
      setSelectedTemplateKey(null);
      message.success("Template deleted");
    } catch {
      message.error("Failed to delete template");
    }
  };

  // Menu click handler (for dropdown)
  const onMenuClick = ({ key }) => {
    if (key === "applyLast") applyLast();
    if (key === "saveTemplate") saveTemplate();
    if (key === "clearDraft") clearDraft();
  };

  // Responsive layout config
  const formLayout = isXs ? "vertical" : "horizontal";
  const labelCol = isXs ? { span: 24 } : { span: 8 };
  const wrapperCol = isXs ? { span: 24 } : { span: 16 };

  const onFinish = async (values) => {
    setLoading(true);
    setCreatedSerial(null);
    setServerMessage(null);

    const payload = buildPayload(values);

    try {
      // prefer your API wrapper if available
      if (typeof AddItemAPI === "function") {
        const res = await AddItemAPI(payload);
        // support Response-like object or parsed JSON/string
        if (res && res.ok !== undefined) {
          if (res.ok) {
            try {
              const data = await res.json();
              const serial =
                data?.serial || data?.serialNo || data?.createdSerial || null;
              const msg = data?.message || "Item added successfully";
              setServerMessage(msg);
              message.success(msg);
              if (serial) setCreatedSerial(serial);
            } catch {
              const txt = await res.text();
              const serial = extractSerialFromText(txt);
              setServerMessage(txt);
              message.success(txt);
              if (serial) setCreatedSerial(serial);
            }
          } else {
            const err = await res.text();
            message.error(err || "Failed to add item");
          }
        } else if (typeof res === "object") {
          const serial = res?.serial || res?.serialNo || res?.createdSerial || null;
          const msg = res?.message || "Item added successfully";
          setServerMessage(msg);
          message.success(msg);
          if (serial) setCreatedSerial(serial);
        } else if (typeof res === "string") {
          const serial = extractSerialFromText(res);
          setServerMessage(res);
          message.success(res);
          if (serial) setCreatedSerial(serial);
        } else {
          message.success("Item added successfully");
        }
      } else {
        const r = await fetch("/api/item/addComponent", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });
        if (r.ok) {
          try {
            const data = await r.json();
            const serial = data?.serial || data?.serialNo || data?.createdSerial || null;
            const msg = data?.message || "Item added successfully";
            setServerMessage(msg);
            message.success(msg);
            if (serial) setCreatedSerial(serial);
          } catch {
            const txt = await r.text();
            const serial = extractSerialFromText(txt);
            setServerMessage(txt);
            message.success(txt);
            if (serial) setCreatedSerial(serial);
          }
        } else {
          const txt = await r.text();
          message.error(txt || "Failed to add item");
        }
      }

      // Save last successful payload & clear draft
      try {
        localStorage.setItem(LAST_KEY, JSON.stringify(payload));
        localStorage.removeItem(DRAFT_KEY);
      } catch {}
      form.resetFields();
    } catch (err) {
      console.error("Add item error:", err);
      message.error("Network error: " + (err?.message || err));
    } finally {
      setLoading(false);
    }
  };

  // toolbar Menu for small screens & dropdown
  const toolbarMenu = (
    <Menu onClick={onMenuClick}>
      <Menu.Item key="applyLast">Apply Last</Menu.Item>
      <Menu.Item key="saveTemplate">Save Template</Menu.Item>
      <Menu.Item key="clearDraft">Clear Draft</Menu.Item>
    </Menu>
  );

  return (
    <>
      <title>Add New Item</title>
      <Card
        title={
          <div className="toolbar-wrap" style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
            <div style={{ display: "flex", gap: 8, alignItems: "center", flexWrap: "wrap" }}>
              <Text strong>Add New Item</Text>

              {!isXs ? (
                <>
                  <Tooltip title="Apply values from last successful add">
                    <Button size="small" onClick={applyLast}>Apply Last</Button>
                  </Tooltip>

                  {/* Templates select */}
                  <Select
                    size="small"
                    placeholder="Templates"
                    value={selectedTemplateKey}
                    onChange={(val) => {
                      setSelectedTemplateKey(val);
                      applyTemplate(val);
                    }}
                    style={{ width: 180, marginLeft: 8 }}
                    allowClear
                  >
                    {Object.keys(templates).map((k) => (
                      <Select.Option key={k} value={k}>
                        {k}
                      </Select.Option>
                    ))}
                  </Select>

                  <Button
                    size="small"
                    onClick={() => {
                      if (selectedTemplateKey) deleteTemplate(selectedTemplateKey);
                      else message.info("Select a template to delete");
                    }}
                    style={{ marginLeft: 6 }}
                    danger
                  >
                    Delete Template
                  </Button>

                  <Tooltip title="Save current form as a named template">
                    <Button size="small" onClick={saveTemplate}>Save Template</Button>
                  </Tooltip>

                  <Tooltip title="Clear draft">
                    <Button size="small" onClick={clearDraft}>Clear Draft</Button>
                  </Tooltip>
                </>
              ) : (
                <Dropdown overlay={toolbarMenu} trigger={['click']}>
                  <Button size="small">Actions ▾</Button>
                </Dropdown>
              )}
            </div>

            <div style={{ marginLeft: "auto" }}>
              {!isXs && (
                <Text type="secondary" style={{ fontSize: 12 }}>
                  Tip: Press Ctrl + Enter to submit
                </Text>
              )}
            </div>
          </div>
        }
      >
        <Form
          form={form}
          name="spare-part-form"
          onFinish={onFinish}
          onValuesChange={onValuesChange}
          layout={formLayout}
          labelCol={labelCol}
          wrapperCol={wrapperCol}
          initialValues={{}}
        >
          <Row gutter={[16, 8]}>
            <Col xs={24} sm={12} md={12} lg={12}>
              <Form.Item label="Serial No." name="serialNo">
                <Input placeholder="Leave empty to auto-generate" />
              </Form.Item>

              <Form.Item>
                <Checkbox
                  checked={autoGenerateSerial}
                  onChange={(e) => setAutoGenerateSerial(e.target.checked)}
                >
                  Auto-generate serial if left empty
                </Checkbox>
              </Form.Item>

              <Form.Item label="Part No." name="partNo">
                <Input />
              </Form.Item>

              <Form.Item
                label="Rack No."
                name="rackNo"
                rules={[
                  { required: true, message: "Please enter the rack number!" },
                ]}
              >
                <Input />
              </Form.Item>

              <Form.Item label="System Name" name="system">
                <AutoComplete
                  options={systemOptionsForAutoComplete}
                  placeholder="Select or type a system"
                  allowClear
                />
              </Form.Item>

              <Form.Item label="Module Version" name="systemVersion">
                <Input />
              </Form.Item>

              <Form.Item
                label="Availability"
                name="availableStatus"
                rules={[
                  { required: true, message: "Please select Availability Status!" },
                ]}
              > 
                <Select showSearch allowClear placeholder="Select Availability">
                  {itemAvailabilityOption?.map((itemAvailStatus) => (
                    <Select.Option key={itemAvailStatus} value={itemAvailStatus}>
                      {itemAvailStatus}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>

              {role === "admin" && (
                <Form.Item
                  label="Region"
                  name="region"
                  rules={[{ required: true, message: "Please select a region!" }]}
                >
                  <Select showSearch allowClear placeholder="Select a region">
                    {regions?.map((region) => (
                      <Select.Option key={region} value={region}>
                        {region}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
              )}

              <Form.Item label="Remark" name="remark">
                <Input.TextArea />
              </Form.Item>
            </Col>

            <Col xs={24} sm={12} md={12} lg={12}>
              <Form.Item label="Box No." name="boxNo">
                <Input />
              </Form.Item>
              <Form.Item label="Model No." name="modelNo">
                <Input />
              </Form.Item>
              <Form.Item label="Spare Location" name="spareLocation">
                <Input />
              </Form.Item>

              <Form.Item label="Module For" name="moduleFor">
                <AutoComplete options={moduleOptionsForAutoComplete} placeholder="Select or type a module" allowClear />
              </Form.Item>

              <Form.Item
                label="Keyword"
                name="keyword"
                rules={[{ required: true, message: "Please select a keyword!" }]}
              >
                <Select
                  onChange={handleKeywordChange}
                  showSearch
                  allowClear
                  placeholder="Select a keyword"
                  optionFilterProp="children"
                >
                  {keywords?.map((keyword) => (
                    <Select.Option key={keyword} value={keyword}>
                      {keyword}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>

              {subkeywords && (
                <Form.Item
                  label="Sub Keyword"
                  name="subKeyword"
                  style={{ marginTop: -12 }}
                >
                  <Select
                    allowClear
                    showSearch
                    placeholder="Select subkeyword"
                    optionFilterProp="children"
                  >
                    {subkeywords.map((subkeyword) => (
                      <Select.Option key={subkeyword} value={subkeyword}>
                        {subkeyword}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
              )}

              <Form.Item
                label="Item Status"
                name="itemStatus"
                rules={[{ required: true, message: "Please select the item status!" }]}
              >
                <Select showSearch allowClear placeholder="Item status">
                  {itemStatusOption?.map((itemStatus) => (
                    <Select.Option key={itemStatus} value={itemStatus}>
                      {itemStatus}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item label="Party Name" name="partyName">
                <Input />
              </Form.Item>

              <Form.Item label="Item Description" name="itemDescription">
                <Input.TextArea />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item wrapperCol={isXs ? { span: 24 } : { offset: 0 }}>
            <Button type="primary" htmlType="submit" loading={loading} style={{ minWidth: 120 }}>
              Submit
            </Button>
          </Form.Item>

          {createdSerial && (
            <Form.Item>
              <Space>
                <div>
                  <Text strong>Created Serial:</Text> <Text code>{createdSerial}</Text>
                </div>
                <Button
                  size="small"
                  onClick={() => {
                    navigator.clipboard.writeText(createdSerial)
                      .then(() => message.success("Copied to clipboard"))
                      .catch(() => message.error("Copy failed"));
                  }}
                >
                  Copy
                </Button>
              </Space>
            </Form.Item>
          )}

          {serverMessage && (
            <Form.Item>
              <Text type="secondary">{serverMessage}</Text>
            </Form.Item>
          )}
        </Form>
      </Card>
    </>
  );
}

export default AddItem;
