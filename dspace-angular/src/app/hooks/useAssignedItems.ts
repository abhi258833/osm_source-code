import { useState, useCallback, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';

export interface AssignedItem {
  id: string;
  title: string;
  handle: string;
  lastModified: string;
  assignedTo: string;
}

export interface PaginatedResult {
  content: AssignedItem[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

// Hook to fetch assigned items for current user
export const useAssignedItems = (page: number = 0, limit: number = 10) => {
  const { user, isAuthenticated } = useAuth();
  const [data, setData] = useState<PaginatedResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchAssignedItems = useCallback(async () => {
    if (!isAuthenticated || !user) {
      setError('User not authenticated');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem('dspace_token');
      const response = await fetch(
        `/api/items/assigned-to-me?page=${page}&limit=${limit}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }
      );

      if (!response.ok) {
        throw new Error(`Failed to fetch assigned items: ${response.statusText}`);
      }

      const result = await response.json();
      setData(result);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unknown error occurred';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, user, page, limit]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchAssignedItems();
    }
  }, [isAuthenticated, fetchAssignedItems]);

  return { data, loading, error, refetch: fetchAssignedItems };
};

// Hook to get count of assigned items
export const useAssignedItemsCount = () => {
  const { user, isAuthenticated } = useAuth();
  const [count, setCount] = useState<number>(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchCount = useCallback(async () => {
    if (!isAuthenticated || !user) {
      setError('User not authenticated');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem('dspace_token');
      const response = await fetch('/api/items/assigned-to-me/count', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch count: ${response.statusText}`);
      }

      const result = await response.json();
      setCount(result.count || 0);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unknown error occurred';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchCount();
    }
  }, [isAuthenticated, fetchCount]);

  return { count, loading, error, refetch: fetchCount };
};

// Hook for batch assignment (Admin only)
export const useBatchAssignment = () => {
  const { user, isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const assignItemsToUsers = useCallback(
    async (collectionId: string, userEmails: string[]) => {
      if (!isAuthenticated || !user || !user.isAdmin) {
        setError('Admin access required');
        return;
      }

      setLoading(true);
      setError(null);
      setSuccess(false);

      try {
        const token = localStorage.getItem('dspace_token');
        const response = await fetch(
          `/api/admin/items/${collectionId}/assign-to-users`,
          {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ userEmails }),
          }
        );

        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.message || `Assignment failed: ${response.statusText}`);
        }

        const result = await response.json();
        setSuccess(true);
        return result;
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unknown error occurred';
        setError(message);
      } finally {
        setLoading(false);
      }
    },
    [isAuthenticated, user]
  );

  return { assignItemsToUsers, loading, error, success };
};

// Hook for unassigning items
export const useUnassignItem = () => {
  const { isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const unassignItem = useCallback(
    async (itemId: string) => {
      if (!isAuthenticated) {
        setError('User not authenticated');
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const token = localStorage.getItem('dspace_token');
        const response = await fetch(`/api/items/${itemId}/unassign`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });

        if (!response.ok) {
          throw new Error(`Unassignment failed: ${response.statusText}`);
        }

        return true;
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unknown error occurred';
        setError(message);
      } finally {
        setLoading(false);
      }
    },
    [isAuthenticated]
  );

  return { unassignItem, loading, error };
};
