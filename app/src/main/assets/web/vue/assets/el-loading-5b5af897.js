import{u}from"./index-ab2e286b.js";const f=/\[([^\]]+)]|Y{1,4}|M{1,4}|D{1,2}|d{1,4}|H{1,2}|h{1,2}|a{1,2}|A{1,2}|m{1,2}|s{1,2}|Z{1,2}|SSS/g;function D(t,n,e,a){let s=t<12?"AM":"PM";return a&&(s=s.split("").reduce((l,i)=>l+=`${i}.`,"")),e?s.toLowerCase():s}function L(t,n,e={}){var a;const s=t.getFullYear(),l=t.getMonth(),i=t.getDate(),o=t.getHours(),r=t.getMinutes(),g=t.getSeconds(),m=t.getMilliseconds(),d=t.getDay(),c=(a=e.customMeridiem)!=null?a:D,M={YY:()=>String(s).slice(-2),YYYY:()=>s,M:()=>l+1,MM:()=>`${l+1}`.padStart(2,"0"),MMM:()=>t.toLocaleDateString(e.locales,{month:"short"}),MMMM:()=>t.toLocaleDateString(e.locales,{month:"long"}),D:()=>String(i),DD:()=>`${i}`.padStart(2,"0"),H:()=>String(o),HH:()=>`${o}`.padStart(2,"0"),h:()=>`${o%12||12}`.padStart(1,"0"),hh:()=>`${o%12||12}`.padStart(2,"0"),m:()=>String(r),mm:()=>`${r}`.padStart(2,"0"),s:()=>String(g),ss:()=>`${g}`.padStart(2,"0"),SSS:()=>`${m}`.padStart(3,"0"),d:()=>d,dd:()=>t.toLocaleDateString(e.locales,{weekday:"narrow"}),ddd:()=>t.toLocaleDateString(e.locales,{weekday:"short"}),dddd:()=>t.toLocaleDateString(e.locales,{weekday:"long"}),A:()=>c(o,r),AA:()=>c(o,r,!1,!0),a:()=>c(o,r,!0),aa:()=>c(o,r,!0,!0)};return n.replace(f,(S,h)=>h||M[S]())}const w=t=>/,\s*\{/.test(t)||!(t.startsWith("http")||t.startsWith("data:")||t.startsWith("blob:"));function Y(t){return location.origin+"/image?path="+encodeURIComponent(t)+"&url="+encodeURIComponent(sessionStorage.getItem("bookUrl"))+"&width="+u().config.readWidth}const $=t=>{let n=new Date().getTime(),e=Math.floor((n-t)/1e3),a="";return e<=30?a="刚刚":e<60?a=e+"秒前":e<3600?a=Math.floor(e/60)+"分钟前":e<86400?a=Math.floor(e/3600)+"小时前":e<2592e3?a=Math.floor(e/86400)+"天前":a=L(new Date(t),"YYYY-MM-DD"),a},v='<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M512 64a32 32 0 0 1 32 32v192a32 32 0 0 1-64 0V96a32 32 0 0 1 32-32zm0 640a32 32 0 0 1 32 32v192a32 32 0 1 1-64 0V736a32 32 0 0 1 32-32zm448-192a32 32 0 0 1-32 32H736a32 32 0 1 1 0-64h192a32 32 0 0 1 32 32zm-640 0a32 32 0 0 1-32 32H96a32 32 0 0 1 0-64h192a32 32 0 0 1 32 32zM195.2 195.2a32 32 0 0 1 45.248 0L376.32 331.008a32 32 0 0 1-45.248 45.248L195.2 240.448a32 32 0 0 1 0-45.248zm452.544 452.544a32 32 0 0 1 45.248 0L828.8 783.552a32 32 0 0 1-45.248 45.248L647.744 692.992a32 32 0 0 1 0-45.248zM828.8 195.264a32 32 0 0 1 0 45.184L692.992 376.32a32 32 0 0 1-45.248-45.248l135.808-135.808a32 32 0 0 1 45.248 0zm-452.544 452.48a32 32 0 0 1 0 45.248L240.448 828.8a32 32 0 0 1-45.248-45.248l135.808-135.808a32 32 0 0 1 45.248 0z"/></svg>';export{$ as d,Y as g,w as i,v as l};
