// Read-only inspection of the Android WebView DOM. Gestures remain device actions.
import {execFileSync} from 'node:child_process';
import {readFileSync} from 'node:fs';
if(!readFileSync('/tmp/reader-ldx-issues-9-10-emulator-lease/owner','utf8').startsWith('issue-10'))throw Error('Exclusive issue-10 emulator lease required');
const pid=execFileSync('adb',['-s','emulator-5554','shell','pidof','dev.reader.ldx'],{encoding:'utf8'}).trim();
execFileSync('adb',['-s','emulator-5554','forward','tcp:9228',`localabstract:webview_devtools_remote_${pid}`]);
const tabs=await (await fetch('http://localhost:9228/json/list')).json();
const expression=`(()=>{let s=getSelection(),r=s.rangeCount?s.getRangeAt(0):null,e=document.scrollingElement;return {url:location.href,text:s.toString(),range:r?{startId:r.startContainer.parentElement.id,startOffset:r.startOffset,endId:r.endContainer.parentElement.id,endOffset:r.endOffset,rects:Array.from(r.getClientRects(),b=>({x:b.x,y:b.y,width:b.width,height:b.height}))}:null,viewport:{width:innerWidth,height:innerHeight,scrollLeft:e.scrollLeft,scrollWidth:e.scrollWidth},font:getComputedStyle(document.body).fontSize,overflow:{html:getComputedStyle(document.documentElement).overflow,body:getComputedStyle(document.body).overflow},boundary:Array.from(document.querySelectorAll("#p-2-12")).flatMap(p=>["Una", "canción"].map(word=>{let n=p.firstChild,i=n.textContent.indexOf(word),q=document.createRange();q.setStart(n,i);q.setEnd(n,i+word.length);let b=q.getBoundingClientRect();return {word,element:p.id,start:i,end:i+word.length,x:b.x,y:b.y,width:b.width,height:b.height}}))}})()`;
for(const tab of tabs){
 if(JSON.parse(tab.description).screenX!==0)continue;
 const ws=new WebSocket(tab.webSocketDebuggerUrl);
 await new Promise((resolve,reject)=>{ws.onopen=()=>ws.send(JSON.stringify({id:1,method:'Runtime.evaluate',params:{expression,returnByValue:true}}));ws.onerror=reject;ws.onmessage=event=>{const m=JSON.parse(event.data);if(m.id===1){console.log(JSON.stringify({utc:new Date().toISOString(),...m.result.result.value},null,2));ws.close();resolve();}};});
}
