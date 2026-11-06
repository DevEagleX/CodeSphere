import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeSanitize from 'rehype-sanitize';
import rehypeHighlight from 'rehype-highlight';
import PreBlock, { InlineCode } from './CodeBlock.jsx';

/**
 * Renders user-authored Markdown safely. rehype-sanitize runs BEFORE rehype-highlight
 * so any raw HTML a user types (<script>, onerror=...) is stripped first.
 */
export default function MarkdownRenderer({ content }) {
  if (!content) return null;

  return (
    <div className="prose prose-sm max-w-none break-words prose-code:before:content-none prose-code:after:content-none">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeSanitize, rehypeHighlight]}
        components={{ pre: PreBlock, code: InlineCode }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}
