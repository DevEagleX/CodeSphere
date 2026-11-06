import { useCallback, useEffect, useState } from 'react';
import api from '../services/api';
import { errorMessage } from '../utils/format';

/**
 * GETs `url` (with optional query `params`) and tracks loading/error state.
 * Pass a falsy url to skip fetching. `reload(true)` refetches without flashing the
 * loading state - use it after a mutation so the page doesn't blink.
 */
export default function useFetch(url, params) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(Boolean(url));
  const [error, setError] = useState(null);
  const key = url ? url + JSON.stringify(params || {}) : '';

  const load = useCallback(
    async (silent = false) => {
      if (!url) {
        setLoading(false);
        return;
      }
      if (silent !== true) setLoading(true);
      try {
        const res = await api.get(url, { params });
        setData(res.data);
        setError(null);
      } catch (e) {
        setError(errorMessage(e, 'Failed to load'));
      } finally {
        setLoading(false);
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [key]
  );

  useEffect(() => {
    load();
  }, [load]);

  return { data, loading, error, reload: load, setData };
}
