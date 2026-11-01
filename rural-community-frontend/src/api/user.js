import request from './request'

export function getUserList() {
  return request.get('/user/list')
}

export function getAssignableUsers() {
  return request.get('/user/assignable')
}

export function createUser(data) {
  return request.post('/user', data)
}

export function getRoleList() {
  return request.get('/role/list')
}
