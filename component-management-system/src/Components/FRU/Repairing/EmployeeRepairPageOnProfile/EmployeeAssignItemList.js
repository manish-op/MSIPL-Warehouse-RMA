import React, { useState } from 'react';
import { Table,  Typography, Tag, message } from 'antd';
import { useTicketDetails } from '../../RepairingContext/RepairingContext';
import GetSingleAssignTicketFullDetailAPI from '../../../API/FRU/EmployeeAssignItems/GetSingleAssignTicketFullDetailAPI';
import './EmployeeAssignItemList.css';

const columnsConfig = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    sorter: (a, b) => a.id - b.id,
  },
  {
    title: 'Serial No',
    dataIndex: 'serial_No',
    key: 'serial_No',
  },
  {
    title: 'RMA No.',
    dataIndex: 'rmaNo',
    key: 'rmaNo',
  },
  {
    title: 'Repair Status',
    dataIndex: 'repairStatus',
    key: 'repairStatus',
    render: (status) => {
        let color = 'geekblue';
        if (status?.toLowerCase().includes('complete')) {
            color = 'green';
        } else if (status?.toLowerCase().includes('pending')) {
            color = 'volcano';
        }
        return <Tag color={color}>{status?.toUpperCase()}</Tag>;
    }
  },
  {
    title: 'Generated Date',
    dataIndex: 'generatedDate',
    key: 'generatedDate',
    sorter: (a, b) => new Date(a.generatedDate) - new Date(b.generatedDate),
  },
  {
    title: 'Action',
    key: 'action',
   
    render: (_, record) => (
      <Typography.Link>
        View Details
      </Typography.Link>
    ),
  },
];



function EmployeeAssignItemList({ assignTicketDetailList, isLoading }) {
  const { setTicketUpdateDetailsForEmployee } = useTicketDetails();
  const [selectedRowKey, setSelectedRowKey] = useState(null);

  const handleTicketDetailsClick = async (id) => {
    
    setSelectedRowKey(id);

    
    try {
        await GetSingleAssignTicketFullDetailAPI(id, setTicketUpdateDetailsForEmployee);
    } catch (error) {
        message.error("Failed to fetch ticket details.");
    }
  };

  return (

    <>
    <Table

      columns={columnsConfig}
      className="employee-ticket-table" 
      dataSource={assignTicketDetailList}
      rowKey="id"
      loading={isLoading}
      bordered
      size="middle"
      scroll={{ x: 'max-content' }} 
      onRow={(record) => ({
        onClick: () => handleTicketDetailsClick(record.id), 
      })}
      rowClassName={(record) =>
        record.id === selectedRowKey ? 'selected-row' : '' 
      }
    />

    </>
  );
}

export default EmployeeAssignItemList;