import React, { useState, useEffect } from 'react';
import { Table, Card, Button, notification, Input, Space } from 'antd';
import { SearchOutlined, PlusOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import Highlighter from 'react-highlight-words';
import { CustomerSlaApi } from '../API/CustomerSla/CustomerSlaApi';

const ManageCustomerSLA = () => {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const [searchText, setSearchText] = useState('');
    const [searchedColumn, setSearchedColumn] = useState('');

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        setLoading(true);
        try {
            const response = await CustomerSlaApi.getAllCustomerSla();
            if (response) {
                setData(response);
            }
        } catch (error) {
            console.error(error);
            notification.error({
                message: 'Error',
                description: 'Failed to fetch Customer SLA data',
            });
        } finally {
            setLoading(false);
        }
    };

    const getColumnSearchProps = (dataIndex) => ({
        filterDropdown: ({ setSelectedKeys, selectedKeys, confirm, clearFilters }) => (
            <div style={{ padding: 8 }}>
                <Input
                    placeholder={`Search ${dataIndex}`}
                    value={selectedKeys[0]}
                    onChange={e => setSelectedKeys(e.target.value ? [e.target.value] : [])}
                    onPressEnter={() => handleSearch(selectedKeys, confirm, dataIndex)}
                    style={{ width: 188, marginBottom: 8, display: 'block' }}
                />
                <Space>
                    <Button
                        type="primary"
                        onClick={() => handleSearch(selectedKeys, confirm, dataIndex)}
                        icon={<SearchOutlined />}
                        size="small"
                        style={{ width: 90 }}
                    >
                        Search
                    </Button>
                    <Button onClick={() => handleReset(clearFilters)} size="small" style={{ width: 90 }}>
                        Reset
                    </Button>
                </Space>
            </div>
        ),
        filterIcon: filtered => <SearchOutlined style={{ color: filtered ? '#1890ff' : undefined }} />,
        onFilter: (value, record) =>
            record[dataIndex]
                ? record[dataIndex].toString().toLowerCase().includes(value.toLowerCase())
                : '',
        render: text =>
            searchedColumn === dataIndex ? (
                <Highlighter
                    highlightStyle={{ backgroundColor: '#ffc069', padding: 0 }}
                    searchWords={[searchText]}
                    autoEscape
                    textToHighlight={text ? text.toString() : ''}
                />
            ) : (
                text
            ),
    });

    const handleSearch = (selectedKeys, confirm, dataIndex) => {
        confirm();
        setSearchText(selectedKeys[0]);
        setSearchedColumn(dataIndex);
    };

    const handleReset = (clearFilters) => {
        clearFilters();
        setSearchText('');
    };

    const columns = [
        {
            title: 'System ID',
            dataIndex: 'systemId',
            key: 'systemId',
            ...getColumnSearchProps('systemId'),
        },
        {
            title: 'Project Name',
            dataIndex: 'projectName',
            key: 'projectName',
            ...getColumnSearchProps('projectName'),
        },
        {
            title: 'Region',
            dataIndex: 'region',
            key: 'region',
            filters: [
                { text: 'North', value: 'North' },
                { text: 'South', value: 'South' },
                { text: 'East', value: 'East' },
                { text: 'West', value: 'West' },
            ],
            onFilter: (value, record) => record.region.indexOf(value) === 0,
        },
        {
            title: 'Customer Number',
            dataIndex: 'customerNumber',
            key: 'customerNumber',
        },
        {
            title: 'AMC Status',
            dataIndex: 'amcStatus',
            key: 'amcStatus',
        },
        {
            title: 'Contract Start',
            dataIndex: 'contractStartDate',
            key: 'contractStartDate',
        },
        {
            title: 'Contract End',
            dataIndex: 'contractEndDate',
            key: 'contractEndDate',
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card
                title="Manage Customer SLA"
                extra={
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/customer-sla/add')}>
                        Add New
                    </Button>
                }
                bordered={false}
            >
                <Table
                    columns={columns}
                    dataSource={data}
                    rowKey="id"
                    loading={loading}
                    pagination={{ pageSize: 10 }}
                />
            </Card>
        </div>
    );
};

export default ManageCustomerSLA;
