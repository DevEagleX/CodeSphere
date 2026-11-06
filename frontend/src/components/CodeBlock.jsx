import { useState } from 'react';

// After syntax highlighting, a code block's children are a tree of <span> elements,
// not a string - so String(children) would give "[object Object]". Walk the tree.
function extractText(node) {
  if (node == null || typeof node === 'boolean') return '';
  if (typeof node === 'string' || typeof node === 'number') return String(node);
  if (Array.isArray(node)) return node.map(extractText).join('');
  if (node.props) return extractText(node.props.children);
  return '';
}

// Rendered in place of react-markdown's <pre>. The child is the <code> element
// holding the (already highlighted) content and its "language-xxx" class.
export default function PreBlock({ children }) {
  const [copied, setCopied] = useState(false);
  const child = Array.isArray(children) ? children[0] : children;
  const className = child?.props?.className || '';
  const language = /language-([\w-]+)/.exec(className)?.[1] || 'text';
  const codeText = extractText(child?.props?.children).replace(/\n$/, '');

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(codeText);
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    } catch {
      /* clipboard unavailable (e.g. insecure context) - ignore */
    }
  }

  return (
    <div className="not-prose my-3 overflow-hidden rounded-lg border border-gray-200">
      <div className="flex items-center justify-between bg-gray-100 px-3 py-1.5 font-mono text-xs text-gray-500">
        <span>{language}</span>
        <button type="button" onClick={handleCopy} className="transition-colors hover:text-gray-800">
          {copied ? 'Copied!' : 'Copy'}
        </button>
      </div>
      <pre className="overflow-x-auto bg-white p-0 text-sm">
        <code className={className || 'hljs'}>{child?.props?.children}</code>
      </pre>
    </div>
  );
}

// Inline `code` only - fenced blocks never reach this because PreBlock renders its own <code>.
export function InlineCode({ children }) {
  return (
    <code className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[0.85em] text-pink-600">{children}</code>
  );
}
