import assert from 'node:assert/strict';
const url=process.env.WS_URL||'ws://localhost:5174/ws';
async function client(){const ws=new WebSocket(url);const queue=[];let waiting;ws.onmessage=e=>{const m=JSON.parse(e.data);if(waiting){const w=waiting;waiting=null;w(m);}else queue.push(m);};await new Promise((resolve,reject)=>{ws.onopen=resolve;ws.onerror=reject;});return {ws,send:m=>ws.send(JSON.stringify(m)),next:()=>queue.length?Promise.resolve(queue.shift()):new Promise((resolve,reject)=>{const t=setTimeout(()=>reject(Error('Timed out waiting for server')),4000);waiting=m=>{clearTimeout(t);resolve(m);};})};}
const a=await client(),b=await client(),c=await client();
try{
 a.send({type:'create',name:'Alice'});let state=await a.next();assert.equal(state.type,'state');const code=state.code;
 a.send({type:'move',column:0});assert.equal((await a.next()).type,'error');
 b.send({type:'join',name:'Bob',code});assert.equal((await a.next()).names.length,2);assert.equal((await b.next()).you,2);
 c.send({type:'join',name:'Charlie',code});assert.equal((await c.next()).type,'error');
 b.send({type:'move',column:2});assert.equal((await b.next()).type,'error');
 a.send({type:'move',column:1.5});assert.equal((await a.next()).type,'error');
 for(const [i,column] of [0,1,0,1,0,1,0].entries()){(i%2?b:a).send({type:'move',column});state=await a.next();assert.deepEqual((await b.next()).game,state.game);}
 assert.equal(state.game.winner,1);assert.equal(state.game.moves,7);
 a.send({type:'rematch'});assert.equal((await a.next()).rematch,1);await b.next();
 b.send({type:'rematch'});state=await a.next();assert.equal(state.game.moves,0);assert.equal(state.you,2);assert.equal((await b.next()).you,1);
 b.ws.close();assert.equal((await a.next()).type,'closed');
 c.send({type:'join',name:'Charlie',code});assert.equal((await c.next()).type,'error');
 console.log('PASS: room creation, join, full room, turn validation, invalid move, synchronization, win, rematch, disconnect cleanup');
}finally{a.ws.close();b.ws.close();c.ws.close();}
