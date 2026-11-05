import { Link, NavLink, useNavigate } from 'react-router-dom'
import { Leaf, LayoutDashboard, BarChart2, ListOrdered, Briefcase, LogOut, User } from 'lucide-react'
import { useAuth } from '../../store/auth'
import toast from 'react-hot-toast'

const nav = [
  { to: '/market',    label: 'Market',    icon: BarChart2 },
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/portfolio', label: 'Portfolio', icon: Briefcase },
  { to: '/orders',    label: 'Orders',    icon: ListOrdered },
]

export default function Navbar() {
  const { user, clear } = useAuth()
  const navigate = useNavigate()

  const logout = () => { clear(); toast.success('Logged out'); navigate('/login') }

  return (
    <header className="h-12 border-b border-border bg-surface flex items-center px-5 gap-5 sticky top-0 z-50">
      <Link to="/" className="flex items-center gap-2 flex-shrink-0">
        <Leaf size={15} className="text-green" />
        <span className="font-semibold text-sm tracking-tight">TrueCarbon</span>
      </Link>

      {user && (
        <nav className="flex items-center gap-0.5">
          {nav.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to}
              className={({ isActive }) =>
                `flex items-center gap-1.5 px-3 py-1.5 rounded text-xs font-medium transition-colors ${
                  isActive ? 'bg-green/10 text-green' : 'text-dim hover:text-text hover:bg-white/5'
                }`}>
              <Icon size={12} />{label}
            </NavLink>
          ))}
        </nav>
      )}

      <div className="ml-auto flex items-center gap-3">
        {user ? (
          <>
            <span className="text-xs text-dim hidden sm:flex items-center gap-1.5">
              <User size={12} />{user.name}
            </span>
            <button onClick={logout} className="text-xs text-dim hover:text-text flex items-center gap-1.5">
              <LogOut size={12} /><span className="hidden sm:inline">Logout</span>
            </button>
          </>
        ) : (
          <div className="flex items-center gap-2">
            <Link to="/login" className="text-xs text-dim hover:text-text">Login</Link>
            <Link to="/register" className="btn-primary text-xs px-3 py-1.5">Register</Link>
          </div>
        )}
      </div>
    </header>
  )
}
