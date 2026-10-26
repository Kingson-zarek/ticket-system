
import{createRouter,createWebHistory} from 'vue-router'
import Login from '../views/Login.vue'
import MainLayout from '../layouts/MainLayout.vue'
import Dashboard from '../views/Dashboard.vue'
import TicketList from '../views/TicketList.vue'
import TicketDetail from '../views/TicketDetail.vue'

const router = createRouter({
    history:createWebHistory(),
    routes:[
        {
            path:'/login',
            component:Login
        },
        {
            path:'/',
            component:MainLayout,
            children:[
                {
                    path:'',component:Dashboard
                },
                {
                    path:'tickets',component:TicketList
                },
                {
                    path:'tickets/:id',component:TicketDetail
                }
            ]
        }
    ]
})
router.beforeEach((to, from, next) => {
    //to and from are Route Object,next() must be called to resolve the hook}
        const token = localStorage.getItem('token')
        if(!token && to.path!=='/login'){
            next('/login')
            return
        }
        if(token && to.path ==='/login'){
            next('/')
            return
        }
        next()
})

export default router