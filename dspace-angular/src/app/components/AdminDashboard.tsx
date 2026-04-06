import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import LoginForm from './LoginForm';
import AssignedItemsList from './AssignedItemsList';
import BatchAssignmentForm from './BatchAssignmentForm';

type TabType = 'items' | 'batch-assign' | 'profile';

const AdminDashboard: React.FC = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const [activeTab, setActiveTab] = useState<TabType>('items');

  if (!isAuthenticated || !user) {
    return <LoginForm />;
  }

  if (!user.isAdmin) {
    return (
      <div className="alert alert-danger">
        <h4>Access Denied</h4>
        <p>You do not have admin permissions to access this dashboard.</p>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
        <div className="container-fluid">
          <span className="navbar-brand mb-0 h1">DSpace Item Assignment Admin</span>
          <div className="navbar-nav ms-auto">
            <span className="navbar-text mr-3">Logged in as: {user.email}</span>
            <button className="btn btn-outline-light" onClick={logout}>
              Logout
            </button>
          </div>
        </div>
      </nav>

      <div className="container-fluid">
        <div className="row">
          <div className="col-md-3">
            <div className="list-group">
              <button
                className={`list-group-item list-group-item-action ${
                  activeTab === 'items' ? 'active' : ''
                }`}
                onClick={() => setActiveTab('items')}
              >
                📋 Assigned Items
              </button>
              <button
                className={`list-group-item list-group-item-action ${
                  activeTab === 'batch-assign' ? 'active' : ''
                }`}
                onClick={() => setActiveTab('batch-assign')}
              >
                📦 Batch Assignment
              </button>
              <button
                className={`list-group-item list-group-item-action ${
                  activeTab === 'profile' ? 'active' : ''
                }`}
                onClick={() => setActiveTab('profile')}
              >
                👤 Profile
              </button>
            </div>
          </div>

          <div className="col-md-9">
            <div className="card">
              <div className="card-body">
                {activeTab === 'items' && <AssignedItemsList />}
                {activeTab === 'batch-assign' && <BatchAssignmentForm />}
                {activeTab === 'profile' && (
                  <div className="profile-section">
                    <h2>User Profile</h2>
                    <table className="table table-borderless">
                      <tbody>
                        <tr>
                          <td className="fw-bold">Email:</td>
                          <td>{user.email}</td>
                        </tr>
                        <tr>
                          <td className="fw-bold">Name:</td>
                          <td>{user.name}</td>
                        </tr>
                        <tr>
                          <td className="fw-bold">Role:</td>
                          <td>
                            <span className="badge bg-danger">Admin</span>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
