import React, { useState } from 'react';
import { Table, Typography, Tag, message } from 'antd';
import { useTicketDetails } from '../../RepairingContext/RepairingContext';
import TicketDetailsUpdateAPI from '../../../API/FRU/GetTicketDetails/TicketDetailsUpdateAPI';

// Define columns outside the component for better performance
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
        } else if (status?.toLowerCase().includes('pending') || status?.toLowerCase().includes('open')) {
            color = 'volcano';
        }
        return <Tag color={color}>{status?.toUpperCase() || 'N/A'}</Tag>;
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
    render: () => (
      <Typography.Link>
        Assign
      </Typography.Link>
    ),
  },
];

// Add this CSS to your stylesheet (e.g., App.css) if you haven't already.
// It can be shared by all your tables.
/*
  .selected-row {
    background-color: #e6f7ff !important;
  }
*/


function TableFormateFormAdminRepairingSearch({ ticketDetails, isLoading }) {
  const { setTicketUpdateDetails } = useTicketDetails();
  const [selectedRowKey, setSelectedRowKey] = useState(null);

  const handleTicketDetailsClick = async (id) => {
    // Set the selected row for immediate visual feedback
    setSelectedRowKey(id);

    // Call the API to fetch the full details for the form
    try {
        await TicketDetailsUpdateAPI(id, setTicketUpdateDetails);
    } catch (error) {
        message.error("Failed to fetch ticket details for assignment.");
    }
  };

  return (
    <Table
      columns={columnsConfig}
      dataSource={ticketDetails}
      rowKey="id"
      loading={isLoading}
      bordered
      size="middle"
      scroll={{ x: 'max-content' }} // Ensures responsiveness on small screens
      onRow={(record) => ({
        onClick: () => handleTicketDetailsClick(record.id), // Make entire row clickable
      })}
      rowClassName={(record) =>
        record.id === selectedRowKey ? 'selected-row' : '' // Highlight the selected row
      }
    />
  );
}

export default TableFormateFormAdminRepairingSearch;