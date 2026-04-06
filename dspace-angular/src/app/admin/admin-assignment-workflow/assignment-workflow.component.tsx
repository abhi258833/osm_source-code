import React, { useState, useEffect } from 'react';
import './AssignmentWorkflow.scss';

/**
 * Assignment Workflow Component
 * 
 * Provides a step-by-step UI for:
 * 1. Selecting community/subcommunity/collection
 * 2. Selecting users with checkboxes
 * 3. Confirming and assigning items
 */
const AssignmentWorkflow = () => {
  const [step, setStep] = useState(1); // Step 1: Community selection, Step 2: Collection selection, Step 3: User selection, Step 4: Confirm
  const [communities, setCommunities] = useState([]);
  const [subcommunities, setSubcommunities] = useState([]);
  const [collections, setCollections] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const [selectedCommunity, setSelectedCommunity] = useState(null);
  const [selectedSubcommunity, setSelectedSubcommunity] = useState(null);
  const [selectedCollection, setSelectedCollection] = useState(null);
  const [selectedUsers, setSelectedUsers] = useState([]);

  // Fetch root communities on component load
  useEffect(() => {
    fetchCommunities();
  }, []);

  const fetchCommunities = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch('/api/assignment-workflow/communities', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to load communities');
      }

      const data = await response.json();
      setCommunities(data);
    } catch (err) {
      setError(err.message);
      console.error('Error fetching communities:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchSubcommunities = async (communityId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`/api/assignment-workflow/communities/${communityId}/subcommunities`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to load subcommunities');
      }

      const data = await response.json();
      setSubcommunities(data);
    } catch (err) {
      setError(err.message);
      console.error('Error fetching subcommunities:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchCollections = async (communityId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`/api/assignment-workflow/communities/${communityId}/collections`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to load collections');
      }

      const data = await response.json();
      setCollections(data);
    } catch (err) {
      setError(err.message);
      console.error('Error fetching collections:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch('/api/assignment-workflow/users', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to load users');
      }

      const data = await response.json();
      setUsers(data);
    } catch (err) {
      setError(err.message);
      console.error('Error fetching users:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCommunitySelect = (community) => {
    setSelectedCommunity(community);
    setSelectedSubcommunity(null);
    setSelectedCollection(null);
    setSubcommunities([]);
    setCollections([]);

    if (community.subcommunities && community.subcommunities.length > 0) {
      fetchSubcommunities(community.id);
    } else {
      // No subcommunities, fetch collections directly
      fetchCollections(community.id);
    }
  };

  const handleSubcommunitySelect = (subcommunity) => {
    setSelectedSubcommunity(subcommunity);
    setSelectedCollection(null);
    setCollections([]);
    fetchCollections(subcommunity.id);
  };

  const handleCollectionSelect = (collection) => {
    setSelectedCollection(collection);
    setStep(3);
    fetchUsers();
  };

  const handleUserToggle = (userId) => {
    setSelectedUsers((prev) => {
      if (prev.includes(userId)) {
        return prev.filter((id) => id !== userId);
      } else {
        return [...prev, userId];
      }
    });
  };

  const handleAssign = async () => {
    if (!selectedCollection || selectedUsers.length === 0) {
      setError('Please select a collection and at least one user');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await fetch('/api/assignment-workflow/assign', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        },
        body: JSON.stringify({
          collectionId: selectedCollection.id,
          selectedUserIds: selectedUsers
        })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to assign items');
      }

      const data = await response.json();
      setSuccess(data.message);
      
      // Reset form after successful assignment
      setTimeout(() => {
        resetForm();
      }, 2000);
    } catch (err) {
      setError(err.message);
      console.error('Error assigning items:', err);
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setStep(1);
    setSelectedCommunity(null);
    setSelectedSubcommunity(null);
    setSelectedCollection(null);
    setSelectedUsers([]);
    setSuccess(null);
    setCommunities([]);
    setSubcommunities([]);
    setCollections([]);
    setUsers([]);
    fetchCommunities();
  };

  return (
    <div className="assignment-workflow-container">
      <h1>Assignment Workflow</h1>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {/* Step 1: Select Community */}
      {step === 1 && (
        <div className="workflow-step">
          <h2>Step 1: Select Community</h2>
          {loading ? (
            <p>Loading communities...</p>
          ) : (
            <div className="community-list">
              {communities.map((community) => (
                <div
                  key={community.id}
                  className={`community-item ${selectedCommunity?.id === community.id ? 'selected' : ''}`}
                  onClick={() => handleCommunitySelect(community)}
                >
                  <div className="community-name">{community.name}</div>
                  <div className="community-handle">{community.handle}</div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Step 2: Select Collection */}
      {(step === 2 || (step >= 2 && step < 3)) && selectedCommunity && (
        <div className="workflow-step">
          <h2>Step 2: Select Collection</h2>

          {/* Show subcommunities if available */}
          {subcommunities && subcommunities.length > 0 && !selectedSubcommunity && (
            <div className="subcommunity-section">
              <h3>Or select a Sub-community first:</h3>
              <div className="subcommunity-list">
                {subcommunities.map((subcommunity) => (
                  <div
                    key={subcommunity.id}
                    className={`subcommunity-item ${selectedSubcommunity?.id === subcommunity.id ? 'selected' : ''}`}
                    onClick={() => handleSubcommunitySelect(subcommunity)}
                  >
                    <div className="subcommunity-name">{subcommunity.name}</div>
                    <div className="subcommunity-handle">{subcommunity.handle}</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Show collections */}
          {collections && collections.length > 0 && (
            <div className="collection-section">
              <h3>Collections:</h3>
              <div className="collection-list">
                {collections.map((collection) => (
                  <div
                    key={collection.id}
                    className={`collection-item ${selectedCollection?.id === collection.id ? 'selected' : ''}`}
                    onClick={() => handleCollectionSelect(collection)}
                  >
                    <div className="collection-name">{collection.name}</div>
                    <div className="collection-handle">{collection.handle}</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {collections.length === 0 && !loading && (
            <p>No collections found in this community.</p>
          )}

          <button
            className="btn btn-secondary mt-3"
            onClick={() => {
              setStep(1);
              setSelectedCommunity(null);
              setSelectedSubcommunity(null);
              setCollections([]);
            }}
          >
            Back
          </button>
        </div>
      )}

      {/* Step 3: Select Users */}
      {step === 3 && selectedCollection && (
        <div className="workflow-step">
          <h2>Step 3: Select Users</h2>
          <div className="selected-collection">
            <strong>Selected Collection:</strong> {selectedCollection.name}
          </div>

          {loading ? (
            <p>Loading users...</p>
          ) : (
            <div className="user-list">
              {users.map((user) => (
                <div key={user.id} className="user-checkbox-item">
                  <input
                    type="checkbox"
                    id={`user-${user.id}`}
                    checked={selectedUsers.includes(user.id)}
                    onChange={() => handleUserToggle(user.id)}
                  />
                  <label htmlFor={`user-${user.id}`}>
                    <span className="user-name">{user.name}</span>
                    <span className="user-email">({user.email})</span>
                  </label>
                </div>
              ))}
            </div>
          )}

          {selectedUsers.length > 0 && (
            <div className="selected-users-summary">
              <strong>Selected Users ({selectedUsers.length}):</strong>
              <div className="selected-users-list">
                {users
                  .filter((u) => selectedUsers.includes(u.id))
                  .map((u) => (
                    <span key={u.id} className="user-badge">
                      {u.name}
                    </span>
                  ))}
              </div>
            </div>
          )}

          <div className="workflow-buttons">
            <button
              className="btn btn-secondary"
              onClick={() => {
                setStep(2);
                setSelectedUsers([]);
              }}
            >
              Back
            </button>
            <button
              className="btn btn-primary"
              onClick={() => setStep(4)}
              disabled={selectedUsers.length === 0}
            >
              Next
            </button>
          </div>
        </div>
      )}

      {/* Step 4: Confirm Assignment */}
      {step === 4 && selectedCollection && (
        <div className="workflow-step">
          <h2>Step 4: Confirm Assignment</h2>

          <div className="confirmation-summary">
            <div className="summary-item">
              <strong>Collection:</strong> {selectedCollection.name}
            </div>
            <div className="summary-item">
              <strong>Number of Users:</strong> {selectedUsers.length}
            </div>
            <div className="summary-item">
              <strong>Users to Assign:</strong>
              <ul>
                {users
                  .filter((u) => selectedUsers.includes(u.id))
                  .map((u) => (
                    <li key={u.id}>
                      {u.name} ({u.email})
                    </li>
                  ))}
              </ul>
            </div>
          </div>

          <div className="confirmation-message">
            <p>Items in this collection will be distributed equally among the selected users.</p>
          </div>

          <div className="workflow-buttons">
            <button
              className="btn btn-secondary"
              onClick={() => setStep(3)}
              disabled={loading}
            >
              Back
            </button>
            <button
              className="btn btn-danger"
              onClick={resetForm}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              className="btn btn-success"
              onClick={handleAssign}
              disabled={loading}
            >
              {loading ? 'Assigning...' : 'Confirm Assignment'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AssignmentWorkflow;
