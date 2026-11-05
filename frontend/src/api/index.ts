import axios from 'axios'
import { useAuth } from '../store/auth'

export const userApi  = axios.create({ baseURL: '/api' })
export const tradeApi = axios.create({ baseURL: '/api' })

;[userApi, tradeApi].forEach(client => {
  client.interceptors.request.use(cfg => {
    const token = useAuth.getState().token
    if (token) cfg.headers.Authorization = `Bearer ${token}`
    return cfg
  })
  client.interceptors.response.use(r => r, err => {
    if (err.response?.status === 401) {
      useAuth.getState().clear()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  })
})

// ── Types ──────────────────────────────────────────────────────────────────

export interface Certificate {
  id: string; title: string; description: string
  totalCredits: number; availableCredits: number
  registryId: string; status: 'PENDING' | 'VERIFIED' | 'REJECTED'
  projectType?: string; projectLocation?: string; vintageYear?: number
  listedAt: string
}

export interface Holding {
  id: string; certificateId: string; certificateTitle: string
  creditsHeld: number; creditsReserved: number; avgBuyPrice: number
}

export interface Order {
  id: string; certificateId: string; certificateTitle: string
  side: 'BUY' | 'SELL'; orderType: 'LIMIT' | 'INSTANT'
  priceLimit: number; creditsRequested: number; creditsFilled: number
  status: 'OPEN' | 'PARTIALLY_FILLED' | 'FILLED' | 'CANCELLED' | 'EXPIRED'
  createdAt: string
}

export interface Trade {
  id: string; certificateTitle: string; side: 'BUY' | 'SELL'
  creditsTraded: number; tradePrice: number; totalAmount: number
  status: 'MATCHED' | 'COMPLETED' | 'FAILED'; createdAt: string
}

export interface PricePoint {
  recordedOn: string; openPrice: number; highPrice: number
  lowPrice: number; closePrice: number; volume: number; tradeCount: number
}

export interface OrderBook {
  bids: { price: number; credits: number; count: number }[]
  asks: { price: number; credits: number; count: number }[]
  lastPrice: number
}

// ── API calls ──────────────────────────────────────────────────────────────

export const authApi = {
  register: (d: { name: string; email: string; password: string }) => userApi.post('/auth/register', d),
  login:    (d: { email: string; password: string })               => userApi.post('/auth/login', d),
}

export const certificateApi = {
  list:   ()            => userApi.get<Certificate[]>('/certificates'),
  get:    (id: string)  => userApi.get<Certificate>(`/certificates/${id}`),
  create: (fd: FormData)=> userApi.post('/certificates', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
}

export const portfolioApi = {
  myHoldings: () => userApi.get<Holding[]>('/portfolio'),
}

export const orderApi = {
  placeLimitOrder: (d: { certificateId: string; side: 'BUY' | 'SELL'; priceLimit: number; credits: number }) =>
    tradeApi.post<Order>('/orders/limit', d),
  placeInstantBuy: (d: { certificateId: string; credits: number }) =>
    tradeApi.post<Order>('/orders/instant', d),
  myOrders:  ()            => tradeApi.get<Order[]>('/orders/my'),
  cancel:    (id: string)  => tradeApi.patch(`/orders/${id}/cancel`),
  orderBook: (id: string)  => tradeApi.get<OrderBook>(`/orders/book/${id}`),
}

export const tradeHistoryApi = {
  myTrades: () => tradeApi.get<Trade[]>('/trades/my'),
}

export const priceApi = {
  history: (days = 30) => tradeApi.get<PricePoint[]>('/prices/history', { params: { days } }),
}
