import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Typography, Button, Spin, Breadcrumb, Empty, Statistic, Tag, Avatar, Badge } from 'antd';
import { 
    SafetyCertificateOutlined, 
    RocketOutlined, 
    BankOutlined, 
    ExperimentOutlined, 
    ApartmentOutlined, 
    AppstoreOutlined,
    GlobalOutlined,
    ArrowLeftOutlined,
    PlusOutlined,
    UnorderedListOutlined,
    EnvironmentOutlined,
    CalendarOutlined,
    IdcardOutlined,
    RightOutlined,
    UserOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { CustomerSlaApi } from '../API/CustomerSla/CustomerSlaApi';
import RmaLayout from '../RMA/RmaLayout';

const { Title, Text, Paragraph } = Typography;

const CustomerSlaDashboard = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [allData, setAllData] = useState([]);
    
    // Navigation State
    const [view, setView] = useState('categories'); // 'categories' | 'regions' | 'customers'
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [selectedRegion, setSelectedRegion] = useState(null);

    // Categories Configuration with refined colors and gradients
    const categories = [
        { id: 'Airport', title: 'Airport Customers', icon: <RocketOutlined />, color: '#1890ff', gradient: 'linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%)' },
        { id: 'Metro', title: 'Metro Customers', icon: <BankOutlined />, color: '#722ed1', gradient: 'linear-gradient(135deg, #f9f0ff 0%, #d3adf7 100%)' },
        { id: 'Refinery', title: 'Refinery Customers', icon: <ExperimentOutlined />, color: '#fa8c16', gradient: 'linear-gradient(135deg, #fff7e6 0%, #ffd591 100%)' },
        { id: 'Police', title: 'Police Customers', icon: <SafetyCertificateOutlined />, color: '#eb2f96', gradient: 'linear-gradient(135deg, #fff0f6 0%, #ffadd2 100%)' },
        { id: 'Others', title: 'Other Sectors', icon: <AppstoreOutlined />, color: '#52c41a', gradient: 'linear-gradient(135deg, #f6ffed 0%, #b7eb8f 100%)' },
    ];

    const regions = ['North', 'South', 'East', 'West', 'Center'];

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        setLoading(true);
        try {
            const data = await CustomerSlaApi.getAllCustomerSla();
            if (data && Array.isArray(data)) {
                setAllData(data);
            }
        } catch (error) {
            console.error("Failed to fetch SLA data", error);
        } finally {
            setLoading(false);
        }
    };

    // Helper: Normalize category string for matching
    const normalizeCategory = (cat) => (cat ? cat.toLowerCase().trim() : 'others');
    const normalizeRegion = (reg) => (reg ? reg.toLowerCase().trim() : 'others');

    // Filter Data Logic
    const getCategoryCount = (catId) => {
        if (catId === 'Others') {
            return allData.filter(item => {
                const c = normalizeCategory(item.category);
                return !['airport', 'metro', 'refinery', 'police'].some(k => c.includes(k));
            }).length;
        }
        return allData.filter(item => normalizeCategory(item.category).includes(catId.toLowerCase())).length;
    };

    const getRegionCount = (regionName) => {
        const categoryData = getFilteredCategoryData();
        return categoryData.filter(item => normalizeRegion(item.region) === regionName.toLowerCase()).length;
    };

    const getFilteredCategoryData = () => {
        if (!selectedCategory) return allData;
        if (selectedCategory.id === 'Others') {
            return allData.filter(item => {
                const c = normalizeCategory(item.category);
                return !['airport', 'metro', 'refinery', 'police'].some(k => c.includes(k));
            });
        }
        return allData.filter(item => normalizeCategory(item.category).includes(selectedCategory.id.toLowerCase()));
    };

    const getFinalCustomerList = () => {
        const catData = getFilteredCategoryData();
        return catData.filter(item => normalizeRegion(item.region) === selectedRegion.toLowerCase());
    };

    // Handlers
    const handleCategoryClick = (cat) => {
        setSelectedCategory(cat);
        setView('regions');
    };

    const handleRegionClick = (reg) => {
        setSelectedRegion(reg);
        setView('customers');
    };

    const handleBack = () => {
        if (view === 'customers') {
            setView('regions');
            setSelectedRegion(null);
        } else if (view === 'regions') {
            setView('categories');
            setSelectedCategory(null);
        }
    };

    // Components
    const renderCategories = () => (
        <Row gutter={[32, 32]}>
            <Col span={24}>
                <Title level={3} style={{ fontWeight: 600, color: '#262626' }}>Customer Sectors</Title>
                <Paragraph type="secondary" style={{ fontSize: '16px' }}>Select a sector to explore regional agreements</Paragraph>
            </Col>
            {categories.map((cat) => (
                <Col xs={24} sm={12} md={8} lg={6} key={cat.id}>
                    <Card
                        hoverable
                        className="category-card"
                        onClick={() => handleCategoryClick(cat)}
                    >
                        <div className="category-icon" style={{ background: cat.gradient, color: cat.color }}>
                            {cat.icon}
                        </div>
                        <Title level={4} style={{ marginBottom: '8px', marginTop: '16px' }}>{cat.title}</Title>
                        <Tag color={cat.color} style={{ fontSize: '14px', padding: '4px 12px', borderRadius: '12px' }}>
                            {getCategoryCount(cat.id)} Projects
                        </Tag>
                        <div className="card-shine"></div>
                    </Card>
                </Col>
            ))}
        </Row>
    );

    const renderRegions = () => (
        <div className="animate-fade-in">
            <div style={{ marginBottom: '32px', textAlign: 'center' }}>
                <Title level={2} style={{ marginBottom: '8px' }}>{selectedCategory?.title}</Title>
                <Text type="secondary" style={{ fontSize: '16px' }}>Select a region to view specific customer portfolios</Text>
            </div>
            <Row gutter={[24, 24]} justify="center">
                {regions.map((reg) => (
                    <Col xs={24} sm={12} md={8} lg={6} key={reg}>
                        <Card
                            hoverable
                            onClick={() => handleRegionClick(reg)}
                            className="region-card"
                        >
                            <div className="region-content">
                                <div className="region-header">
                                    <EnvironmentOutlined style={{ fontSize: '24px', color: selectedCategory?.color }} />
                                    <Title level={3} style={{ margin: 0 }}>{reg}</Title>
                                </div>
                                <div className="region-stat">
                                    <Statistic 
                                        value={getRegionCount(reg)} 
                                        suffix="Clients"
                                        valueStyle={{ color: '#262626', fontWeight: 600 }}
                                    />
                                </div>
                            </div>
                            <div className="region-hover-indicator" style={{ background: selectedCategory?.color }}></div>
                        </Card>
                    </Col>
                ))}
            </Row>
        </div>
    );

    const renderCustomers = () => {
        const customers = getFinalCustomerList();
        return (
            <div className="animate-fade-in">
                <div style={{ marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#fff', padding: '16px 24px', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.04)' }}>
                    <div>
                        <Title level={3} style={{ margin: 0 }}>
                             {selectedCategory?.title} <RightOutlined style={{ fontSize: '16px', color: '#bfbfbf', margin: '0 8px' }} /> {selectedRegion}
                        </Title>
                        <Text type="secondary">{customers.length} customer agreements found</Text>
                    </div>
                </div>

                {customers.length === 0 ? (
                    <Empty 
                        image={Empty.PRESENTED_IMAGE_SIMPLE} 
                        description={<span style={{ fontSize: '16px', color: '#8c8c8c' }}>No customers found in this region</span>}
                        style={{ margin: '48px 0' }}
                    />
                ) : (
                    <Row gutter={[24, 24]}>
                        {customers.map((item, idx) => (
                            <Col xs={24} sm={12} md={12} lg={8} xl={8} key={item.id || idx}>
                                <Card
                                    hoverable
                                    className="customer-card"
                                    actions={[
                                        <Button type="link" onClick={() => navigate('/customer-sla/manage')}>View Full Details</Button>,
                                        <Badge status={item.amcStatus === 'Active' ? 'success' : 'warning'} text={item.amcStatus || 'Unknown'} />
                                    ]}
                                >
                                    <Card.Meta
                                        avatar={<Avatar style={{ backgroundColor: selectedCategory?.color }} icon={<UserOutlined />} size="large" />}
                                        title={<span style={{ fontSize: '18px', fontWeight: 600 }}>{item.projectName || "Unnamed Project"}</span>}
                                        description={
                                            <div style={{ marginTop: '12px' }}>
                                                <div className="info-row">
                                                    <IdcardOutlined /> <Text>{item.partnerName}</Text>
                                                </div>
                                                <div className="info-row">
                                                    <SafetyCertificateOutlined /> <Text code>{item.systemId}</Text>
                                                </div>
                                                <div className="info-row">
                                                    <CalendarOutlined /> <Text type="secondary">Ends: {item.contractEndDate || 'N/A'}</Text>
                                                </div>
                                            </div>
                                        }
                                    />
                                </Card>
                            </Col>
                        ))}
                    </Row>
                )}
            </div>
        );
    };

    return (
        <RmaLayout sidebarType="customerSla">
            <div style={{ padding: '0px', minHeight: '100vh', background: '#f0f2f5' }}>
                {/* Top Banner / Header */}
                <div style={{ 
                    background: '#fff', 
                    padding: '20px 40px', 
                    boxShadow: '0 4px 12px rgba(0,0,0,0.03)',
                    // Remove sticky since the layout handles scroll usually, or keep it if content scrolls
                    // position: 'sticky', 
                    // top: 0,
                    // zIndex: 10,
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '24px' }}>
                        {view !== 'categories' && (
                            <Button 
                                icon={<ArrowLeftOutlined />} 
                                shape="circle" 
                                size="large"
                                onClick={handleBack} 
                                style={{ border: 'none', boxShadow: '0 2px 8px rgba(0,0,0,0.1)' }}
                            />
                        )}
                        <div>
                            <Breadcrumb separator=">">
                                <Breadcrumb.Item onClick={() => { setView('categories'); setSelectedCategory(null); setSelectedRegion(null); }} style={{ cursor: 'pointer', display: 'flex', alignItems: 'center' }}>
                                    <SafetyCertificateOutlined style={{ marginRight: 8 }} /> SLA Home
                                </Breadcrumb.Item>
                                {selectedCategory && (
                                    <Breadcrumb.Item onClick={() => { setView('regions'); setSelectedRegion(null); }} style={{ cursor: 'pointer' }}>
                                        {selectedCategory.title}
                                    </Breadcrumb.Item>
                                )}
                                {selectedRegion && (
                                    <Breadcrumb.Item>{selectedRegion}</Breadcrumb.Item>
                                )}
                            </Breadcrumb>
                            {view === 'categories' && <Title level={4} style={{ margin: '4px 0 0 0' }}>Overview</Title>}
                        </div>
                    </div>

                    {view === 'categories' && (
                        <div style={{ display: 'flex', gap: '12px' }}>
                            <Button icon={<UnorderedListOutlined />} size="large" onClick={() => navigate('/customer-sla/manage')}>
                                Manage All
                            </Button>
                            <Button type="primary" icon={<PlusOutlined />} size="large" onClick={() => navigate('/customer-sla/add')}>
                                Add New SLA
                            </Button>
                        </div>
                    )}
                </div>

                <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '40px' }}>
                    {loading ? (
                        <div style={{ textAlign: 'center', padding: '100px' }}><Spin size="large" tip="Loading SLA Data..." /></div>
                    ) : (
                        <>
                            {view === 'categories' && renderCategories()}
                            {view === 'regions' && renderRegions()}
                            {view === 'customers' && renderCustomers()}
                        </>
                    )}
                </div>

                <style>
                    {`
                        .category-card {
                            border-radius: 20px;
                            border: none;
                            height: 100%;
                            text-align: center;
                            position: relative;
                            overflow: hidden;
                            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                            background: #fff;
                            padding: 24px;
                        }
                        .category-card:hover {
                            transform: translateY(-8px);
                            box-shadow: 0 20px 40px rgba(0,0,0,0.08);
                        }
                        .category-icon {
                            width: 80px;
                            height: 80px;
                            border-radius: 24px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            margin: 0 auto;
                            font-size: 36px;
                        }
                        
                        .region-card {
                            border-radius: 16px;
                            border: none;
                            background: #fff;
                            overflow: hidden;
                            transition: all 0.3s ease;
                            position: relative;
                            padding: 24px;
                        }
                        .region-card:hover {
                            transform: scale(1.02);
                            box-shadow: 0 12px 24px rgba(0,0,0,0.06);
                        }
                        .region-hover-indicator {
                            position: absolute;
                            bottom: 0;
                            left: 0;
                            width: 100%;
                            height: 4px;
                            transform: scaleX(0);
                            transition: transform 0.3s ease;
                            transform-origin: left;
                        }
                        .region-card:hover .region-hover-indicator {
                            transform: scaleX(1);
                        }
                        
                        .customer-card {
                            border-radius: 16px;
                            border: none;
                            background: #fff;
                            transition: all 0.3s ease;
                            box-shadow: 0 4px 12px rgba(0,0,0,0.03);
                        }
                        .customer-card:hover {
                            box-shadow: 0 16px 32px rgba(0,0,0,0.08);
                            transform: translateY(-4px);
                        }
                        .info-row {
                            display: flex;
                            align-items: center;
                            gap: 8px;
                            margin-bottom: 6px;
                            font-size: 14px;
                            color: #595959;
                        }

                        .animate-fade-in {
                            animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1);
                        }
                        @keyframes fadeIn {
                            from { opacity: 0; transform: translateY(20px); }
                            to { opacity: 1; transform: translateY(0); }
                        }
                    `}
                </style>
            </div>
        </RmaLayout>
    );
};

export default CustomerSlaDashboard;
