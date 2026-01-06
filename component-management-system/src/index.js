import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import { BrowserRouter } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import { ItemProvider } from './Components/Items/UpdateItem/ItemContext';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(

    <BrowserRouter>
        <ItemProvider>
            <App />
        </ItemProvider>
    </BrowserRouter>


);

reportWebVitals();
