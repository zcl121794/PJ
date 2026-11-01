import request from './request'

export function getVillagerList(params) {
  return request.get('/villager/list', { params })
}

export function createVillager(data) {
  return request.post('/villager', data)
}

export function updateVillager(id, data) {
  return request.put(`/villager/${id}`, data)
}

export function deleteVillager(id) {
  return request.delete(`/villager/${id}`)
}

export function importVillagers(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/villager/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
