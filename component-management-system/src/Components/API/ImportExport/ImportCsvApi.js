import Cookies from "js-cookie";
import { URL } from "../URL";
import { message } from "antd";

async function ImportCsvApi(data){
    const token= atob(Cookies.get('authToken'));
        
       await fetch(URL+'/itemList/CSVFile', {
          method: 'POST',
          headers: { 
           'Authorization': `Bearer ${token}`,
          },
          body: data,
        }).then(async response => {  
          if (!response.ok) {
            const mess= await response.text();
            return message.warning(mess, 5);
          }else{
            const mess= await response.text();
            if(response.status===200){
            return message.success(mess, 3);
            }else{
              return message.warning(mess, 5);
            }
          }
        }).catch(error => {
          return message.error('API Error:'+ error.message,5);
        });
}

export default ImportCsvApi;