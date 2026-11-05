import { create } from 'zustand'

export interface User { id: string; name: string; email: string }

interface AuthState {
  user: User | null
  token: string | null
  setAuth: (user: User, token: string) => void
  clear: () => void
}

export const useAuth = create<AuthState>((set) => ({
  user: null,
  token: localStorage.getItem('tc_token'),
  setAuth: (user, token) => {
    localStorage.setItem('tc_token', token)
    set({ user, token })
  },
  clear: () => {
    localStorage.removeItem('tc_token')
    set({ user: null, token: null })
  },
}))
