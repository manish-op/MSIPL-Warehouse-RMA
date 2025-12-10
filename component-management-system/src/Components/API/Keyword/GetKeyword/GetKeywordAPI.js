import Cookies from "js-cookie";
import { URL } from "../../URL";
import { message } from "antd";

async function GetKeywordAPI(){
    const token= atob(Cookies.get('authToken'));
 
    try {
        const response = await fetch(URL+'/keyword/keywordList', {
            method: 'GET',
            headers: { 
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            // const mess= await response.text();
            return null;
        } else {
            const json = await response.json();            
            return json;
        }
    } catch (error) {
        message.error('API Error:'+ error.message,2);
        return null;
    }
}

export default GetKeywordAPI;