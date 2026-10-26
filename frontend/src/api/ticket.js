import request from "./request";

export function getTickets(params){
    return request.get('/tickets',{params})
}

export function getTicket(id){
    return request.get(`/tickets/${id}`)
}