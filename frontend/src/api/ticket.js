import request from "./request";

export function getTickets(params){
    return request.get('/tickets',{params})
}

export function getTicket(id){
    return request.get(`/tickets/${id}`)
}

export function createTicket(data){
    return request.post(`/tickets`,data)
}

export function assignTicket(id,assigneeId){
    return request.put(`/tickets/${id}/assign`,{assigneeId})
}
export function updateStatus(id,status){
    return request.put(`/tickets/${id}/status`,{status})
}

export function deleteTicket(id){
    return request.delete(`/tickets/${id}`)
}