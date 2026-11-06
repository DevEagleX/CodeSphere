export function formatDate(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
}

export function formatBytes(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

// Pulls the most useful message out of the backend's ErrorResponse shape
// ({ message, fieldErrors }) so toasts show real reasons, not "Request failed".
export function errorMessage(err, fallback = 'Something went wrong') {
  const data = err?.response?.data;
  if (data?.fieldErrors) return Object.values(data.fieldErrors).join(', ');
  return data?.message || fallback;
}

export function snippet(text, max = 140) {
  if (!text) return '';
  const plain = text.replace(/[#*`>_~-]/g, '').replace(/\s+/g, ' ').trim();
  return plain.length > max ? `${plain.slice(0, max)}...` : plain;
}
