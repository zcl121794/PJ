import request from './request'

export function getAppealList(params) {
  return request.get('/appeal/list', { params })
}

export function getAppealDetail(id) {
  return request.get(`/appeal/${id}`)
}

export function getAppealFlows(id) {
  return request.get(`/appeal/${id}/flows`)
}

export function submitAppeal(data) {
  return request.post('/appeal', data)
}

export function acceptAppeal(id) {
  return request.put(`/appeal/${id}/accept`)
}

export function assignAppeal(id, data) {
  return request.put(`/appeal/${id}/assign`, data)
}

export function handleAppeal(id, data) {
  return request.put(`/appeal/${id}/handle`, data)
}

export function reportAppeal(id, data) {
  return request.put(`/appeal/${id}/report`, data)
}

export function finishAppeal(id) {
  return request.put(`/appeal/${id}/finish`)
}

export function approveAppeal(id) {
  return request.put(`/appeal/${id}/finish`)
}

export function rejectAppeal(id, data) {
  return request.put(`/appeal/${id}/reject`, data)
}

export function updateAppealResult(id, data) {
  return request.put(`/appeal/${id}/result`, data)
}

export function urgeAppeal(id, data) {
  return request.put(`/appeal/${id}/urge`, data)
}

export function satisfiedAppeal(id) {
  return request.put(`/appeal/${id}/satisfied`)
}

export function dissatisfiedAppeal(id, data) {
  return request.put(`/appeal/${id}/dissatisfied`, data)
}

export function followUpAppeal(id, data) {
  return request.put(`/appeal/${id}/follow-up`, data)
}
