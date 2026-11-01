import request from './request'

export function getServiceItems() {
  return request.get('/service/item/list')
}

export function getServiceItem(id) {
  return request.get(`/service/item/${id}`)
}

export function getServiceApplies(params) {
  return request.get('/service/apply/list', { params })
}

export function getServiceApply(id) {
  return request.get(`/service/apply/${id}`)
}

export function submitServiceApply(data) {
  return request.post('/service/apply', data)
}

export function acceptServiceApply(id) {
  return request.put(`/service/apply/${id}/accept`)
}

export function gridPassServiceApply(id, data) {
  return request.put(`/service/apply/${id}/grid-pass`, data)
}

export function gridRejectServiceApply(id, data) {
  return request.put(`/service/apply/${id}/grid-reject`, data)
}

export function finishServiceApply(id, data) {
  return request.put(`/service/apply/${id}/finish`, data)
}

export function approveServiceApply(id) {
  return request.put(`/service/apply/${id}/approve`)
}

export function rejectServiceApply(id, data) {
  return request.put(`/service/apply/${id}/reject`, data)
}
