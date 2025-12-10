import { createContext, useState, useContext, useEffect } from "react";

const RepairingContext = createContext();

export const RepairingItemProvider = ({ children }) => {
 

  const [ticketUpdateDetails, setTicketUpdateDetails] = useState();
  const [ticketUpdateDetailsForEmployee, setTicketUpdateDetailsForEmployee]=useState();

 
  useEffect(() => {
    localStorage.setItem('ticketUpdateDetails', JSON.stringify(ticketUpdateDetails));
  }, [ticketUpdateDetails]);

   useEffect(() => {
   localStorage.setItem('ticketUpdateDetailsForEmployee', JSON.stringify(ticketUpdateDetailsForEmployee));
  }, [ticketUpdateDetailsForEmployee]);

  return (
    <RepairingContext.Provider
      value={{ticketUpdateDetails, setTicketUpdateDetails, ticketUpdateDetailsForEmployee, setTicketUpdateDetailsForEmployee }}
    >
      {children}
    </RepairingContext.Provider>
  );
};

export const useTicketDetails = () => {
  return useContext(RepairingContext);
};
