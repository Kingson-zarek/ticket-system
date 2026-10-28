import request from "./request";

export function getAftersales(){
    return request.get(`/users/aftersales`)
}