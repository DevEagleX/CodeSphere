import { useState } from 'react';
import MarkdownRenderer from '../components/MarkdownRenderer.jsx';

const SAMPLE = `# Welcome to DevCollab

This editor supports **GitHub-flavored Markdown**, including:

- Bullet lists
- [Links](https://example.com)
- \`inline code\`

## Code blocks with syntax highlighting

\`\`\`javascript
function greet(name) {
  return \`Hello, \${name}!\`;
}
\`\`\`

> Blockquotes work too.

| Feature | Status |
|---|---|
| Markdown | Done |
| Code blocks | Done |
`;

export default function MarkdownDemoPage() {
  const [content, setContent] = useState(SAMPLE);

  return (
    <div className="max-w-5xl mx-auto mt-10 px-4">
      <h1 className="text-xl font-semibold mb-4">Markdown preview</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          className="w-full h-[480px] border border-gray-300 rounded-lg p-3 font-mono text-sm resize-none"
        />
        <div className="border border-gray-200 rounded-lg p-4 h-[480px] overflow-y-auto bg-white">
          <MarkdownRenderer content={content} />
        </div>
      </div>
    </div>
  );
}
