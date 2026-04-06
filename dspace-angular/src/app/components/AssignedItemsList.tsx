import React, { useState } from 'react';
import { useAssignedItems, useAssignedItemsCount, useUnassignItem } from '../hooks/useAssignedItems';
import { useAuth } from '../contexts/AuthContext';

const AssignedItemsList: React.FC = () => {
  const { user } = useAuth();
  const [page, setPage] = useState(0);
  const [limit, setLimit] = useState(10);

  const { data, loading, error, refetch } = useAssignedItems(page, limit);
  const { count } = useAssignedItemsCount();
  const { unassignItem, loading: unassignLoading, error: unassignError } = useUnassignItem();

  if (!user) {
    return <div className="alert alert-warning">Please log in first</div>;
  }

  const handleUnassign = async (itemId: string) => {
    if (confirm('Are you sure you want to unassign this item?')) {
      await unassignItem(itemId);
      refetch();
    }
  };

  return (
    <div className="assigned-items-list">
      <h2>My Assigned Items ({count})</h2>

      {error && <div className="alert alert-danger">Error: {error}</div>}
      {unassignError && <div className="alert alert-danger">Error: {unassignError}</div>}

      {loading ? (
        <div className="text-center">
          <div className="spinner-border" role="status">
            <span className="sr-only">Loading...</span>
          </div>
        </div>
      ) : (
        <>
          {data && data.content.length > 0 ? (
            <>
              <table className="table table-striped table-hover">
                <thead className="table-dark">
                  <tr>
                    <th>Title</th>
                    <th>Handle</th>
                    <th>Assigned To</th>
                    <th>Last Modified</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((item) => (
                    <tr key={item.id}>
                      <td>{item.title}</td>
                      <td>
                        <a href={`/handle/${item.handle}`} target="_blank" rel="noopener noreferrer">
                          {item.handle}
                        </a>
                      </td>
                      <td>{item.assignedTo}</td>
                      <td>{new Date(item.lastModified).toLocaleDateString()}</td>
                      <td>
                        <button
                          className="btn btn-sm btn-danger"
                          onClick={() => handleUnassign(item.id)}
                          disabled={unassignLoading}
                        >
                          Unassign
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {/* Pagination Controls */}
              <nav aria-label="Page navigation">
                <ul className="pagination">
                  <li className={`page-item ${page === 0 ? 'disabled' : ''}`}>
                    <button
                      className="page-link"
                      onClick={() => setPage(page - 1)}
                      disabled={page === 0}
                    >
                      Previous
                    </button>
                  </li>
                  {data.totalPages > 0 &&
                    Array.from({ length: Math.min(5, data.totalPages) }).map((_, i) => (
                      <li
                        key={page + i}
                        className={`page-item ${page === i ? 'active' : ''}`}
                      >
                        <button
                          className="page-link"
                          onClick={() => setPage(i)}
                          disabled={page === i}
                        >
                          {i + 1}
                        </button>
                      </li>
                    ))}
                  <li className={`page-item ${page >= (data.totalPages - 1) ? 'disabled' : ''}`}>
                    <button
                      className="page-link"
                      onClick={() => setPage(page + 1)}
                      disabled={page >= (data.totalPages - 1)}
                    >
                      Next
                    </button>
                  </li>
                </ul>
              </nav>
            </>
          ) : (
            <div className="alert alert-info">No items assigned to you yet</div>
          )}
        </>
      )}
    </div>
  );
};

export default AssignedItemsList;
