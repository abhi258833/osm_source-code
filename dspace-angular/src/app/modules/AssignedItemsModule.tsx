import React from 'react';
import { AuthProvider } from '../contexts/AuthContext';
import AdminDashboard from '../components/AdminDashboard';
import 'bootstrap/dist/css/bootstrap.min.css';
import './AssignedItemsModule.css';

/**
 * Complete Assigned Items Module
 * 
 * This is a self-contained module that can be integrated into any DSpace Angular UI.
 * It includes authentication, item listing, and admin batch assignment features.
 * 
 * Usage in your main app:
 * 
 * import { AssignedItemsModule } from './modules/AssignedItemsModule';
 * 
 * export function App() {
 *   return <AssignedItemsModule />;
 * }
 */
const AssignedItemsModule: React.FC = () => {
  return (
    <AuthProvider>
      <div className="assigned-items-module">
        <AdminDashboard />
      </div>
    </AuthProvider>
  );
};

export default AssignedItemsModule;
