// @vitest-environment jsdom
import {act} from 'react';
import {createRoot,type Root} from 'react-dom/client';
import {MemoryRouter,Routes,Route} from 'react-router-dom';
import {beforeEach,afterEach,it,expect,vi} from 'vitest';
import {AppProvider} from '../context/AppContext';
import {Login,Profile,ProtectedRoute} from './User';
import {authService} from '../services/auth';
let container:HTMLDivElement;let root:Root;
const user={id:'test-uuid',name:'Movie lover',mobile:'+919876543210',email:null,role:'USER',enabled:true,createdAt:'2026-09-29T12:00:00Z'};
beforeEach(()=>{(globalThis as Record<string,unknown>).IS_REACT_ACT_ENVIRONMENT=true;sessionStorage.clear();localStorage.clear();authService.logout();container=document.createElement('div');document.body.appendChild(container);root=createRoot(container);});
afterEach(async()=>{await act(async()=>root.unmount());container.remove();vi.unstubAllGlobals();});
async function input(node:HTMLInputElement,value:string){await act(async()=>{Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value')!.set!.call(node,value);node.dispatchEvent(new Event('input',{bubbles:true}));});}
async function submit(){await act(async()=>{container.querySelector('form')!.dispatchEvent(new Event('submit',{bubbles:true,cancelable:true}));});}
it('runs the login and editable profile UI against the API contract',async()=>{
 let saved={...user};const fetch=vi.fn(async(url:string,options:RequestInit={})=>{
 let body:unknown;
 if(url.endsWith('/otp/request'))body={success:true,expiresInSeconds:300,resendAfterSeconds:30};
 else if(url.endsWith('/otp/verify'))body={user:saved,accessToken:'test-token',tokenType:'Bearer'};
 else if(url.endsWith('/auth/me')){if(options.method==='PUT')saved={...saved,...JSON.parse(options.body as string)};body=saved;}
 else throw new Error(`Unexpected API ${url}`);
 return new Response(JSON.stringify(body),{status:200});
 });vi.stubGlobal('fetch',fetch);
 await act(async()=>root.render(<AppProvider><MemoryRouter initialEntries={['/login?next=/profile']}><Routes><Route path="/login" element={<Login/>}/><Route path="/verify-otp" element={<Login/>}/><Route path="/profile" element={<ProtectedRoute><Profile/></ProtectedRoute>}/></Routes></MemoryRouter></AppProvider>));
 await input(container.querySelector('input[type="tel"]')!,'9876543210');await submit();
 expect(container.textContent).toContain('Verify & continue');expect(container.textContent).not.toContain('123456');
 await input(container.querySelector('input[autocomplete="one-time-code"]')!,'654321');await submit();
 expect(container.textContent).toContain('Your profile');const fields=container.querySelectorAll('input');expect(fields[1].disabled).toBe(true);expect(fields[1].value).toBe('+919876543210');
 await input(fields[0],'Updated name');await input(fields[2],'user@example.com');await submit();
 expect(container.textContent).toContain('Your profile has been saved');expect(saved.name).toBe('Updated name');
 const logout=Array.from(container.querySelectorAll('button')).find(x=>x.textContent?.includes('Logout'))!;await act(async()=>logout.click());expect(container.textContent).toContain('Send OTP');expect(sessionStorage.getItem('av_access_token')).toBeNull();
});
