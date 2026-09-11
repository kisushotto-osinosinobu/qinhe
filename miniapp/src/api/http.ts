const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

export function assetUrl(path?:string):string|undefined{
  if(!path||/^https?:\/\//i.test(path))return path
  return API_BASE.replace(/\/api\/?$/,'')+(path.startsWith('/')?path:`/${path}`)
}

type Method='GET'|'POST'|'PUT'|'DELETE'
interface Envelope<T>{code:number;message:string;data:T}
export function request<T>(path:string,method:Method='GET',data?:unknown):Promise<T>{
  return new Promise((resolve,reject)=>{
    uni.request({url:API_BASE+path,method,data:data as any,header:{Authorization:token()?`Bearer ${token()}`:''},
      success(res:any){const body=res.data as Envelope<T>;if(res.statusCode===401){clearAuth();uni.showToast({title:'请重新登录',icon:'none'});reject(new Error('unauthorized'));return}if(res.statusCode>=200&&res.statusCode<300&&body.code===0)resolve(body.data);else{const message=body?.message||`请求失败 ${res.statusCode}`;uni.showToast({title:message,icon:'none'});reject(new Error(message))}},
      fail(err:any){uni.showToast({title:'无法连接服务器',icon:'none'});reject(err)}
    })
  })
}
export const token=()=>uni.getStorageSync<string>('token')||''
export const user=()=>uni.getStorageSync<any>('user')||null
export function saveAuth(value:{token:string;user:any}){uni.setStorageSync('token',value.token);uni.setStorageSync('user',value.user)}
export function clearAuth(){uni.removeStorageSync('token');uni.removeStorageSync('user')}
export function requireLogin(){if(token())return true;uni.navigateTo({url:'/pages/auth/login'});return false}
