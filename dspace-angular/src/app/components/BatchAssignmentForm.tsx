import React, { useState } from 'react';
import { useBatchAssignment } from '../hooks/useAssignedItems';
import { useAuth } from '../contexts/AuthContext';

interface BatchAssignmentFormProps {
  collectionId?: string;
}

const BatchAssignmentForm: React.FC<BatchAssignmentFormProps> = ({ collectionId = '' }) => {
  const { user } = useAuth();
  const { assignItemsToUsers, loading, error, success } = useBatchAssignment();

  const [selectedCollectionId, setSelectedCollectionId] = useState(collectionId);
  const [userEmails, setUserEmails] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  if (!user || !user.isAdmin) {
    return (
      <div className="alert alert-danger">
        Admin access required to perform batch assignments
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSuccessMessage('');

    const emails = userEmails
      .split('\n')
      .map((email) => email.trim())
      .filter((email) => email.length > 0);

    if (emails.length === 0) {
      alert('Please enter at least one email address');
      return;
    }

    if (!selectedCollectionId) {
      alert('Please select a collection');
      return;
    }

    try {
      const result = await assignItemsToUsers(selectedCollectionId, emails);
      if (result) {
        setSuccessMessage(
          `Successfully assigned items to ${emails.length} users. ${result.itemsAssigned || 0} items were assigned.`
        );
        setUserEmails('');
      }
    } catch (err) {
      console.error('Batch assignment error:', err);
    }
  };

  return (
    <div className="batch-assignment-form">
      <h2>Batch Assign Items to Users</h2>
      <p className="text-muted">Distribute all items in a collection equally among users</p>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && successMessage && (
        <div className="alert alert-success">{successMessage}</div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="collectionId">Collection ID:</label>
          <input
            id="collectionId"
            type="text"
            className="form-control"
            value={selectedCollectionId}
            onChange={(e) => setSelectedCollectionId(e.target.value)}
            placeholder="e.g., 123e4567-e89b-12d3-a456-426614174000"
            required
          />
          <small className="form-text text-muted">
            Enter the UUID of the collection containing items to assign
          </small>
        </div>

        <div className="form-group">
          <label htmlFor="userEmails">User Emails (one per line):</label>
          <textarea
            id="userEmails"
            className="form-control"
            rows={6}
            value={userEmails}
            onChange={(e) => setUserEmails(e.target.value)}
            placeholder="user1@example.com&#10;user2@example.com&#10;user3@example.com"
            required
          />
          <small className="form-text text-muted">
            Items will be distributed equally among these users
          </small>
        </div>

        <div className="form-group">
          <button
            type="submit"
            className="btn btn-primary btn-lg"
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm mr-2" role="status" aria-hidden="true"></span>
                Assigning Items...
              </>
            ) : (
              'Assign Items'
            )}
          </button>
        </div>

        <div className="alert alert-info mt-4">
          <h5>How it works:</h5>
          <ul className="mb-0">
            <li>All items in the selected collection will be fetched</li>
            <li>Items will be distributed as equally as possible among the specified users</li>
            <li>If distribution results in remainders, the first users receive additional items</li>
            <li>Each item's metadata will be updated with the assigned user email</li>
            <li>The search index will be automatically updated</li>
          </ul>
        </div>
      </form>
    </div>
  );
};

export default BatchAssignmentForm;
