import request from './request'

export function getAnnouncementList(params) {
  return request.get('/announcement/list', { params })
}

export function createAnnouncement(data) {
  return request.post('/announcement', data)
}

export function updateAnnouncement(id, data) {
  return request.put(`/announcement/${id}`, data)
}

export function withdrawAnnouncement(id) {
  return request.put(`/announcement/${id}/withdraw`)
}
