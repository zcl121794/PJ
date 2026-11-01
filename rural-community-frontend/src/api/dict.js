import request from './request'

export function getDictByType(type) {
  return request.get(`/dict/type/${type}`)
}
