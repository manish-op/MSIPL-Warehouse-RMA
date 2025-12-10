import { createContext, useState, useContext, useEffect } from "react";

const ItemContext = createContext();

export const ItemProvider = ({ children }) => {
  const [itemDetails, setItemDetails] = useState(JSON.parse(localStorage.getItem('itemDetails')) ||{
    itemId: "",
    serialNo: "",
    rackNo: "",
    boxNo: "",
    partNo: "",
    modelNo: "",
    itemStatus: "",
    spareLocation: "",
    system: "",
    systemVersion: "",
    moduleFor: "",
    itemAvailability: "",
    itemDescription: "",
    addedByEmail: "",
    addingDate: "",
    region: "",
    keyword: "",
    subKeyword: "",
    empEmail: "",
    partyName: "",
    remark: "",
    updateDate: "",
  });

  const [itemHistory, setItemHistory] = useState(JSON.parse(localStorage.getItem('itemHistory')) ||[]);

  useEffect(() => {
    localStorage.setItem('itemDetails', JSON.stringify(itemDetails));
  }, [itemDetails]);

  useEffect(() => {
    localStorage.setItem('itemHistory', JSON.stringify(itemHistory));
  }, [itemHistory]);


  return (
    <ItemContext.Provider
      value={{ itemDetails, setItemDetails, itemHistory, setItemHistory }}
    >
      {children}
    </ItemContext.Provider>
  );
};

export const useItemDetails = () => {
  return useContext(ItemContext);
};
