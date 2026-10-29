// Vite 启动时会 child_process.exec('net use') 探测网络映射盘。
// DSH 沙箱下 piped stdio 的 spawn 会抛 EPERM，而且这个异常没被 catch，会直接打断配置加载。
// 这里把同步抛出转成回调报错，让 Vite 走它自己的降级分支（当作没有网络盘）。
const childProcess = require('node:child_process')

const originalExec = childProcess.exec

childProcess.exec = function execWithSyncGuard(...args) {
    try {
        return originalExec.apply(this, args)
    } catch (error) {
        const callback = args.find((arg) => typeof arg === 'function')
        if (callback) {
            callback(error)
        }
        return undefined
    }
}
