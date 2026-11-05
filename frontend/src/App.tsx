import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { useAuth } from './store/auth'
import Layout from './components/layout/Layout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import HomePage from './pages/HomePage'
import MarketPage from './pages/MarketPage'
import TradePage from './pages/TradePage'
import DashboardPage from './pages/DashboardPage'
import PortfolioPage from './pages/PortfolioPage'
import OrdersPage from './pages/OrdersPage'
import ListCertPage from './pages/ListCertPage'

function Guard({ children }: { children: React.ReactNode }) {
  const token = useAuth(s => s.token)
  return token ? <>{children}</> : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <BrowserRouter>
      <Toaster position="top-right" toastOptions={{
        style: { background: '#111118', color: '#e4e4e7', border: '1px solid #1e1e2e', fontSize: '13px' },
        success: { iconTheme: { primary: '#22c55e', secondary: '#111118' } },
      }} />
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<HomePage />} />
          <Route path="login"    element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route path="market"   element={<MarketPage />} />
          <Route path="trade/:certId" element={<TradePage />} />
          <Route path="dashboard"       element={<Guard><DashboardPage /></Guard>} />
          <Route path="portfolio"       element={<Guard><PortfolioPage /></Guard>} />
          <Route path="orders"          element={<Guard><OrdersPage /></Guard>} />
          <Route path="list-certificate" element={<Guard><ListCertPage /></Guard>} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
