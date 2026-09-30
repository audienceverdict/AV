// @vitest-environment jsdom
import {beforeEach,afterEach,describe,it,expect,vi} from 'vitest';
import {authService,adminUsers} from './auth';
const user={id:'user-uuid',name:'Movie lover',mobile:'+919876543210',email:null,role:'USER',enabled:true,createdAt:'2026-09-29T12:00:00Z'};
const response=(body:unknown,status=200)=>Promise.resolve(new Response(JSON.stringify(body),{status,headers:{'Content-Type':'application/json'}}));
beforeEach(()=>{sessionStorage.clear();localStorage.clear();authService.logout();});
afterEach(()=>vi.unstubAllGlobals());
describe('backend authentication',()=>{
 it('requests an OTP, verifies it, restores /me, updates profile, and logs out',async()=>{
 const fetch=vi.fn().mockImplementationOnce(()=>response({success:true,expiresInSeconds:300,resendAfterSeconds:30})).mockImplementationOnce(()=>response({user,accessToken:'server-token',tokenType:'Bearer'})).mockImplementationOnce(()=>response(user)).mockImplementationOnce(()=>response({...user,name:'John',email:'john@example.com'}));vi.stubGlobal('fetch',fetch);
 await authService.requestOtp('9876543210');expect(fetch.mock.calls[0][0]).toBe('/api/v1/auth/otp/request');
 await authService.verifyOtp('9876543210','654321');expect(fetch.mock.calls[2][0]).toBe('/api/v1/auth/me');expect(fetch.mock.calls[2][1].headers.Authorization).toBe('Bearer server-token');expect(authService.current()?.email).toBe('');expect(authService.isAdmin()).toBe(false);
 await authService.updateProfile('John','john@example.com');expect(JSON.parse(fetch.mock.calls[3][1].body)).toEqual({name:'John',email:'john@example.com'});expect(authService.current()?.name).toBe('John');authService.logout();expect(authService.current()).toBeNull();expect(sessionStorage.getItem('av_access_token')).toBeNull();
 });
 it('ignores legacy demo admin flags and trusts the backend role',async()=>{
 localStorage.setItem('av_admin_session','true');expect(authService.isAdmin()).toBe(false);sessionStorage.setItem('av_access_token','admin-token');vi.stubGlobal('fetch',vi.fn(()=>response({...user,role:'ADMIN'})));await authService.restore();expect(authService.isAdmin()).toBe(true);
 });
 it('clears expired authentication and exposes backend errors',async()=>{
 sessionStorage.setItem('av_access_token','expired');vi.stubGlobal('fetch',vi.fn(()=>response({message:'Authentication required'},401)));await expect(authService.restore()).rejects.toThrow('Authentication required');expect(authService.current()).toBeNull();expect(sessionStorage.getItem('av_access_token')).toBeNull();
 });
 it('does not restore a session after logout races with /me',async()=>{
 sessionStorage.setItem('av_access_token','old-token');let finish!:(value:Response)=>void;vi.stubGlobal('fetch',vi.fn(()=>new Promise<Response>(resolve=>{finish=resolve;})));const restore=authService.restore();authService.logout();finish(new Response(JSON.stringify(user)));await restore;expect(authService.current()).toBeNull();
 });
 it('sends admin operations to protected endpoints',async()=>{
 sessionStorage.setItem('av_access_token','admin-token');const fetch=vi.fn((_url:string,_options?:RequestInit)=>response(user));vi.stubGlobal('fetch',fetch);await adminUsers.role(user.id,'ADMIN');await adminUsers.status(user.id,false);expect(fetch.mock.calls[0][0]).toBe('/api/v1/admin/users/user-uuid/role');expect(fetch.mock.calls[1][0]).toBe('/api/v1/admin/users/user-uuid/status');
 });
});
