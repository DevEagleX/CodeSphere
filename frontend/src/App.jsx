import { Navigate, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import WorkspaceLayout from './layouts/WorkspaceLayout.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import MarkdownDemoPage from './pages/MarkdownDemoPage.jsx';
import WorkspacesPage from './pages/WorkspacesPage.jsx';
import WorkspaceHome from './pages/WorkspaceHome.jsx';
import ChannelPage from './pages/ChannelPage.jsx';
import DiscussionPage from './pages/DiscussionPage.jsx';
import KnowledgeBasePage from './pages/KnowledgeBasePage.jsx';
import ArticlePage from './pages/ArticlePage.jsx';
import DocumentsPage from './pages/DocumentsPage.jsx';
import DocumentPage from './pages/DocumentPage.jsx';
import ResourcesPage from './pages/ResourcesPage.jsx';
import AnnouncementsPage from './pages/AnnouncementsPage.jsx';
import ActivityPage from './pages/ActivityPage.jsx';
import MembersPage from './pages/MembersPage.jsx';
import SearchPage from './pages/SearchPage.jsx';
import NotificationsPage from './pages/NotificationsPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import AdminPage from './pages/AdminPage.jsx';

const guard = (element) => <ProtectedRoute>{element}</ProtectedRoute>;

export default function App() {
  return (
    <div className="min-h-screen">
      <Navbar />
      <main>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route path="/" element={guard(<WorkspacesPage />)} />
          <Route path="/notifications" element={guard(<NotificationsPage />)} />
          <Route path="/profile" element={guard(<ProfilePage />)} />
          <Route path="/admin" element={guard(<AdminPage />)} />
          <Route path="/markdown-preview" element={guard(<MarkdownDemoPage />)} />

          <Route path="/w/:workspaceId" element={guard(<WorkspaceLayout />)}>
            <Route index element={<WorkspaceHome />} />
            <Route path="channels/:channelId" element={<ChannelPage />} />
            <Route path="discussions/:discussionId" element={<DiscussionPage />} />
            <Route path="kb" element={<KnowledgeBasePage />} />
            <Route path="kb/:articleId" element={<ArticlePage />} />
            <Route path="docs" element={<DocumentsPage />} />
            <Route path="docs/:documentId" element={<DocumentPage />} />
            <Route path="resources" element={<ResourcesPage />} />
            <Route path="announcements" element={<AnnouncementsPage />} />
            <Route path="activity" element={<ActivityPage />} />
            <Route path="members" element={<MembersPage />} />
            <Route path="search" element={<SearchPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
