export interface CartItem{id:number;name:string;specification?:string;unit:string;salePrice:number;availableQty:number;imageUrl?:string;quantity:number}
export function getCart():CartItem[]{return uni.getStorageSync<CartItem[]>('cart')||[]}
export function saveCart(items:CartItem[]){uni.setStorageSync('cart',items);try{uni.setTabBarBadge({index:1,text:String(items.reduce((s,x)=>s+x.quantity,0))})}catch{/* tab bar may not be mounted yet */}}
export function addCart(product:any){const list=getCart(),found=list.find(x=>x.id===product.id);if(product.availableQty<1){uni.showToast({title:'商品暂无库存',icon:'none'});return}if(found)found.quantity=Math.min(found.quantity+1,product.availableQty);else list.push({...product,quantity:1});saveCart(list);uni.showToast({title:'已加入购物车',icon:'success'})}
