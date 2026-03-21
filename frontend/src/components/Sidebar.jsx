import { Link, useLocation } from 'react-router-dom'
import { FiHome, FiGrid, FiSettings, FiChevronDown } from 'react-icons/fi'
import { useState, useMemo } from 'react'

const Sidebar = () => {
  const location = useLocation()
  const [workspaceOpen, setWorkspaceOpen] = useState(true)

  const activeNav = useMemo(() => {
    if (location.pathname === '/home') return 'home'
    if (location.pathname === '/boards') return 'boards'
    if (location.pathname.startsWith('/boards/')) return 'board-detail'
    if (location.pathname === '/settings') return 'settings'
    return null
  }, [location.pathname])

  const NavItem = ({ to, icon: Icon, label, id }) => {
    const isActive = activeNav === id
    const isBoardDetail = id === 'boards' && activeNav === 'board-detail'
    
    return (
      <Link
        to={to}
        className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-all duration-200 group ${
          isActive || isBoardDetail
            ? 'bg-blue-50 text-blue-600 font-semibold border-l-[3px] border-blue-600 rounded-l-none'
            : 'text-slate-600 hover:bg-gray-50 hover:text-slate-900 font-medium'
        }`}
      >
        <Icon className={`w-4 h-4 ${isActive || isBoardDetail ? 'text-blue-600' : 'text-slate-400 group-hover:text-slate-600'}`} />
        {label}
      </Link>
    )
  }

  return (
    <aside className="hidden lg:flex w-[260px] shrink-0 border-r border-slate-200 bg-white min-h-[calc(100vh-56px)] flex-col sticky top-14 left-0 overflow-y-auto">
      <div className="flex-1 px-4 py-6 space-y-8">
        {/* Main Navigation */}
        <div className="space-y-1">
          <NavItem to="/home" icon={FiHome} label="Home" id="home" />
          <NavItem to="/boards" icon={FiGrid} label="Boards" id="boards" />
        </div>

        {/* Workspace Section */}
        <div>
          <div className="flex items-center justify-between text-[11px] font-bold text-slate-400 uppercase tracking-[0.5px] px-3 mb-2">
            Workspaces
            <button
              onClick={() => setWorkspaceOpen(!workspaceOpen)}
              className="p-1 hover:bg-slate-100 rounded transition-colors"
            >
              <FiChevronDown className={`w-3.5 h-3.5 text-slate-400 transition-transform duration-200 ${workspaceOpen ? 'rotate-180' : ''}`} />
            </button>
          </div>
          
          {workspaceOpen && (
            <div className="flex items-center gap-3 px-3 py-2">
              <div className="w-6 h-6 rounded bg-gradient-to-br from-blue-600 to-indigo-700 text-white flex items-center justify-center text-[10px] font-bold shadow-sm shrink-0">
                K
              </div>
              <span className="text-sm font-semibold text-slate-700 truncate">Kanban Workspace</span>
            </div>
          )}
        </div>
      </div>

      {/* Bottom Section */}
      <div className="p-4 border-t border-slate-100 bg-white">
        <NavItem to="/settings" icon={FiSettings} label="Settings" id="settings" />
      </div>
    </aside>
  )
}

export default Sidebar
