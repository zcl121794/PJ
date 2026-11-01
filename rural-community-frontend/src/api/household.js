import request from './request'

export function getHouseholdList() {
  return request.get('/household/list')
}
