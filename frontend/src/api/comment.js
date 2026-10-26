import request from "./request";

export function getComments(ticketId){
    return request.get(`/tickets/${ticketId}/comments`)
}

export function addComment(ticketId,content){
    return request.post(`/tickets/${ticketId}/comments`,{content})
}