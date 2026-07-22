import { get, post, put, del } from '@/api/request'
import type { Campus, Building, Floor, MeetingRoom, Workstation, Facility, PageResult, PageParams } from '@/types'

export function getCampusList(params: PageParams & { keyword?: string }) { return get<PageResult<Campus>>('/resources/campus', params) }
export function getAllCampuses() { return get<Campus[]>('/resources/campus/all') }
export function createCampus(data: Partial<Campus>) { return post<Campus>('/resources/campus', data) }
export function updateCampus(id: number, data: Partial<Campus>) { return put<Campus>(`/resources/campus/${id}`, data) }
export function deleteCampus(id: number) { return del<void>(`/resources/campus/${id}`) }

export function getBuildingList(params: PageParams & { keyword?: string; campusId?: number }) { return get<PageResult<Building>>('/resources/buildings', params) }
export function getBuildingsByCampus(campusId: number) { return get<Building[]>(`/resources/buildings/by-campus/${campusId}`) }
export function createBuilding(data: Partial<Building>) { return post<Building>('/resources/buildings', data) }
export function updateBuilding(id: number, data: Partial<Building>) { return put<Building>(`/resources/buildings/${id}`, data) }
export function deleteBuilding(id: number) { return del<void>(`/resources/buildings/${id}`) }

export function getFloorList(params: PageParams & { keyword?: string; buildingId?: number }) { return get<PageResult<Floor>>('/resources/floors', params) }
export function getFloorsByBuilding(buildingId: number) { return get<Floor[]>(`/resources/floors/by-building/${buildingId}`) }
export function createFloor(data: Partial<Floor>) { return post<Floor>('/resources/floors', data) }
export function updateFloor(id: number, data: Partial<Floor>) { return put<Floor>(`/resources/floors/${id}`, data) }
export function deleteFloor(id: number) { return del<void>(`/resources/floors/${id}`) }

export function getRoomList(params: PageParams & { keyword?: string; floorId?: number }) { return get<PageResult<MeetingRoom>>('/resources/rooms', params) }
export function createRoom(data: Partial<MeetingRoom>) { return post<MeetingRoom>('/resources/rooms', data) }
export function updateRoom(id: number, data: Partial<MeetingRoom>) { return put<MeetingRoom>(`/resources/rooms/${id}`, data) }
export function deleteRoom(id: number) { return del<void>(`/resources/rooms/${id}`) }

export function getWorkstationList(params: PageParams & { keyword?: string; floorId?: number }) { return get<PageResult<Workstation>>('/resources/workstations', params) }
export function createWorkstation(data: Partial<Workstation>) { return post<Workstation>('/resources/workstations', data) }
export function updateWorkstation(id: number, data: Partial<Workstation>) { return put<Workstation>(`/resources/workstations/${id}`, data) }
export function deleteWorkstation(id: number) { return del<void>(`/resources/workstations/${id}`) }

export function getFacilityList(params: PageParams & { keyword?: string }) { return get<PageResult<Facility>>('/resources/facilities', params) }
export function createFacility(data: Partial<Facility>) { return post<Facility>('/resources/facilities', data) }
export function updateFacility(id: number, data: Partial<Facility>) { return put<Facility>(`/resources/facilities/${id}`, data) }
export function deleteFacility(id: number) { return del<void>(`/resources/facilities/${id}`) }
