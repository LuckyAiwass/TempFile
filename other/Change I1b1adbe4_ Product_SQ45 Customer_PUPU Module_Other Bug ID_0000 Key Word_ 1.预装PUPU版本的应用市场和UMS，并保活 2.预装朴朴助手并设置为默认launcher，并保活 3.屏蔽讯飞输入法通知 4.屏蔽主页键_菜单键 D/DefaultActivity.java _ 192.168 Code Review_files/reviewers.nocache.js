function reviewers(){var O='bootstrap',P='begin',Q='gwt.codesvr.reviewers=',R='gwt.codesvr=',S='reviewers',T='startup',U='DUMMY',V=0,W=1,X='iframe',Y='position:absolute; width:0; height:0; border:none; left: -1000px;',Z=' top: -1000px;',$='CSS1Compat',_='<!doctype html>',ab='',bb='<html><head><\/head><body><\/body><\/html>',cb='undefined',db='readystatechange',eb=10,fb='Chrome',gb='eval("',hb='");',ib='script',jb='javascript',kb='moduleStartup',lb='moduleRequested',mb='Failed to load ',nb='head',ob='meta',pb='name',qb='reviewers::',rb='::',sb='gwt:property',tb='content',ub='=',vb='gwt:onPropertyErrorFn',wb='Bad handler "',xb='" for "gwt:onPropertyErrorFn"',yb='gwt:onLoadErrorFn',zb='" for "gwt:onLoadErrorFn"',Ab='#',Bb='?',Cb='/',Db='img',Eb='clear.cache.gif',Fb='baseUrl',Gb='reviewers.nocache.js',Hb='base',Ib='//',Jb='user.agent',Kb='webkit',Lb='safari',Mb='msie',Nb=11,Ob='ie10',Pb=9,Qb='ie9',Rb=8,Sb='ie8',Tb='gecko',Ub='gecko1_8',Vb=2,Wb=3,Xb=4,Yb='selectingPermutation',Zb='reviewers.devmode.js',$b='5BD8EB615556C658B421CA6545D1EA27',_b='6A1CB8341B00B2D45D89FCCEC5908DEA',ac='770D6EF3F4BBC7AF1F6BAC8C2424F326',bc='95ED61D7E4775EBE1E6AE67D440382A7',cc='9E16EF8125D0A32C9B9ABC035271754C',dc=':',ec='.cache.js',fc='link',gc='rel',hc='stylesheet',ic='href',jc='loadExternalRefs',kc='reviewers.css',lc='end',mc='http:',nc='file:',oc='_gwt_dummy_',pc='__gwtDevModeHook:reviewers',qc='Ignoring non-whitelisted Dev Mode URL: ',rc=':moduleBase';var o=window;var p=document;r(O,P);function q(){var a=o.location.search;return a.indexOf(Q)!=-1||a.indexOf(R)!=-1}
function r(a,b){if(o.__gwtStatsEvent){o.__gwtStatsEvent({moduleName:S,sessionId:o.__gwtStatsSessionId,subSystem:T,evtGroup:a,millis:(new Date).getTime(),type:b})}}
reviewers.__sendStats=r;reviewers.__moduleName=S;reviewers.__errFn=null;reviewers.__moduleBase=U;reviewers.__softPermutationId=V;reviewers.__computePropValue=null;reviewers.__getPropMap=null;reviewers.__installRunAsyncCode=function(){};reviewers.__gwtStartLoadingFragment=function(){return null};reviewers.__gwt_isKnownPropertyValue=function(){return false};reviewers.__gwt_getMetaProperty=function(){return null};var s=null;var t=o.__gwt_activeModules=o.__gwt_activeModules||{};t[S]={moduleName:S};reviewers.__moduleStartupDone=function(e){var f=t[S].bindings;t[S].bindings=function(){var a=f?f():{};var b=e[reviewers.__softPermutationId];for(var c=V;c<b.length;c++){var d=b[c];a[d[V]]=d[W]}return a}};var u;function v(){w();return u}
function w(){if(u){return}var a=p.createElement(X);a.id=S;a.style.cssText=Y+Z;a.tabIndex=-1;p.body.appendChild(a);u=a.contentWindow.document;u.open();var b=document.compatMode==$?_:ab;u.write(b+bb);u.close()}
function A(k){function l(a){function b(){if(typeof p.readyState==cb){return typeof p.body!=cb&&p.body!=null}return /loaded|complete/.test(p.readyState)}
var c=b();if(c){a();return}function d(){if(!c){if(!b()){return}c=true;a();if(p.removeEventListener){p.removeEventListener(db,d,false)}if(e){clearInterval(e)}}}
if(p.addEventListener){p.addEventListener(db,d,false)}var e=setInterval(function(){d()},eb)}
function m(c){function d(a,b){a.removeChild(b)}
var e=v();var f=e.body;var g;if(navigator.userAgent.indexOf(fb)>-1&&window.JSON){var h=e.createDocumentFragment();h.appendChild(e.createTextNode(gb));for(var i=V;i<c.length;i++){var j=window.JSON.stringify(c[i]);h.appendChild(e.createTextNode(j.substring(W,j.length-W)))}h.appendChild(e.createTextNode(hb));g=e.createElement(ib);g.language=jb;g.appendChild(h);f.appendChild(g);d(f,g)}else{for(var i=V;i<c.length;i++){g=e.createElement(ib);g.language=jb;g.text=c[i];f.appendChild(g);d(f,g)}}}
reviewers.onScriptDownloaded=function(a){l(function(){m(a)})};r(kb,lb);var n=p.createElement(ib);n.src=k;if(reviewers.__errFn){n.onerror=function(){reviewers.__errFn(S,new Error(mb+code))}}p.getElementsByTagName(nb)[V].appendChild(n)}
reviewers.__startLoadingFragment=function(a){return D(a)};reviewers.__installRunAsyncCode=function(a){var b=v();var c=b.body;var d=b.createElement(ib);d.language=jb;d.text=a;c.appendChild(d);c.removeChild(d)};function B(){var c={};var d;var e;var f=p.getElementsByTagName(ob);for(var g=V,h=f.length;g<h;++g){var i=f[g],j=i.getAttribute(pb),k;if(j){j=j.replace(qb,ab);if(j.indexOf(rb)>=V){continue}if(j==sb){k=i.getAttribute(tb);if(k){var l,m=k.indexOf(ub);if(m>=V){j=k.substring(V,m);l=k.substring(m+W)}else{j=k;l=ab}c[j]=l}}else if(j==vb){k=i.getAttribute(tb);if(k){try{d=eval(k)}catch(a){alert(wb+k+xb)}}}else if(j==yb){k=i.getAttribute(tb);if(k){try{e=eval(k)}catch(a){alert(wb+k+zb)}}}}}__gwt_getMetaProperty=function(a){var b=c[a];return b==null?null:b};s=d;reviewers.__errFn=e}
function C(){function e(a){var b=a.lastIndexOf(Ab);if(b==-1){b=a.length}var c=a.indexOf(Bb);if(c==-1){c=a.length}var d=a.lastIndexOf(Cb,Math.min(c,b));return d>=V?a.substring(V,d+W):ab}
function f(a){if(a.match(/^\w+:\/\//)){}else{var b=p.createElement(Db);b.src=a+Eb;a=e(b.src)}return a}
function g(){var a=__gwt_getMetaProperty(Fb);if(a!=null){return a}return ab}
function h(){var a=p.getElementsByTagName(ib);for(var b=V;b<a.length;++b){if(a[b].src.indexOf(Gb)!=-1){return e(a[b].src)}}return ab}
function i(){var a=p.getElementsByTagName(Hb);if(a.length>V){return a[a.length-W].href}return ab}
function j(){var a=p.location;return a.href==a.protocol+Ib+a.host+a.pathname+a.search+a.hash}
var k=g();if(k==ab){k=h()}if(k==ab){k=i()}if(k==ab&&j()){k=e(p.location.href)}k=f(k);return k}
function D(a){return reviewers.__moduleBase+a}
function F(){var f=[];var g=V;function h(a,b){var c=f;for(var d=V,e=a.length-W;d<e;++d){c=c[a[d]]||(c[a[d]]=[])}c[a[e]]=b}
var i=[];var j=[];function k(a){var b=j[a](),c=i[a];if(b in c){return b}var d=[];for(var e in c){d[c[e]]=e}if(s){s(a,d,b)}throw null}
j[Jb]=function(){var a=navigator.userAgent.toLowerCase();var b=p.documentMode;if(function(){return a.indexOf(Kb)!=-1}())return Lb;if(function(){return a.indexOf(Mb)!=-1&&(b>=eb&&b<Nb)}())return Ob;if(function(){return a.indexOf(Mb)!=-1&&(b>=Pb&&b<Nb)}())return Qb;if(function(){return a.indexOf(Mb)!=-1&&(b>=Rb&&b<Nb)}())return Sb;if(function(){return a.indexOf(Tb)!=-1||b>=Nb}())return Ub;return ab};i[Jb]={'gecko1_8':V,'ie10':W,'ie8':Vb,'ie9':Wb,'safari':Xb};__gwt_isKnownPropertyValue=function(a,b){return b in i[a]};reviewers.__getPropMap=function(){var a={};for(var b in i){if(i.hasOwnProperty(b)){a[b]=k(b)}}return a};reviewers.__computePropValue=k;o.__gwt_activeModules[S].bindings=reviewers.__getPropMap;r(O,Yb);if(q()){return D(Zb)}var l;try{h([Qb],$b);h([Ub],_b);h([Ob],ac);h([Lb],bc);h([Sb],cc);l=f[k(Jb)];var m=l.indexOf(dc);if(m!=-1){g=parseInt(l.substring(m+W),eb);l=l.substring(V,m)}}catch(a){}reviewers.__softPermutationId=g;return D(l+ec)}
function G(){if(!o.__gwt_stylesLoaded){o.__gwt_stylesLoaded={}}function c(a){if(!__gwt_stylesLoaded[a]){var b=p.createElement(fc);b.setAttribute(gc,hc);b.setAttribute(ic,D(a));p.getElementsByTagName(nb)[V].appendChild(b);__gwt_stylesLoaded[a]=true}}
r(jc,P);c(kc);r(jc,lc)}
B();reviewers.__moduleBase=C();t[S].moduleBase=reviewers.__moduleBase;var H=F();if(o){var I=!!(o.location.protocol==mc||o.location.protocol==nc);o.__gwt_activeModules[S].canRedirect=I;function J(){var b=oc;try{o.sessionStorage.setItem(b,b);o.sessionStorage.removeItem(b);return true}catch(a){return false}}
if(I&&J()){var K=pc;var L=o.sessionStorage[K];if(!/^http:\/\/(localhost|127\.0\.0\.1)(:\d+)?\/.*$/.test(L)){if(L&&(window.console&&console.log)){console.log(qc+L)}L=ab}if(L&&!o[K]){o[K]=true;o[K+rc]=C();var M=p.createElement(ib);M.src=L;var N=p.getElementsByTagName(nb)[V];N.insertBefore(M,N.firstElementChild||N.children[V]);return false}}}G();r(O,lc);A(H);return true}
reviewers.succeeded=reviewers();