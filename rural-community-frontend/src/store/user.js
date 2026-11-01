import { defineStore } from 'pinia'
import { login as loginApi, getInfo as getInfoApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    roles: JSON.parse(localStorage.getItem('roles') || '[]'),
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]'),
    dataScope: localStorage.getItem('dataScope') || ''
  }),
  getters: {
    isLogin: (state) => !!state.token
  },
  actions: {
    async login(loginForm) {
      const res = await loginApi(loginForm)
      this.token = res.data.token
      this.user = res.data.user
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      await this.fetchInfo()
    },
    async fetchInfo() {
      const res = await getInfoApi()
      this.roles = res.data.roles || []
      this.permissions = res.data.permissions || []
      this.dataScope = res.data.dataScope || ''
      localStorage.setItem('roles', JSON.stringify(this.roles))
      localStorage.setItem('permissions', JSON.stringify(this.permissions))
      localStorage.setItem('dataScope', this.dataScope)
    },
    logout() {
      this.token = ''
      this.user = null
      this.roles = []
      this.permissions = []
      this.dataScope = ''
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('roles')
      localStorage.removeItem('permissions')
      localStorage.removeItem('dataScope')
    }
  }
})
